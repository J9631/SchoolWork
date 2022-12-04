package application;
import javafx.scene.image.Image;

//import org.dyn4j.dynamics.Body;

/*CHANGE LOG:
*-added onSurface() (10/10/2022) by Jacob R. S.
*-added new setVelocity(double x, double y) (10/10/2022) by Jacob R. S.
*-added setVelocityY(), getBoundaryY(), setPositionY(), getVelocityY() (and corresponding X functions) (10/11/2022) by Ryan Weems 
*-added getBoundaryHeight() and getBoundaryWidth() (10/11/2022) by Jacob R. S.
* */
import javafx.scene.canvas.GraphicsContext;

/** A generic Sprite object whose code base of the constructor, setImage, update, getBoundaryX, getBoundaryY, getBoundaryWidth, getBoundaryHeight, and render
 *  were originally taken from the Sprite Based Tutorial here: https://www.youtube.com/watch?v=Pkjdl5X0ylc
 *  
 *  All other methods were originally created
 *  */
//This is a generic Sprite object which can be used for all of our game's entities as of now
public class Sprite {
	private Vector position;
	
	private Vector totalVelocity;//Sum of velocity and tempVelocity
	private Vector velocity;	//Velocity that is a result of the sprites own movement.
	private decayingVelocity tempVelocity;//Velocity as a reult of another action taken on the sprite, used so force isn't permanently applied.
	private boolean solid; //Whether the sprite can be walked through or not (only relevant if not a PhysicsSprite)
	
	private Image image;
	private Rectangle boundary;
	
	//Default constructor sets everything to 0
	public Sprite() {
		position = new Vector(0,0);
		
		velocity = new Vector(0,0);
		totalVelocity = new Vector(0,0);
		
		boundary = new Rectangle(0,0,0,0);
	}

	//setImage to a file from a file path
	public void setImage(String filename) {
		image = new Image(filename);
		boundary.setWidth(image.getWidth());
		boundary.setHeight(image.getHeight());
	}
	
	public void addDecayingVelocity(double x, double y, double decayRate) {
		if(tempVelocity == null) {
			tempVelocity = new decayingVelocity(x, y, decayRate);
			return;
		}
		tempVelocity.add(x, y);
		tempVelocity.setDecayRate(decayRate);
	}
	
	public void addDecayingVelocity(Vector velocity, double decayRate) {
		if(tempVelocity == null) {
			tempVelocity = new decayingVelocity(velocity, decayRate);
			return;
		}
		tempVelocity.add(velocity);
		tempVelocity.setDecayRate(decayRate);
	}
	
	public void update() {
		totalVelocity.set(velocity);
		if(tempVelocity != null) {
			totalVelocity.add(tempVelocity);
			tempVelocity.decay();
		}
		this.addPosition(totalVelocity);
	}


	//returns boundary with updated position (essentially our hitbox)
	public Rectangle getBoundary() {
		boundary.setX(position.getX());
		boundary.setY(position.getY());
		return boundary;
	}
	
	// gets the boundary of Y
	public double getBoundaryY() {
		boundary.setY(position.getY());
		return boundary.getY();
	}
	
	// gets the boundary of X
	public double getBoundaryX() {
		boundary.setX(position.getX());
		return boundary.getX();
	}
	
	// directly returns the width of boundary
	public double getBoundaryWidth() {
		return boundary.getWidth();
	}
	
	// directly returns the height of boundary
	public double getBoundaryHeight() {
		return boundary.getHeight();
	}
	
	//Checks if boundary overlaps another sprites
	public boolean overlaps(Sprite other) {
		return this.getBoundary().overlaps( other.getBoundary() );
	}
	
	//Checks if the sprite is directly on top of another sprite (Also updates boundary)
	public boolean onSurface(Sprite other) {
		return this.getBoundary().bottomSupported( other.getBoundary() );
	}
	
	public boolean isSolid() {
		return solid;
	}
	
	public void setSolid(boolean solid) {
		this.solid = solid;
	}
	
	//draws sprite at current position
	public void render(GraphicsContext context) {
		context.drawImage(image, position.getX(), position.getY());
	}
	
	//ADD: SETTER
	public void setVelocity(double x, double y) {
		velocity.set(x, y);
	}
	
	// Setter for Y
	public void setVelocityY(double y) {
		velocity.setY(y);
	}
	
	// Setter for X
	public void setVelocityX(double x) {
		velocity.setX(x);
	}
	
	//Add methods for Velocity
	public void addVelocity(double x, double y) {
		velocity.add(x, y);
	}
	public void addVelocity(Vector other) {
		velocity.add(other);
	}
	//Adds a vector to position
	public void addPosition(double x, double y) {
		position.add(x, y);
	}
	public void addPosition(Vector other) {
		position.add(other);
	}
	
	
//generic getters and setters
	public Vector getTotalVelocity() {
		return totalVelocity;
	}

	public void setTotalVelocity(Vector totalVelocity) {
		this.totalVelocity = totalVelocity;
	}

	public decayingVelocity getTempVelocity() {
		return tempVelocity;
	}

	public void setTempVelocity(decayingVelocity tempVelocity) {
		this.tempVelocity = tempVelocity;
	}
	public Vector getPosition() {
		return position;
	}
	public void setPosition(Vector position) {
		this.position = position;
	}
	// sets position for Y
	public void setPositionY(double position) {
		this.position.setY(position);
	}
	// sets position for X
	public void setPositionX(double position) {
		this.position.setX(position);
	}
	// gets velocity
	public Vector getVelocity() {
		return velocity;
	}
	// gets the Velocity of Y
	public double getVelocityY() {
		return velocity.getY();
	}
	// gets the Velocity of X
	public double getVelocityX() {
		return velocity.getX();
	}
	// sets the Velocity
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}
	// gets the Image
	public Image getImage() {
		return image;
	}
	
	// sets the Image
	public void setImage(Image image) {
		this.image = image;
	}

	// sets the boundary to a Rectangle object
	public void setBoundary(Rectangle boundary) {
		this.boundary = boundary;
	}
	// sets the position with 2 doubles
	public void setPosition(double x, double y) {
		position.set(x,y);
	}

}