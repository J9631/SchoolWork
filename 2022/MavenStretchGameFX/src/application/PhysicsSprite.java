package application;

import java.util.ArrayList;
import java.util.Iterator;

/** PhysicsSprite is an instance of a Sprite that has physical properties like mass and can be affected by other PhysicsSprites */
public class PhysicsSprite extends Sprite {

	private double mass; //The "mass" of the sprite, used to check what PhysicsSprites are capable of moving each other.
	private double momentum; //The "momentum" is derived from mass and is updated dynamically to reflect what can move each other like mass, but in a chain of collisions.
	private double gravity; //The gravity determines the rate at which a PhysicsSprite falls
	
	private boolean onSurface = false; //Boolean representing whether the sprite is on top of something or actively falling.
	
	private boolean justCollidedStatically; //A flag indicating if a PhysicsSprite is backed up against somewhere it can't be pushed past like an unmovable wall.
	
	/** Constructs a PhysicsSprite
	 * @param mass A double determining what the sprite can be moved by
	 * @param gravity The rate at which the sprite will fall 
	 * */
	public PhysicsSprite(double mass, double gravity) {
		super();
		this.mass = mass;
		this.momentum = mass;
		this.gravity = gravity;
		this.setSolid(true); //All PhysicsSprites are solid
	}
	
	/**Updates a PhysicsSprites attributes including its velocity, mass, and position.
	 * @param surfaces A list of ALL other Sprites the PhysicsSprite can interact with
	 * */
	//PRE: Surfaces is a non-empty ArrayList of valid Sprites this PhysicsSprite may land on.
	//Post: This PhysicsSprite's location is updated and checked against other sprites.
	public void update(ArrayList<Sprite> surfaces) {
		//Resets static collision and momentum so it won't continually act as if its being hit by something before checking.
		this.justCollidedStatically = false;
		this.setMomentum(this.getMass());
		
		
		//Updates the total velocity
		this.getTotalVelocity().set(this.getVelocity());
		
		//If a temporary velocity is present adds it into the total velocity.
		if(this.getTempVelocity() != null) {
			this.getTotalVelocity().add(this.getTempVelocity());
			this.getTempVelocity().decay();
		}
		
		this.collide(surfaces); //Checks for collisions
		
		this.addPosition(getTotalVelocity()); //Updates position
		
		this.fall(surfaces); //Updates velocity to reflect whether PhysicsSprite is falling or not
	}//end update
	
	/**Method updates sprites velocity based on whether it should be falling or not.
	 * @param surfaces A list of all the sprites PhysicsSprite may land on
	 * */
	//PRE: Surfaces is a non-empty ArrayList of non-null Sprites that PhysicsSprite may land on.
	//POST: This PhysicsSprite either falls at a rate consistent with its gravity or lands on a surface.
	public void fall(ArrayList<Sprite> surfaces) {	
		//pointer is used to check each Sprite from surfaces in conjunction with the it Iterator
		Iterator<Sprite> it = surfaces.iterator();
		Sprite pointer = null;
		
		//While it has Sprites checks this PhysicsObject against them.
		while(it.hasNext()) {
			pointer = it.next();
			

			
			//Checks if pointer has landed on a surface.
			//Y component of Velocity must be > 0 to indicate it is landing.
			if( this.onSurface(pointer) && this.getVelocityY() >= 0 ) {
				onSurface = true;
				break; //Breaks out of loop once sprite can land
			}//end if
			else {
				onSurface = false;
			}//end else
			
		}//end while
		
		//Checks onSurface if true and there is a valid pointer PhysicsSprite stops falling and 
		//has its yposition set to land on it.
		if(onSurface && pointer != null) {

			this.landOn(pointer);

		}//end if
		
		//Otherwise this object's velocity increases by gravity downwards.
		//MUST be outside while loop, as otherwise it would add for every object in it.
		else {
			this.addVelocity(0, gravity);
		}//end else
		
	}//end fall
	
