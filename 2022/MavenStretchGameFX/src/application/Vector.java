package application;

/** An object that holds a coordinate value code for constructors and add methods originally taken from the 
 *  Sprite Based Tutorial here: https://www.youtube.com/watch?v=Pkjdl5X0ylc, all other code has been originally created*/
public class Vector {
	private double x;
	private double y;
	
	//Vector constructor
	public Vector(double x, double y) {
		this.set(x, y);
		//this class stores vector data for us
	}
	
	public Vector(Vector other) {
		this.set(other.getX(), other.getY());
	}
	
	//Sets x and y individually
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(Vector vector2) {
		this.x = vector2.x;
		this.y = vector2.y;
	}
	
	//Adds values to current x and y
	public void add(double x, double y) {
		this.x += x;
		this.y += y;
	}
	
	//Adds 2 vectors components
	public void add(Vector vector2) {
		this.x += vector2.x;
		this.y += vector2.y;
	}
	
	public void addY(double y) {
		this.y += y;
	}
	
	public boolean equals(Vector vector2) {
		return (this.x == vector2.getX() && this.y == vector2.getY());
	}
	
	public boolean equals(double x, double y) {
		return (this.x == x && this.y == y);
	}
	
	public double getMagnitude() {
		return Math.sqrt(x*x + y*y);
	}
	
	//Multiplies the vector's components by a factor
	public void scale(double factor) {
		this.set(this.getX() * factor, this.getY() * factor);
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

}
