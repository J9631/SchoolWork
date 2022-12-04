package application;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


/**The following class creates an object that functions to center a window's "camera"
 *on any given sprite.
 * */

public class ScrollCam {
	
	private Sprite target;
	
	//2 doubles are used to track the change in image size
	double oldImageWidth;
	double oldImageHeight;
	
	//3 vectors used to calculate the necessary translate for graphic context.
	private Vector lastPos = null;
	private Vector currPos = null;
	//DeltaPos is constructed with default 0,0 since it can be assumed there is no change in position until an update is called.
	private Vector deltaPos = new Vector(0, 0);
	
	//Constructs a camera centered on a Sprite target offset to be near the middle of a Canvas c
	public ScrollCam(Sprite target, Canvas c) {
		this.target = target;
		
		//Offsets that will be used for centering scrollcam in initial translation
		double offsetX = c.getWidth()/2;
		double offsetY = c.getHeight()/2;
		
		oldImageWidth = target.getImage().getWidth();
		oldImageHeight = target.getImage().getHeight();

		
		//Initial GraphicsContext Translate to center sprite
		c.getGraphicsContext2D().translate(-target.getPosition().getX() + offsetX - target.getImage().getWidth()/2, -target.getPosition().getY() + offsetY);
		
		this.currPos = new Vector(target.getPosition().getX(), target.getPosition().getY());
		this.lastPos = new Vector(currPos);
	}
	

	
	//Precondition: g is a viable GraphicsContext not null.
	//Postcondition: g is translated to the opposite direction of deltaPos.
	public void deltaTranslate(GraphicsContext g) {
		g.translate(-getDeltaX(), -getDeltaY());
	}
	
	//Precondition: g is a viable GraphicsContext. currPos and lastPos aren't null.
	//Postcondition: g is translated the opposite direction of the change of target's position.
	public void update(GraphicsContext g) {
		if(currPos == null) return;
		
		currPos.set(target.getPosition().getX(), target.getPosition().getY());
		deltaPos.set(currPos.getX() - lastPos.getX(), currPos.getY() - lastPos.getY());
		this.deltaTranslate(g);
		lastPos.set(currPos);
	}
	
	
	//returns x and y of Delta x directly, QOL methods not entirely necessary
	public double getDeltaX() {
		return deltaPos.getX();
	}
	public double getDeltaY() {
		return deltaPos.getY();
	}
	
	//Auto-generated Getters and Setters
	public Vector getLastPos() {
		return lastPos;
	}

	public void setLastPos(Vector lastPos) {
		this.lastPos.set(lastPos);
	}

	public Vector getCurrPos() {
		return currPos;
	}

	public void setCurrPos(Vector currPos) {
		this.currPos.set(currPos);
	}

	//readjust is a method used when the size of the image of target changes.
	//Precondition: A pointer to the graphics context to be adjusted.
	//Postcondition: The graphicscontext is translated to reecenter on the target.
	public void readjust(GraphicsContext g) {
		double deltaWidth = this.target.getImage().getWidth() - oldImageWidth;
		double deltaHeight = this.target.getImage().getHeight() - oldImageHeight;
		oldImageWidth = this.target.getImage().getWidth();
		oldImageHeight = this.target.getImage().getHeight();
		g.translate(-deltaWidth/2, -deltaHeight);
		
	}
}
