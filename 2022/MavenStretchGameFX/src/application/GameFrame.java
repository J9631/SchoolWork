package application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * @author Jacob R. Sanchez
 * 
 *GameFrame is a class that sets up the various scenes that can be switched too alongside tracking volume and the current level
 */
public class GameFrame implements LevelObserver{
	
	private final Stage primaryStage;

	private Level activeLevel; //The current level object being loaded.
	private int currentLevelNum; //The current level the game is on represented as a number in order they come in.
	private final int FINAL_LEVEL = 2; //The number of the final level, where the game ends.
	private Font standardHeaderFont = new Font("Goudy Stout", 40);
	private Font standardFont = Font.font("Arial", FontWeight.BOLD, 20);
	
	//Variables for screen resolution
	private int screenWidth = 1377;
	private int screenHeight = 768;
	
	private double gameVolume = 50;
	
	//Coordinate variables used to re-center the window on where it last was
	private double centerX;
	private double centerY;
	
	/** Constructs the GameFrame object from a given stage whose scenes will be changed*/
	public GameFrame(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	/** Initializes the primaryStage by setting its title, icon, and then setting its scene to the game menu */
	public void initialize() {
		primaryStage.setTitle("THE STRETCH GAME");
		Image windowIcon = new Image("Pictures/Slime.png");
		primaryStage.getIcons().add(windowIcon);
		this.createMenuScene();
		primaryStage.setScene(this.createMenuScene());
		primaryStage.show();
		primaryStage.centerOnScreen();
		primaryStage.setResizable(false);
		centerX = primaryStage.getX() + primaryStage.getWidth()/2;
		centerY = primaryStage.getY() + primaryStage.getHeight()/2;
		
		//Action listener pauses a game if it is minimized during a level, or resumes it if unminimized.
		primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				
				//Pauses the game if on an active level and minimized
	            if(GameFrame.this.activeLevel != null && !newValue && GameFrame.this.primaryStage.getScene() == GameFrame.this.activeLevel.getLevelScene()) {
	                GameFrame.this.activeLevel.pauseGame();
	            }//end if
	            
	            //Resumes the game if on an active level and unminimized.
	            else if(GameFrame.this.activeLevel != null && newValue && GameFrame.this.primaryStage.getScene() == GameFrame.this.activeLevel.getLevelScene()) {
	            	GameFrame.this.activeLevel.startGame();
	            }//end else if
	            
	        }//end changed
			
		});
		
	}
	
	//Centers a window on where it last was
	private void realignCenter() {
		centerX = primaryStage.getX() + primaryStage.getWidth()/2;
		centerY = primaryStage.getY() + primaryStage.getHeight()/2;
		primaryStage.setX(centerX - primaryStage.getWidth()/2);
		primaryStage.setY(centerY - primaryStage.getHeight()/2);
	}//end realignCenter
	
	//Creates a new menu with updated dimensions.
	private Scene createMenuScene() {
		
		StackPane layout = new StackPane();
		Scene menu = new Scene(layout, screenWidth, screenHeight);
	
		layout.getChildren().add(this.drawBackgroundImage()); //Creates and add the underlying background
		
		VBox organize = new VBox(15); //A VBox wrapper used to format the buttons and logo
		
		//title is set to the logo graphic
		Label title = new Label();
		Image logo = new Image("Pictures/TitleLogo.png");
		ImageView viewLogo = new ImageView(logo);
		title.setGraphic(viewLogo);
	
		//quit is a button that will exit the program
		Button quit = new Button();
		Image quitGraphic = new Image("Pictures/quit.png");
		ImageView viewQuitGraphic = new ImageView(quitGraphic);
		quit.setGraphic(viewQuitGraphic);
		quit.setOnAction(e -> {
			System.exit(0);
		});
	
		quit.addEventFilter(KeyEvent.ANY, Event::consume);
	
		//resume is a button that will continue any game that is actively in progress
		Button resume = new Button();
		Image resumeGraphic = new Image("Pictures/continuegame.png");
		ImageView viewResumeGraphic = new ImageView(resumeGraphic);
		resume.setGraphic(viewResumeGraphic);
		resume.setOnAction(e -> {
			//Checks if there is any level being played
			if(activeLevel != null) {
				primaryStage.setScene(activeLevel.getLevelScene());
				this.realignCenter();
				activeLevel.startGame();
				activeLevel.updateVolume(gameVolume); //updates volume
			}//end if
		});
		
		resume.addEventFilter(KeyEvent.ANY, Event::consume);
		
		//startGame is a button that will load level 1
		Button startGame = new Button();
		Image startGameGraphic = new Image("Pictures/startgame.png");
		ImageView viewStartGraphic = new ImageView(startGameGraphic);
		startGame.setGraphic(viewStartGraphic);
		startGame.setOnAction(e -> {
			currentLevelNum = 1;
			this.switchLevelInstance(currentLevelNum);
		});
		
		startGame.addEventFilter(KeyEvent.ANY, Event::consume);
	
		//sett is a button that loads the settings menu.
		Button sett = new Button();
		Image settGraphic = new Image("Pictures/settings.png");
		ImageView viewSettGraphic = new ImageView(settGraphic);
		sett.setGraphic(viewSettGraphic);
		sett.setOnAction(e -> {
			primaryStage.setScene(createSettingsScene());
			this.realignCenter();
		});
		
		sett.addEventFilter(KeyEvent.ANY, Event::consume);
		
		//Adds all buttons and logo to the VBox organize to center it them on the window
		organize.getChildren().addAll(title, startGame, resume, sett, quit);
		organize.setAlignment(Pos.CENTER);
		
		layout.getChildren().add(organize);
		
		return menu;
	}//end createMenuScene
	
	
	//Creates a new settings menu with updated dimensions.
	private Scene createSettingsScene() {
		StackPane layout = new StackPane();
		
		layout.getChildren().add(this.drawBackgroundImage());
		
		Scene settings = new Scene(layout, screenWidth, screenHeight);
		
		
		Label settingsTitle = new Label("Settings");
		settingsTitle.setFont(standardHeaderFont);
		
		ObservableList<String> resolutions = FXCollections.observableArrayList("1280 x 1024", "1366 x 768", "1600 x 900", "1920 x 1080"); //This is a list of all the items in the resolutionSelection drop down menu
		ComboBox<String> resolutionSelection = new ComboBox<String>(resolutions); //This is a drop down menu for the possible resolution selections
		
		Popup pop = new Popup(); //Pop is a pop up window that will ask for the confirmation of a resolution change
		
		//confirmationQuery displays a prompt asking if the selected resolution should be applied
		Label confirmationQuery = new Label();
		confirmationQuery.setFont(standardFont);
		
		//Aligned boxes for pop's buttons and labels
		VBox popupLayout = new VBox(10);
		HBox popupButtons = new HBox(50);
		
		//popupConfirm is a button that will apply a screen size change
		Button popupConfirm = new Button();
		popupConfirm.addEventFilter(KeyEvent.ANY, Event::consume);
		popupConfirm.setText("Confirm");
		
		popupConfirm.setOnAction(e -> {
			centerX = primaryStage.getX() + primaryStage.getWidth()/2;
			centerY = primaryStage.getY() + primaryStage.getHeight()/2;
			
			//Changes the stage size to whatever the selected resolution was
			switch(resolutionSelection.getValue()) {

				case "1280 x 1024":
								   primaryStage.setWidth(1280);
								   primaryStage.setHeight(1024);
								   screenWidth = 1280;
								   screenHeight = 1024;
								   break;
				
				case "1366 x 768": 
								  primaryStage.setWidth(1366);
								  primaryStage.setHeight(768);
								  screenWidth = 1366;
								  screenHeight = 768;
								  break;
				
				case "1600 x 900":
								  primaryStage.setWidth(1600);
								  primaryStage.setHeight(900);
								  screenWidth = 1600;
								  screenHeight = 900;
								  break;
								  
				case "1920 x 1080":
								   primaryStage.setWidth(1920);
								   primaryStage.setHeight(1080);
								   screenWidth = 1920;
								   screenHeight = 1080;
								   break;
			}//end switch
			
			pop.hide(); //"Gets rid of" the popup upon confirmation
			
			//The settings menu is recreated with the selected screen size
			primaryStage.setScene(this.createSettingsScene());
			primaryStage.setX(centerX - primaryStage.getWidth()/2);
			primaryStage.setY(centerY - primaryStage.getHeight()/2);
		});
		
		//popupCancel is a button that "closes" pop by hiding it.
		Button popupCancel = new Button();
		popupCancel.setText("Cancel");
		popupCancel.setOnAction(e -> {
			pop.hide();
		});
		popupCancel.addEventFilter(KeyEvent.ANY, Event::consume);
		
		//popupWindow serves as a grey rectangular background for pop
		javafx.scene.shape.Rectangle popupWindow = new javafx.scene.shape.Rectangle();
		popupWindow.setWidth(640);
		popupWindow.setHeight(100);
		popupWindow.setArcHeight(10);
		popupWindow.setArcWidth(10);
		popupWindow.setFill(new Color(0.8, 0.8, 0.8, 0.5));
		popupWindow.setVisible(true);
		
		//windowWrapper serves a box to align popupWindow with
		VBox windowWrapper = new VBox();
		windowWrapper.getChildren().addAll(popupWindow);
		
		popupButtons.getChildren().addAll(popupConfirm, popupCancel);
		popupLayout.getChildren().addAll(confirmationQuery, popupButtons);
		
		//Aligns all elements in pop to center positions
		windowWrapper.setAlignment(Pos.CENTER);
		popupButtons.setAlignment(Pos.CENTER);
		popupLayout.setAlignment(Pos.CENTER);
		pop.getContent().addAll(windowWrapper, popupLayout);
		
		pop.setWidth(640);
		pop.setHeight(100);
		
		pop.setAnchorX(primaryStage.getX() + primaryStage.getWidth()/2 - pop.getWidth()/2);
		pop.setAnchorY(primaryStage.getY() + primaryStage.getHeight()/2 - pop.getHeight()/2);
		
		//Changes confirmationQuery to relevant prompt for screen size, then displays pop so user can confirm their selection.
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				confirmationQuery.setText("\n  Are you sure you want to change the resolution to "+resolutionSelection.getValue() + "?");
				pop.setX(primaryStage.getX() + Math.abs(primaryStage.getWidth()/2) - 300);
				pop.setY(primaryStage.getY() + Math.abs(primaryStage.getHeight()/2) - 40);
				popupWindow.setX(pop.getX());
				popupWindow.setY(pop.getY());
				pop.show(primaryStage);
			}//end handle
			
		};
		
		//goBack is a button that changes the scene to the main menu.
		Button goBack = new Button();
		Image returnGraphic = new Image("Pictures/goback.png");
		ImageView viewReturnGraphic = new ImageView(returnGraphic);
		goBack.setGraphic(viewReturnGraphic);
		goBack.setOnAction(e -> {
			primaryStage.setScene(this.createMenuScene());
			this.realignCenter();
			 
			pop.hide();
		});
		
		goBack.addEventFilter(KeyEvent.ANY, Event::consume);
		
		//soundVolume is a slider that controls the games overall volume
		Slider soundVolume = new Slider();
		soundVolume.setMaxWidth(200);
		soundVolume.setMax(100);
		soundVolume.setValue(this.gameVolume);
		soundVolume.setMin(0);
		
		//volumeValue displays the value soundVolume is at as a rounded int
		Label volumeValue = new Label();
		volumeValue.setFont(standardFont);
		int roundedVolume = (int) gameVolume;
		volumeValue.setText(""+roundedVolume);
		
		//Adds listener to soundVolume that updates gameVolume whenever slider is interacted with
		soundVolume.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				gameVolume = (double) newValue;
				int roundedVolume = (int) gameVolume;
				volumeValue.setText("" + roundedVolume);
			}//end changed

		});
		
		//settingsOptions is a box to format all of settings into a vertical display
		VBox settingsOptions = new VBox(20);
		settingsOptions.setAlignment(Pos.CENTER);
		
		//resolutionWrapper is a box that formats the screen size selection box with a label horizontally
		HBox resolutionWrapper = new HBox(20);
		Label screenSizeLabel = new Label();
		screenSizeLabel.setText("Screen Size: ");
		screenSizeLabel.setFont(standardFont);
		resolutionWrapper.getChildren().addAll(screenSizeLabel, resolutionSelection);
		resolutionWrapper.setAlignment(Pos.CENTER);
		
		//soundSettingsWrapper is a box that formats the sound slider with a label for what it is and its value horizontally
		HBox soundSettingsWrapper = new HBox(20);
		Label soundLabel = new Label();
		soundLabel.setText("Sound Volume: ");
		soundLabel.setFont(standardFont);
		soundSettingsWrapper.getChildren().addAll(soundLabel, soundVolume, volumeValue);
		soundSettingsWrapper.setAlignment(Pos.CENTER);
		
		settingsOptions.getChildren().addAll(settingsTitle, resolutionWrapper, soundSettingsWrapper, goBack);
		
		resolutionSelection.setOnAction(event);
		
		layout.getChildren().addAll(settingsOptions);
		
		return settings;
	}//end createSettingsScene
	
	//Creates a death scene for the player with updated dimensions.
	private Scene createDeathScene() {				
		StackPane layout = new StackPane();
		layout.getChildren().add(this.drawBackgroundImage());
		Scene death = new Scene(layout, screenWidth, screenHeight);
				
		Label DeathTitle = new Label("YOU LOST!!!");
		DeathTitle.setFont(standardHeaderFont);
				
		//Vertical box is created so buttons can be aligned to center
		VBox deathRestart = new VBox(10);
				
		//deathStartOver is a button that restarts the current level
		Button startOver = new Button();
		Image startOverGraphic = new Image("Pictures/restart.png");
		ImageView viewStartOverGraphic = new ImageView(startOverGraphic);
		startOver.setGraphic(viewStartOverGraphic);
		startOver.setOnAction(e -> {
			this.switchLevelInstance(currentLevelNum);
		});
		startOver.addEventFilter(KeyEvent.ANY, Event::consume);
		
		//deathReturnMe is a button that returns to the main menu
		Button deathReturnMe = this.goBackButton();
				
		//aligns "deathRestart" to the center and adds deathReturnMe deathStartOver and DeathTitle.
		deathRestart.setAlignment(Pos.CENTER);
		deathRestart.getChildren().addAll(DeathTitle, deathReturnMe, startOver);
		
		//adds deathRestart to the Death screen layout "layout4"
		layout.getChildren().addAll(deathRestart);
		
		return death;
		
	}//end createDeathScene
	
	//Creates a new victory scene for the player with updated dimensions, time, and score.
	private Scene createVictoryScene(double score, double time) {							
		StackPane layout = new StackPane();
		layout.getChildren().add(this.drawBackgroundImage());
		Scene victory = new Scene(layout, screenWidth, screenHeight);
				
		Label victoryTitle = new Label("YOU WON!!!");
		victoryTitle.setFont(standardHeaderFont);
		
		Label displayScore = new Label();
		displayScore.setText("Level Score: " + score);
		displayScore.setFont(standardFont);
		
		Label displayTime = new Label();
		displayTime.setText("Level Time: " + time);
		displayTime.setFont(standardFont);
				
		//restart is a box that formats the buttons and labels vertically
		VBox restart = new VBox(10);
		
		//victoryReturn is a button that returns to the main menu
		Button victoryReturn = this.goBackButton();
				
		//startOver is a button that restarts game
		Button startOver = new Button();
		Image startOverGraphic = new Image("Pictures/restart.png");
		ImageView viewStartOverGraphic = new ImageView(startOverGraphic);
		startOver.setGraphic(viewStartOverGraphic);
		startOver.setOnAction(e -> {
			this.switchLevelInstance(currentLevelNum);
		});
		startOver.addEventFilter(KeyEvent.ANY, Event::consume);
				
		//aligns "restart" to the center and adds returnMe startOver and VictoryTitle.
		restart.setAlignment(Pos.CENTER);
		
		restart.getChildren().addAll(victoryTitle, displayScore, displayTime);
		
		//Checks if the current level is the final one, if it isn't a button to move to the next level is added to the scene
		if(currentLevelNum < FINAL_LEVEL) {
			//startNextLevel is a button that starts the next level
			Button startNextLevel = new Button();
			Image nextGraphic = new Image("Pictures/nextlevel.png");
			ImageView viewNextGraphic = new ImageView(nextGraphic);
			startNextLevel.setGraphic(viewNextGraphic);
			
			startNextLevel.setOnAction(e -> {
				currentLevelNum++;
				this.switchLevelInstance(currentLevelNum);
			});
			startNextLevel.addEventFilter(KeyEvent.ANY, Event::consume);
			restart.getChildren().add(startNextLevel);
		}//end if
		
		restart.getChildren().addAll(victoryReturn, startOver);
		layout.getChildren().addAll(restart);
		
		return victory;
	}//end createVictoryScene
	
	//"Switches" the scene to a level by creating the level corresponding to currentLevel then setting primaryStage to its level scene
	private void switchLevelInstance(int currentLevel) {
		Level levelInstance = new Level(screenWidth, screenHeight, currentLevel, gameVolume);
		levelInstance.subscribeObserver(this);
		
		activeLevel = levelInstance; //maintains reference to the level created for the purposes of the continue game option in the main menu
		
		primaryStage.setScene(levelInstance.getLevelScene());
		this.realignCenter();
	}//end switchLevelInstance
	
	//Helper method that creates and returns a button that returns to the main menu
	private Button goBackButton() {
		Button goBack = new Button();
		Image backGraphic = new Image("Pictures/goback.png");
		ImageView viewBackGraphic = new ImageView(backGraphic);
		goBack.setGraphic(viewBackGraphic);
		
		goBack.setOnAction(e -> {
			primaryStage.setScene(this.createMenuScene());
			this.realignCenter();
		});
		
		//Buttons consume any key inputs; only activate by clicking
		goBack.addEventFilter(KeyEvent.ANY, Event::consume);
		
		return goBack;
	}//end goBackButton
	
	//Helper method that creates a canvas for a scene where a background is drawn onto
	private Canvas drawBackgroundImage() {
		Canvas menuBackground = new Canvas(2000, 1344);
		GraphicsContext graphics = menuBackground.getGraphicsContext2D();
		Image backgroundImage = new Image("Pictures/MenuBackground.jpg");
		graphics.drawImage(backgroundImage, 1000 - backgroundImage.getWidth()/2, 672 - backgroundImage.getHeight()/2);
		return menuBackground;
	}//end drawBackgroundImage



	/**update switches the primaryStage scene depending on a control flag set from a Level.
	 * @param switchCase The integer flag used to decide what scene must be switched to.
	 * @param score The player score obtained in the Level that called this method.
	 * @param time The time remaining in the Level that called this method.
	 * @throws IllegalArgumentException If a switch case not in 1-3 is used.
	 * */
	@Override
	public void update(int switchCase, double score, double time) {
		
		//Decides what scene should be switched to from flag set by a level
		switch(switchCase) {
		
		case 0: primaryStage.setScene(this.createMenuScene());
				this.realignCenter();
				 
				break;
				
		case 1: primaryStage.setScene(this.createDeathScene());
				this.realignCenter();
				 
				activeLevel = null;
				break;
				
		case 2: primaryStage.setScene(this.createVictoryScene(score, time));
				this.realignCenter();
				 
				activeLevel = null;
				break;
				
		default: throw new IllegalArgumentException("The number " + switchCase + " IS NOT a valid notify gameGrame can switch scene based on");
	
		}//end switch
		
	}//end update

}//end GameFrame