	/** Sets the PhysicsSprites velocity and Y position so that it can "land on" a given Sprite 
	 * @param pointer The Sprite being "landed on"
	 * */
	//This method is necessary for polymorphism with the PlayerObject which "overrides" this one
	//PRE: pointer is a non-null pointer.
	//POST: This PhysicsSprite has its velocity and position aligned to land on the sprite pointer.
	public void landOn(Sprite pointer) {
		this.setVelocityY(0);
		this.setPositionY(pointer.getBoundaryY() - this.getBoundaryHeight());
	}//end landOn()
	
	
	/** Checks for collisions updating Sprites positions and velocities in the process.
	 * @param collidables A list of all the sprites PhysicsSprite can collide with.
	 * */
	public void collide(ArrayList<Sprite> collidables) {
		Iterator<Sprite> it = collidables.iterator();
		Sprite collidablePointer = null;
		
		double collisionScale = 0.5; //Value collisionVelocity is scaled down to
		
		Vector collisionVelocity = new Vector(0,0); //vector that will be added to decayingVelocity to represent the collision moving this
		
		//Loop iterates through all sprites from collidables seeing if this PhysicsSprite should be moved by any of them
		while(it.hasNext()) {
			
			collidablePointer = it.next();
			
			//Can't collide if it isn't solid so skips (platforms and background images weeded out here)
			if(!collidablePointer.isSolid()) continue;
			
			//Skips over anything that isn't overlapping the sprite or is itself
			if(!this.overlaps(collidablePointer) || this == collidablePointer) continue;
		
			//Handles collisions for non-physics based sprites that still move or are solid
			if( !(collidablePointer instanceof PhysicsSprite) ) {
				collisionVelocity.add(this.staticCollision(collidablePointer));
				continue;
			}
			
			//If loop gets this far the collidable must be an instance of PhysicsSprite
			PhysicsSprite physicsCollidable = (PhysicsSprite)collidablePointer;
			

			
			//Checks if this PhysicsSprite has been hit from below
			if(physicsCollidable.getBoundary().below(this.getBoundary())) {
				
				//This uses set instead of add so that infinite upwards velocity can't be gained from bumping something up then landing on it.
				if(physicsCollidable.getTotalVelocity().getY() < 0) {
					collisionVelocity.set(physicsCollidable.getTotalVelocity());
				}
				collisionVelocity.scale(collisionScale);
				
				//The conditional here helps ensures that a sprite won't be pushed below ground
				if( !(physicsCollidable.isOnSurface()) && (physicsCollidable.getBoundaryY() >= this.getBoundaryY() + this.getBoundaryHeight())) 
					physicsCollidable.setPosition(physicsCollidable.getBoundaryX(), this.getBoundaryY() + this.getBoundaryHeight() + 1);
				
				continue;
				
			}
			
			
			
			//Checks if this PhysicsSprite has been hit from the left.
			if(this.getBoundary().leftHit(physicsCollidable.getBoundary())) {			
				
				//If statement ensures objects can't be moved into non-movable objects like walls.
				//Effectively pauses motion if a series of objects are running another one into a wall.
				if(physicsCollidable.justCollidedStatically) {
					this.setPosition(physicsCollidable.getBoundaryX() + physicsCollidable.getBoundaryWidth() + 1, this.getBoundaryY());
					this.getTotalVelocity().setX(0);
					continue;
				}//end if
				
				physicsCollidable.setPosition(this.getBoundaryX() - physicsCollidable.getBoundaryWidth() - 1, physicsCollidable.getBoundaryY());
				
				//If physicsCollidable has a lesser momentum than this PhysicsSprite velocity won't be updated as physicsCollidable can't move this.
				if(physicsCollidable.getMomentum() < this.getMomentum()) continue;
				
				this.momentum = physicsCollidable.momentum; //Updates this PhysicsSprite's momentum to match the physicsCollidable
				
				//If an object is running into this from the left, adds velocity to this so it will be moved
				if(physicsCollidable.getTotalVelocity().getX() > 0) {
					collisionVelocity.add(physicsCollidable.getTotalVelocity());
				}
								
				collisionVelocity.scale(collisionScale); //Scales the velocity down, diminishing returns
				
				continue; 
			}//end if
			
			
			
			//Checks if this PhysicsSprite has been hit from the right.
			if(this.getBoundary().rightHit(physicsCollidable.getBoundary())) {
				
				//If statement ensures objects can't be moved into non-movable objects like walls.
				//Effectively pauses motion if a series of objects are running another one into a wall.
				if(physicsCollidable.justCollidedStatically) {
					this.setPosition(physicsCollidable.getBoundaryX() - this.getBoundaryWidth() - 1, this.getBoundaryY());
					this.getTotalVelocity().setX(0);
					continue;
				}//end if
				
				physicsCollidable.setPosition(this.getBoundaryX() + this.getBoundaryWidth() + 1, physicsCollidable.getBoundaryY());
				
				//If physicsCollidable has a lesser momentum than this PhysicsSprite velocity won't be updated as physicsCollidable can't move this.
				if(physicsCollidable.getMomentum() < this.getMomentum()) continue;
				
				this.momentum = physicsCollidable.momentum; //Updates this PhysicsSprite's momentum to match the physicsCollidable
				
				//If an object is running into this from the right, adds velocity to this so it will be moved
				if(physicsCollidable.getTotalVelocity().getX() < 0) {
					collisionVelocity.add(physicsCollidable.getTotalVelocity());
				}

				collisionVelocity.scale(collisionScale); //Scales the velocity down, diminishing returns
				
				continue; 
			}//end if
			

		}//end while
		
		this.addDecayingVelocity(collisionVelocity, 0.5);
		
		//Arbitrary speed control, necessary because collisions are factored every frame
		while(this.getTempVelocity().getMagnitude() > 20) this.getTempVelocity().scale(0.3);
	}//end collide
	
	
	
