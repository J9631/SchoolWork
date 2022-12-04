package application;
	
import javafx.application.Application;
import javafx.stage.Stage;


/* CHANGE LOG:
 * - Broke all functionality into objects between GameFrame and Level (11/09/22) by Jacob Sanchez
 * - Added Victory and Death screens with their respective action buttons. (11/01/22) by Jeremiah Johnson
 * - Added timer by (11/4/2022) Ryan Weems
 * - Added both health bars to the slime character and called the hit function to show damage (10/19/2022) by Jeremiah Johnson  
 * - Added "Block" and "Building" to test jump functionality of slime PhysicsObject (10/12/2022) by Jeremiah Johnson
 * - Added change log (10/12/2022) by Jacob R. S.
 * - Before 10/12/2022 created and updated by Jacob R. S, Ryan Weems, Jeremiah Johnson, Tony Ramos
 * */

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		GameFrame game = new GameFrame(primaryStage);
		game.initialize();
	}
	
	public static void launchProgram(String[] args) {
		launch(args);
	}

}
