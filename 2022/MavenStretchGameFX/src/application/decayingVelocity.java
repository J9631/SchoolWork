package application;

/** An instance of vector representing a velocity that decreases over time */
public class decayingVelocity extends Vector {

	private double decayRate;
	
	public decayingVelocity(double x, double y, double decayRate) {
		super(x, y);
		this.decayRate = decayRate;
	}
	
	public decayingVelocity(Vector velocity, double decayRate) {
		super(velocity);
		this.decayRate = decayRate;
	}
	
	public void decay() {
		//0 is lowest possible velocity decayingVelocity should be decreased to
		if(this.getX() == 0 && this.getY() == 0)
			return;
		
		//Get signs of x and y velocity to preserve their directions.
		double decayedVelocityX = Math.signum(this.getX());
		double decayedVelocityY = Math.signum(this.getY());
		
		//Decreases absolute value of x velocity
		if( (Math.abs(this.getX()) - decayRate) < 0)
			decayedVelocityX = 0;
		else
			decayedVelocityX *= (Math.abs(this.getX()) - decayRate);
		
		//Decreases absolute value of y velocity
		if( (Math.abs(this.getY()) - decayRate) < 0)
			decayedVelocityY = 0;
		else
			decayedVelocityY *= (Math.abs(this.getY()) - decayRate);

		this.set(decayedVelocityX, decayedVelocityY);
	}

	//Auto generated getters and setters
	public double getDecayRate() {
		return decayRate;
	}

	public void setDecayRate(double decayRate) {
		this.decayRate = decayRate;
	}

}