	/** staticCollision checks for collisins between non-physics based Sprites and PhysicsSprites
	 * @param collidable The non-physics based Sprite being collided with
	 * */
	//Handles collisions for non-physics based sprites that still move or are solid
	//IMPORTANT: this moves THIS object to the border of the SPRITE and is done with position not VELOCITY or DECAYING VELOCITY
	private Vector staticCollision(Sprite collidable) {
		Vector collisionVelocity = new Vector(0, 0);
		
		//Checks if collidable is being hit from the Right
		if(collidable.getBoundary().rightHit(this.getBoundary())) {
			//Ensures that velocity isn't added that pushes sprite into the collidable
			this.setPosition(collidable.getBoundaryX() + collidable.getBoundaryWidth() + 1, this.getBoundaryY());
			this.justCollidedStatically = true;
		}//end if
		
		//Checks if collidable is being hit from the Left
		else if(collidable.getBoundary().leftHit(this.getBoundary())) {
			//Ensures that velocity isn't added that pushes sprite into the collidable
			this.setPosition(collidable.getBoundaryX() - this.getBoundaryWidth() - 1, this.getBoundaryY());
			this.justCollidedStatically = true;
		}//end if
		
		//Checks if collidable is being hit from Below
		else if(this.getBoundary().below(collidable.getBoundary())) {
			this.setPosition(this.getBoundaryX(), collidable.getBoundaryY() + collidable.getBoundaryHeight());
			if(this.getVelocityY() < 0) this.setVelocityY(0);
			this.justCollidedStatically = true;
		}//end if
		
		return collisionVelocity;	
	}//end staticVollision
	
	//Generic getters and setters
	public boolean isOnSurface() {
		return onSurface;
	}
	
	public double getGravity() {
		return gravity;
	}

	public void setGravity(double gravity) {
		this.gravity = gravity;
	}

	public void setOnSurface(boolean onSurface) {
		this.onSurface = onSurface;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getMomentum() {
		return momentum;
	}

	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	public boolean isJustCollidedStatically() {
		return justCollidedStatically;
	}

	public void setJustCollidedStatically(boolean justCollidedStatically) {
		this.justCollidedStatically = justCollidedStatically;
	}
	
}//end PhysicsSprite
