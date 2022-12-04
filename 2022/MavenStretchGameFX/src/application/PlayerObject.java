package application;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.scene.media.AudioClip;

/** PlayerObject is an instance of PhysicsSprite that represents the player character */
public class PlayerObject extends PhysicsSprite {
	
	private double health; //The amount of health the player has
	
	//AudioClips static so volume can be changed anywhere and applies to ALL PlayerObject instances
	private static AudioClip slimeJump; //The sound played when the player jumps or lands
	private static AudioClip slimeHurt; //The sound played when the player takes damage
	
	//Sprites below are used to check when player is in an area it can transform in
	private Rectangle bigSlimeHitBox = new Rectangle(0, 0, 500, 376); //A rectangle the size of the large slime Sprite's hitbox
	private Rectangle normalSlimeHitBox = new Rectangle(0, 0, 225, 169);//A rectangle the size of the normal slime Sprite's hitbox

	private int bounces = 0; //The number of times the player has bounced

	/** Constructs a PlayerObject
	 * @param mass A double that determines what the player can and can't move
	 * @param gravity The rate at which the player falls
	 * @param health The amount of health the player has
	 * */
	public PlayerObject(double mass, double gravity, double health) {
		super(mass, gravity);
		this.health = health;
		
		try {
			slimeJump = new AudioClip(getClass().getResource("/Music/slimejump-6913.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		try {
			slimeHurt = new AudioClip(getClass().getResource("/Music/slime-squish-14539.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}	
	}//end PlayerObject constructor
	
	public void playJumpSound() {
		slimeJump.play();
	}//end playJumpSound
	
	/** changeVolume updates the sound of the slimeJump and slimeHurt AudioClips
	 * @param volume The number that the volume is to be updated by
	 * */
	public static void changeVolume(double volume) {
		slimeJump.setVolume(volume * 0.006);
		slimeHurt.setVolume(volume * 0.0015);
	}//end changeVolume
	
	/** landOn either will cause the player to land on a Sprite or bounce
	 * @param surface The Sprite that the player can land on
	 * */
	//PRE: Surface is a non-null pointer to a sprite the player is going to land on.
	//POST: Player will either bounce off the surface or land on it.
	public void landOn(Sprite surface) {
		
		//If the surface is ever registered as null the program will close to avoid more severe errores
		if(surface == null) { 
			System.exit(0);
		}
		
		//Checks if the player is falling at a rate acceptable to cause a bounce, also checks that player can only
		//bounce 3 times consecutively
		if(this.getVelocityY() < 19 || bounces > 3){
			this.setVelocityY(0);
			this.setPositionY(surface.getBoundaryY() - this.getBoundaryHeight());
			bounces = 0;
			return;
		}//end if
		
		this.setVelocityY(-this.getVelocityY()/2);
		this.setPositionY(surface.getBoundaryY() - this.getBoundaryHeight());
		bounces++;
		slimeJump.play();
	}//end landOn
	
	/** Updates the player to check its collisions and also if it needs to take damage from a level hazard
	 * @param surfaces The Sprites in a level that the PlayerObject can collide with.
	 * @param enemies The level hazards in a level that the PlayerObject will be damaged by.
	 * */
	//PRE: Surfaces is a list of all sprites player can land on, and enemies is a list of sprites player can take damage from.
	//POST: Updates player position to fall or land and then collides with any enemies.
	public void playerUpdate(ArrayList<Sprite> surfaces, ArrayList<Sprite> enemies) {
		//Resets static collision and momentum so it won't continually act as if its being hit by something before checking.
		//TODO: Reset static collision for the player
		this.setMomentum(this.getMass());
		
		//Checks what direction the player should be facing if it is normal sized
		if(this.getMass() == 1) {
			if(this.getVelocityX() > 0)
				this.setImage("Pictures/SlimeRight.png");
			else if (this.getVelocityX() < 0)
				this.setImage("Pictures/Slime.png");
		}
		
		//Checks what direction the player should be facing if it is large.
		else if(this.getMass() == 2) {
			if(this.getVelocityX() > 0)
				this.setImage("Pictures/BigSlimeRight.png");
			else if (this.getVelocityX() < 0)
				this.setImage("Pictures/BigSlime.png");
		}
		
		//Checks what direction the player should be facing if it is small.
		else if(this.getMass() == 0.5) {
			if(this.getVelocityX() > 0)
				this.setImage("Pictures/smolSlimeRight.png");
			else if (this.getVelocityX() < 0)
				this.setImage("Pictures/smolSlime.png");
		}
		
		this.update(surfaces); //Checks collisions and falling
		
		//Updates bigSlimeHitBox and normalSlimeHitBox to match the current player position, both slightly above so floor doesn't count as preventing transformation
		this.bigSlimeHitBox.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - 250, this.getBoundaryY() + this.getBoundaryHeight() - 376 - 1);
		this.normalSlimeHitBox.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - 112.5, this.getBoundaryY() + this.getBoundaryHeight() - 169 - 1);
		
		Iterator<Sprite> it = enemies.iterator();
		Sprite pointer;
		
		//Checks if the PlayerObject has collided with any enemies, taking damage if it has
		while(it.hasNext()) {
			pointer = it.next();
			if(this.hit(pointer)) break;
		}
		
		//Kills the player if they have been falling for too long, in the case of jumping of the map
		if(this.getVelocityY() > 300) health -= 20;
	}//end PlayerUpdate
	
	/** Checks if the player is overlapping a level hazard, and if so takes damage 
	 * @param enemy The level hazard being checked for collision
	 * */
	public boolean hit(Sprite enemy) {
		
		//Checks if the enemy and PlayerObject are overlapping
		if (this.overlaps(enemy)) {
			health -= 10;
			slimeHurt.play();
			this.bump(enemy);
			return true;
		}//end if
	
		return false;
	}//end hit
	
	
	/** takeDamage causes the player to take damage then be bumped away from its source.
	 * @param enemy The source of the damage the player took
	 * */
	public void takeDamage(Enemy enemy) {
		health -= 10;
		slimeHurt.play();
		this.bump(enemy);
	}//end takeDamage
	
	
	/** bump moves the player in a direction away from a sprite
	 * @param enemy The Sprite the PlayerObject is "bumped" away from
	 * */
	//PRE: Enemy isn't a NULL sprite pointer
	//POST: The player is "bumped" out of Enemie's hitbox
	public void bump(Sprite enemy) {

		Vector bumpVelocity = new Vector(0, 0);
		
		//Bumps player below the enemy if the enemy is above, otherwise bumps player upwards
		if(this.getBoundary().below(enemy.getBoundary()))
			this.setVelocityY(3);
		else
			this.setVelocityY(-10);
		
		//Bumps player to the left if enemy is to the rightm otherwise bumps player to the right
		if(this.getBoundary().toLeft(enemy.getBoundary()))
			bumpVelocity.setX(-3);
		else
			bumpVelocity.setX(3);
			
		this.addDecayingVelocity(bumpVelocity, 0.3);
	}//end bump
	
	
	/** grow will change the PlayerObject's sprite into the large slime sprite if there is room.
	 * @param surfaces The list of Sprites the player can collide with which prevent room for growth.
	 * */
	//PRE: A list of all the surfaces Sprite can collide with
	//POST: The player is transformed into its large size
	public void grow(ArrayList<Sprite> surfaces) {
		for(Sprite other : surfaces)
			if(bigSlimeHitBox.overlaps(other.getBoundary()) && other.isSolid()) {
				if(other == this) continue;
				return;
			}
		this.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - 250, this.getBoundaryY() + this.getBoundaryHeight() - 376); //Centers slime where it previously was
		this.setImage("Pictures/BigSlime.png");
		this.setMass(2);
	}//end grow
	
	
	/** shrink will change the sprite into its small size
	 * */
	//PRE: none
	//POST: The player is transformed into its small size
	public void shrink() {
		this.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - 50, this.getBoundaryY() + this.getBoundaryHeight() - 75);
		this.setImage("Pictures/smolSlime.png");
		this.setMass(0.5);
	}//end shrink
	
	
	/** grow will change the PlayerObject's sprite into the normal slime sprite if there is room.
	 * @param surfaces The list of Sprites the player can collide with which prevent room for growth.
	 * */
	//PRE: A list of all the surfaces Sprite can collide with
	//POST: The player is returned to its base size
	public void returnToBaseSize(ArrayList<Sprite> surfaces) {
		
		if(this.getMass() == 0.5)
		for(Sprite other : surfaces)
			if(normalSlimeHitBox.overlaps(other.getBoundary()) && other.isSolid()) {
				if(other == this) continue;
				return;
			}
		
		this.setPosition(this.getBoundaryX() + this.getBoundaryWidth()/2 - 112.5, this.getBoundaryY() + this.getBoundaryHeight() - 169);
		this.setImage("Pictures/Slime.png");
		this.setMass(1);
	}//end returnToBaseSize
	
	

	public boolean isBouncing() {
		return bounces > 0;
	}
	
	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}
	
}//end PlayerObject