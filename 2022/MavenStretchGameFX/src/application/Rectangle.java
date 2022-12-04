package application;

/** A rectangle object that is used as a hit box, original code for constructor and overlaps obtained from Sprite based tutorial here: https://www.youtube.com/watch?v=Pkjdl5X0ylc
 *  rest of class has been originally made/modified from it.
 * */
public class Rectangle {
	private double x;
	private double y;
	private double width;
	private double height;
	
	/** Constructs a rectangle
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle
	 * */
	public Rectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}//end Rectangle constructor
	
	
	/** overlaps checks if two different rectangles overlap each other at any point
	 * @param other The rectangle object being compared with this rectangle for an overlap
	 * */
	public boolean overlaps(Rectangle other) {
		/*Checks that boundaries don't intersect
		 *with four cases:
		 *this rect. left of other rect
		 *this rect. right of other rect
		 *this rec. above other rect
		 *this recy. below other rect
		 **/
		boolean noOverlap =
				this.x + this.width < other.x ||
				other.x + other.width < this.x ||
				this.y + this.height < other.y||
				other.y + other.height < this.y;
		return !noOverlap;
	}//end overlaps
	
	/** bottomSupported checks whether this rectangle's bottom collides with another's top 
	 * @param other The rectangle whose top is being check against this rectangle's bottom for an overlap
	 * */
	//PRE: other is a non-null Rectangle
	//POST: returns whether "this" boundary is on top of another
	public boolean bottomSupported(Rectangle other) {
		boolean noSupport = this.x + this.width < other.x ||
							other.width + other.x < this.x ||
							this.y + this.height < other.y ||
							other.y < this.y + this.height - 60; //This line checks that top of "other" isn't above this rectangle's bottom with a range of 60 pixels.
		return !noSupport;
	}//end bottomSupported
	
	/** leftHit checks whether this rectangle's left side is being collided with by another rectangle.
	 * @param other The rectangle being checked against this rectangle's left side.
	 * */
	//PRE: A pointer to the rectangle being collided with.
	//POST: Returns boolean indicating if OTHER hit THIS from the left
	public boolean leftHit(Rectangle other) {
		boolean hHit =
				other.x + other.width >= this.x &&
				this.x + this.getWidth()/2 >= other.x + other.width &&
				other.x <= this.x;
				
		boolean vHit =
				(other.height + other.y <= this.y + this.height &&
				 this.y < other.height + other.y) ||
				(other.y >= this.y && this.y + this.height > other.y);
		
		boolean hit = hHit && vHit;
				
		return hit;
	}//end leftHit
	
	/** rightHit checks whether this rectangle's right side is being collided with by another rectangle.
	 * @param other The rectangle being checked against this rectangle's right side.
	 * */
	//PRE: A pointer to the rectangle being collided with.
	//POST: Returns boolean indicating if OTHER hit THIS from the right
	public boolean rightHit(Rectangle other) {
		
		//return false;
		
		boolean hHit =
				other.x <= this.x + this.width &&
				this.x + this.width/2 <= other.x &&
				other.x + other.width >= this.x + this.width;
				
		boolean vHit =
				(other.height + other.y <= this.y + this.height &&
				this.y < other.height + other.y) ||
				(other.y >= this.y && this.y + this.height > other.y);
		
		boolean hit = hHit && vHit;
		
		return hit;
	}//end rightHit
	
	
	/** below checks whether this rectangle is below another one.
	 * @param other The rectangle that is being checked if it is above this rectangle.
	 * */
	//PRE: A pointer to another rectangle.
	//POST: Indicates if THIS is below OTHER.
	public boolean below(Rectangle other) {
		boolean below = this.y + this.height <= other.y + other.height||
						this.x > other.x + other.width;
		return !below;
	}//end below
	
	
	/** toLeft checks whether a rectangle is to the left of this rectangle.
	 * @param other The rectangle that is being checked if it is to the left of this rectangle.
	 * */
	//Checks if the rectangle is to the left of another one.
	public boolean toLeft(Rectangle other) {
		return this.x < other.x;
	}//end toLeft
	
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	
	//generic getters and setters
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
}