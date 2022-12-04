package application;

import java.net.URISyntaxException;
import java.util.ArrayList;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;

/** Enemy is an object that represents all hostile entities inside levels that actively will try to harm the player character */
public class Enemy extends PhysicsSprite{
	
	//The area from itself that an enemy can "see" a player, acts as an invisible hitbox
	private Rectangle detectionArea;
	private Rectangle damageSpot;
	private Rectangle vulnerableSpot;
	
	private static AudioClip hurt; //The sound effect played upon an enemy being damaged, static since it must apply to ALL enemies.
	
	private Image leftFacing; //The sprite of an enemy when it is facing the left
	private Image rightFacing; //The sprite of an enemy when it is facing the right
	
	private double health;	//Amount of health an enemy has, when depleted will be de-rendered.
	private double speed = 3; //Rate at which the enemy will move

	/**Constructs an enemy with no sprite
	 * @param mass The mass another object needs to move the Enemy.
	 * @param gravity The rate at which the Enemy falls.
	 * @param health The amount of health an enemy has.
	 * */
	public Enemy(double mass, double gravity, double health) {
		super(mass, gravity);
		this.health = health;
		
		//Initializes hurt
		try {
			hurt = new AudioClip(getClass().getResource("/Music/umph-47201.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}//end Enemy constructor
	
	/**Constructs an enemy with a left facing and right facing sprite
	 * @param mass The mass another object needs to move the Enemy.
	 * @param gravity The rate at which the Enemy falls.
	 * @param health The amount of health an enemy has.
	 * @param rightFileName The path to the picture an enemy uses when facing the right.
	 * @param leftFileName The path to the picture an enemy uses when facing the left.
	 * */
	public Enemy(double mass, double gravity, double health, String rightFileName, String leftFileName) {
		super(mass, gravity);
		this.health = health;
		
		rightFacing = new Image(rightFileName);
		leftFacing = new Image(leftFileName);
		
		try {
			hurt = new AudioClip(getClass().getResource("/Music/umph-47201.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}//end Enemy constructor
	
	/** Changes the volume of the hurt sound effect.
	 * @param volume The number the volume of hurt is supposed to be changed by.
	 */
	public static void changeVolume(double volume) {
		if(hurt == null) return;
		hurt.setVolume(volume * 0.003);
	}
	
	/** Sets the Sprite the Enemy initially starts with.
	 * @param filename The path to the picture.
	 * */
	@Override
	public void setImage(String filename) {
		Image image = new Image(filename);
		this.setImage(image);
		this.getBoundary().setWidth(image.getWidth());
		this.getBoundary().setHeight(image.getHeight());
		detectionArea = new Rectangle(0, 0, this.getBoundaryWidth() + 600, this.getBoundaryHeight() + 600);
		damageSpot = new Rectangle(0, 0, this.getBoundaryWidth() + 20, this.getBoundaryHeight());
		vulnerableSpot = new Rectangle(0, 0, this.getBoundaryWidth() - 20, 20);
	}
	
	/** Updates the Enemy's attributes and position.
	 * @param surfaces The list of sprites an Enemy can interact with
	 * */
	@Override
	public void update(ArrayList<Sprite> surfaces) {
		//Resets static collision and momentum so it won't continually act as if its being hit by something before checking.
		this.setJustCollidedStatically(false);
		this.setMomentum(this.getMass());
		
		//Updates the total velocity
		this.getTotalVelocity().set(this.getVelocity());
		
		//If a temporary velocity is present adds it into the total velocity.
		if(this.getTempVelocity() != null) {
			this.getTotalVelocity().add(this.getTempVelocity());
			this.getTempVelocity().decay();
		}//end if
		
		this.collide(surfaces); //Checks for collisions
		
		this.addPosition(getTotalVelocity()); //Updates position
		
		this.fall(surfaces); //Updates velocity to reflect whether Enemy is falling or not
		
		this.setVelocityX(0); //Sets internal velocity to 0 before Enemy knows where to move
		
		//Updates position of all possible collision zones for enemy
		detectionArea.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - detectionArea.getWidth()/2, this.getBoundaryY() + this.getBoundaryHeight()/2 - detectionArea.getHeight()/2);
		damageSpot.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - damageSpot.getWidth()/2, this.getBoundaryY() + this.getBoundaryHeight()/2 - damageSpot.getHeight()/2 + 10);
		vulnerableSpot.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - vulnerableSpot.getWidth()/2, this.getBoundaryY() - vulnerableSpot.getHeight()/2);
		
		PlayerObject player; //Pointer for playerObject used for convenience
		
		//Runs through surfaces to check where the player is
		for(Sprite pointer : surfaces) {
			
			//Checks that the current pointer is the player
			if(pointer instanceof PlayerObject) {
				player = (PlayerObject)pointer;
				
				//If player is ontop of the Enemy vulnerable spot Enemy takes damage.
				if(vulnerableSpot.overlaps(player.getBoundary())) {
					hurt.play();
					this.getHit(player);
				}
				//If player ISN'T damaging the enemy but is still overlapping the damageSpot player is hit instead.
				else if(damageSpot.overlaps(player.getBoundary())) this.hitPlayer(player);
				
				//If player is still inside the detectionArea of Enemy the Enemy updates its X velocity to attempt to move closer to player
				if(detectionArea.overlaps(player.getBoundary())) moveIfDetect(player);

			}//end if
			
		}//end for
		
		
		
	}
	
	//Helper method that determines what direction the Enemy should move if the player is inside its detectionArea.
	private void moveIfDetect(PlayerObject player) {
		
		//Moves left if player is to left.
		if(detectionArea.getX() + detectionArea.getWidth()/2 > player.getBoundaryX() + player.getBoundaryWidth()/2) {
			this.setVelocityX(-speed);
			this.setImage(leftFacing);
			return;
		}//end if
		
		//Moves right if player is to right
		if(detectionArea.getX() + detectionArea.getWidth()/2 < player.getBoundaryX() + player.getBoundaryWidth()/2) {
			this.setVelocityX(speed);
			this.setImage(rightFacing);
			return;
		}//end if

	}//end moveIfDetect

	//Helper method that hits player while also bumping Enemy to prevent rapid collisions.
	private void hitPlayer(PlayerObject player) {
		player.takeDamage(this);
		this.bump(player);
	}//end hitPlayer

	//Helper method to determine how much damage Enemy takes form a player attack.
	private void getHit(PlayerObject player) {
		player.bump(this); //Player is bumped to prevent rapid collisions
		
		//If statements check what size player is, more health is subtracted from more mass
		if(player.getMass() == 0.5) health -= 20;
		else if(player.getMass() == 1) health -= 80;
		else if(player.getMass() == 2) health -= 240;
		
		this.bump(player);
	}//end getHit
	
	
	//Helper method that "bumps" the Enemy in a direction away from the player
	private void bump(PlayerObject player) {
		
		Vector bumpVelocity = new Vector(0, 0);
		
		//Bumps the Enemy to the left if player is to the right, else bumps to the right
		if(this.getBoundary().toLeft(player.getBoundary()))
			bumpVelocity.setX(-3);
		else
			bumpVelocity.setX(3);
		
		//Bumps Enemy upwards if player is below Enemy, prevents Enemy landing on player being an instakill
		if(this.getBoundaryY() + this.getBoundaryHeight() <= player.getBoundaryY())
			bumpVelocity.setY(-10);
		
		this.addDecayingVelocity(bumpVelocity, 0.3);
	}//end bump

	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}
	
	
}//end Enemy
