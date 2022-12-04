package application;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**Level is an object that represents an instance of a level which changes its properties to match what number of level a player is on*/

//This is an object that is represents an instance of a level and changes its properties
//to match the specific level the player should be on
//This class contains mostly helper methods to construct the level, which have been separated as such
//to make the process of creating a level as modular as possible.
public class Level implements LevelSubject{
	
	//A list used to track all of this objects observers
	private ArrayList<LevelObserver> observers = new ArrayList<LevelObserver>();
	
	//5 Different array lists are used to track 5 unique cases of sprite interactions:
	private ArrayList<Sprite> surfaces = new ArrayList<Sprite>();		//Sprites that can directly interact and collide (e.g walls, crates, enemies).
	private ArrayList<Sprite> nonsurfaces = new ArrayList<Sprite>();	//Sprites that have no interactions. (e.g tutorial messages, background scenery)
	private ArrayList<Sprite> hazards = new ArrayList<Sprite>();		//Sprites which damage the player, but aren't necessarily enemies. (environmental hazards like spikes)
	private ArrayList<Sprite> collectibles = new ArrayList<Sprite>();	//Sprites whose only interaction is to be picked up. (Items the player collects like gems)
	
	//A list that tracks all the keys pressed on this screen, enables multiple inputs at same time
	private ArrayList<String> inputs = new ArrayList<String>();
	
	private Image background; //The background image of a level, MUST be global to be used in gameLoop scope
	
	private MediaPlayer backgroundMusicPlayer; //The player for the level background music, MUST be global for volume adjustments
	
	private AudioClip gemCollected; //Audio clip played on gem collection, MUST be global for volume adjustments and gameLoop socpe
	private AudioClip levelBeaten;  //Audio clip played on level completion, MUST be global for volume adjustments and gameLoop socpe
	
	private PlayerObject player; //Maintains a reference to the player, used for health tracking in the health bar.
	private Sprite finish;		 //Maintains a reference to the level goal post (point of completion), MUST be global to check for in gameLoop
	
	private Rectangle healthBar; //The health bar, global because it MUST be updated inside the gameLoop
	private Timer timer;		 //The level countdown timer, MUST be global since it must be updated, paused, and resumed in several method scopes
	private Text timerText;		 //The text displayed for the timer tracker, MUST be global to be accessed in the timer run()
	
	private AnimationTimer gameLoop; //The game loop itself, MUST be global so that it can be paused & resumed in several methods
	
	private int levelTime; //The remaining time in any given level, MUST be global so it can be repeatedly referred to for score.
	private double levelScore; //The score, MUST be global so it can be updated inside gameLoop.
	
	private StackPane layout = new StackPane(); //Contains all the assets for the level, global so helper methods can work with it for convenience.
	private Scene levelScene;
	
	/**Constructs a new Level
	 * @param screenWidth The width of the Level.
	 * @param screenHeight The height of the Level.
	 * @param levelNum The Level that is being made.
	 * @param levelVolume The volume of all sound effects on the Level.
	 * */
	//Constructor creates level 1-2, with matching screen dimensions and volume.
	public Level(int screenWidth, int screenHeight, int levelNum, double levelVolume) {
		
		background = new Image("Pictures/BigWallBackground.jpg");
		
		//Switch changes what level entities are created and the levelTime (aspects unique to each level)
		switch(levelNum){
			case 1: this.createLevel1Entities();
					this.levelTime = 300;
					break;
					
			case 2: this.createLevel2Entities();
					this.levelTime = 360;
					break;
			
			//throws an exception if a number is entered that no case exists for
			default: throw new IllegalArgumentException("The level " + levelNum + " doesn't exist!");
		}//end switch
		
		//Resets level score to 0
		levelScore = 0;
		
		//creates rest of level aspects which exist in every level
		this.generateLevelAssets();
		
		//The levelScene is created with the elements that are added into layout from generateLevelAssets()
		levelScene = new Scene(layout, screenWidth, screenHeight);
		
		//The backgroundMusic is loaded into a media object here, stack traced if not found
		Media gameTrack = null;
		try {
			gameTrack = new Media(getClass().getResource("/Music/scifi.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} 
		
		backgroundMusicPlayer = new MediaPlayer(gameTrack);
		
		try {
			gemCollected = new AudioClip(getClass().getResource("/Music/snd_fragment_retrievewav-14728.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		try {
			levelBeaten = new AudioClip(getClass().getResource("/Music/teleport-14639.mp3").toURI().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		this.updateVolume(levelVolume); //The volume is adjusted to what it was set to in settings
		
		//Adds listeners to the levelScene for user input
		levelScene.setOnKeyPressed( (KeyEvent event) -> {
			String KeyName = event.getCode().toString();
			if( !inputs.contains(KeyName) )
				inputs.add(KeyName);
		});
		
		//Adds listeners for the key release, especially relevant to 'Q' and 'E'
		levelScene.setOnKeyReleased( (KeyEvent event) -> {
			String KeyName = event.getCode().toString();
			
			//Toggles the player size with 'E' key.
			if(event.getCode() == KeyCode.E) {
				if(player.getMass() != 2)
					player.grow(surfaces);
				else
					player.returnToBaseSize(surfaces);
			}
			
			//Toggles the player size with 'Q' key.
			if(event.getCode() == KeyCode.Q) {
				if(player.getMass() != 0.5)
					player.shrink();
				else
					player.returnToBaseSize(surfaces);
			}
			
			inputs.remove(KeyName);
		});
		
		//background music is set to loop infinitely
		backgroundMusicPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				backgroundMusicPlayer.seek(Duration.ZERO);
			}//end run
		});


		//Game and music are started as soon as level is loaded
		gameLoop.start();
		backgroundMusicPlayer.play();
		
	}//end Constructor
	
	//This is a helper method used to easily generate all assets in the necessary sequence.
	//PRE: None
	//POST: All level assets are generated in correct sequence
	private void generateLevelAssets() {
		//Creates canvas and context for the game "play area"
		VBox gameArea = new VBox();
		Canvas canvas = new Canvas(3000,3000);
		gameArea.getChildren().add(canvas);										
		gameArea.setAlignment(Pos.TOP_CENTER);
		GraphicsContext context = canvas.getGraphicsContext2D();
		layout.getChildren().add(gameArea);
		
		//Calls helper functions to create rest of level elements
		this.createLoop(context, canvas);
		this.createLevelTimer();
		this.createHealthBar();
		this.createUI();
	}
	
	
	//PRE: none
	//POST: A levelTimer is created that counts down from levelTime; displayed at the top right.
	private void createLevelTimer() {
		timerText = new Text();
		timerText.setFont(Font.font("Arial", FontWeight.BOLD, 40));
		
		TextFlow displayedTime = new TextFlow();
		displayedTime.setTextAlignment(TextAlignment.RIGHT);
		displayedTime.getChildren().add(timerText);
		
		//A timer is created that counts down from levelTime
		this.timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				levelTime--;
				timerText.setText("Time Remaining:\t" + Double.toString(levelTime));
				if (levelTime == 0)
					timer.cancel();
			}
		}, 0,1000);
		
		layout.getChildren().add(displayedTime);
	}//End createLevelTimer
	
	
	//PRE: A valid-player object already exists
	//POST: A healthBar based on the player is created
	private void createHealthBar() {
		//Creates "filled" portion of healthbar
		HBox health = new HBox();
		health.setAlignment(Pos.TOP_LEFT);
		health.setPadding(new Insets(10, 10, 0, 5));
		Rectangle healthBar = new Rectangle(10, 100, player.getHealth(), 25);
		healthBar.setFill(Color.GREEN);
		health.getChildren().add(healthBar);
		
		//Creates underlying red "unfilled" portion of healthbar
		HBox healthRed = new HBox();
		healthRed.setAlignment(Pos.TOP_LEFT);
		healthRed.setPadding(new Insets(10, 10, 0, 5));
		Rectangle healthBarRed = new Rectangle(10, 100, 200, 25);
		healthBarRed.setFill(Color.RED);
		healthRed.getChildren().add(healthBarRed);
		this.healthBar = healthBar;
		
		//Adds the underlying red healthbar and the green portion to the levels layout
		layout.getChildren().addAll(healthRed, health);
	}//end createHealthBar
	
	//POST: Creates the "Go Back" button on the level scene
	//TEMP: Other options for stuff like settings and scene should later be added here
	private void createUI() {
		Button returnMe = new Button(); //Simple button to return to the main menu, progress not saved
		
		Image returnGraphic = new Image("Pictures/goback.png");
		ImageView viewReturnGraphic = new ImageView(returnGraphic);
		returnMe.setGraphic(viewReturnGraphic);
		
		//Button pauses loop, cancels timers, and notifies GameFrame what to do
		returnMe.setOnAction(e -> {
			this.timer.cancel();
			gameLoop.stop();
			this.notifyObservers(0);
		});
		
		//Button absorbs spacebar, enter, and tab keys so it MUST be manually clicked.
		returnMe.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB)
					event.consume();
			}//end handle
		});
		
		HBox align = new HBox();
		align.setAlignment(Pos.BOTTOM_RIGHT);
		align.getChildren().add(returnMe);
		
		layout.getChildren().add(align);
	}//End createUI
	
	//PRE: The correct (non-null) context and canvas the sprites should be drawn to must be passed here.
	//POST: A game loop is created that can render and update all sprites alongside checking for death or victory
	private void createLoop(GraphicsContext context, Canvas canvas) {
		
		//Scrollcam is set to player and relevant canvas to track
		ScrollCam cam = new ScrollCam(player, canvas);
		
		//Timer for our game that calls interactions and renders sprites.
		gameLoop = new AnimationTimer(){
		
			public void handle(long nanotime) {
				
				//"Erases" all previous frames by redrawing area around player as white in a colossal radius that extends beyond all resolutions.
				context.setFill(Color.WHITE);
				context.fillRect(player.getPosition().getX()-1500, player.getPosition().getY()-1500, 3000, 3000);
				
				//Redraws the stone castle background around the player to give the "illusion" of being inside.
				context.drawImage(background, player.getPosition().getX() + player.getBoundaryWidth()/2 - background.getWidth()/2, player.getPosition().getY() + player.getBoundaryHeight() - background.getHeight()/2);
				
				//updates the HealthBar to match player Health, if there is one on this level
				if(healthBar != null) healthBar.setWidth(player.getHealth());
				
				//Iterator and pointer used to move through surfaces list
				Iterator<Sprite> it = surfaces.iterator();
				Sprite pointer;
				
				
				//Renders and updates every object in level in order of declaration
				while(it.hasNext()) {
					pointer = it.next();
					if(pointer instanceof PlayerObject)
						((PlayerObject)pointer).playerUpdate(surfaces, hazards);
					else if (pointer instanceof PhysicsSprite)
						((PhysicsSprite)pointer).update(surfaces);
					else
						pointer.update();
					pointer.render(context);
				}//end while
				
				//Loop runs through nonsurfaces and renders, no updates required since sprites are uninteractable.
				for(Sprite renderPointer : nonsurfaces) {
					renderPointer.render(context);
				}
				
				//Runs through surfaces to remove all enemies who's health has been depleted
				for(int i = 0; i < surfaces.size(); i++) {
					pointer = surfaces.get(i);
					if(pointer instanceof Enemy && ((Enemy)pointer).getHealth() <= 0) {
						surfaces.remove(pointer);
						Level.this.levelScore += 320;
					}
				}
				
				//For loop runs through collectibles and removes them if touched by player, also updates score
				for(int i = 0; i < collectibles.size(); i++) {
					pointer = collectibles.get(i);
					if(pointer.overlaps(player)) {
						Level.this.levelScore += 80;
						
						if(player.getHealth() < 200)
							if(player.getHealth() + 40 >= 200) player.setHealth(200);
							else player.setHealth(player.getHealth() + 40);
						
						Level.this.gemCollected.play();
						collectibles.remove(pointer);
					}
				}
				//Runs through the collectibles to render all of them
				for(Sprite collect : collectibles) {
					collect.render(context);
				}
				//Collectibles needs to be done in these 2 loops to avoid a concurrency error with collectibles changing size
				
				
				//Series of if statements checking for player inputs
				if (inputs.contains("A"))
					player.setVelocityX(-5);
				if (inputs.contains("D"))
					player.setVelocityX(5);
				if(inputs.contains("A") && inputs.contains("D"))
					player.setVelocityX(0);	
				if(!(inputs.contains("A") || inputs.contains("D")))
					player.setVelocityX(0);;
				//Jump action requires slime to also be on surface
				if (inputs.contains("W") && player.isOnSurface() && !player.isBouncing()) {
					player.getVelocity().add(0,-20);
					player.playJumpSound();
				}
				
				
				//updates camera to center itself on the slime
				cam.readjust(context);
				cam.update(context);
				
				
				//notifies if player is dead, which should transition to the death scene
				if(player.getHealth() <= 0) {
					Level.this.timer.cancel();
					Level.this.notifyObservers(1);
					this.stop(); //Stops the gameLoop upon player losing.
				}//end if
				
				//notifies if player has beat the level by reaching the goal-post object and should transition to the death scene
				else if(finish != null && player.overlaps(finish)){
					Level.this.levelScore += player.getHealth()*3 + Level.this.levelTime*2;
					Level.this.timer.cancel();
					Level.this.levelBeaten.play();
					Level.this.notifyObservers(2); 
					this.stop(); //Stops the gameLoop upon level completion
				}//end if
				
				
			}//end handle()
			
		}; //end gameLoop
		
	}//End createLoop
	
	//PRE: none.
	//POST: All entities for level 1 are created and "loaded" onto their corresponding lists.
	private void createLevel1Entities() {
		
		//First tutorial image for horizontal movement with 'A' and 'D' keys
		Sprite movTutorial1 = new Sprite();
		movTutorial1.setImage("Pictures/HorizontalTutorial.png");
		movTutorial1.getPosition().set(20, -movTutorial1.getBoundaryHeight() - 190);
		nonsurfaces.add(movTutorial1);
		
		//Second tutorial image for jump with 'W' key
		Sprite movTutorial2 = new Sprite();
		movTutorial2.setImage("Pictures/VerticalTutorial.png");
		movTutorial2.getPosition().set(1400 - movTutorial2.getBoundaryWidth()/2, -movTutorial2.getBoundaryHeight() -190);
		nonsurfaces.add(movTutorial2);
		
		Sprite floor1 = new Sprite();
		floor1.setImage("Pictures/Floor.png");
		floor1.getPosition().set(0, 0);
		floor1.setSolid(true);
		surfaces.add(floor1);
		
		Sprite floor2 = new Sprite();
		floor2.setImage("Pictures/Floor.png");
		floor2.getPosition().set(floor1.getBoundaryWidth(), 0);
		floor2.setSolid(true);
		surfaces.add(floor2);
		
		Sprite floor3 = new Sprite();
		floor3.setImage("Pictures/Floor.png");
		floor3.getPosition().set(floor2.getBoundaryX() + floor2.getBoundaryWidth() + 700, 0);
		floor3.setSolid(true);
		surfaces.add(floor3);
		
		Sprite floor0 = new Sprite();
		floor0.setImage("Pictures/Floor.png");
		floor0.getPosition().set(-floor0.getBoundaryWidth(), 0);
		floor0.setSolid(true);
		surfaces.add(floor0);
		
		Sprite floor01 = new Sprite();
		floor01.setImage("Pictures/Floor.png");
		floor01.getPosition().set(floor2.getBoundaryX() + floor2.getBoundaryWidth()/2, 400);
		floor01.setSolid(true);
		surfaces.add(floor01);
		
		Sprite spike1 = new Sprite();
		spike1.setImage("Pictures/Spikes.png");
		spike1.getPosition().set(1400, 0 - spike1.getBoundaryHeight());
		spike1.setSolid(true);
		surfaces.add(spike1);
		hazards.add(spike1);
		
		Sprite leftBoundaryWall = new Sprite();
		leftBoundaryWall.setImage("Pictures/Brick.png");
		leftBoundaryWall.getPosition().set(-leftBoundaryWall.getBoundaryWidth(), -leftBoundaryWall.getBoundaryHeight());
		leftBoundaryWall.setSolid(true);
		surfaces.add(leftBoundaryWall);
		
		//Tutorial for shrinking with 'Q' key
		Sprite shrinkTutorial = new Sprite();
		shrinkTutorial.setImage("Pictures/ShrinkTutorial.png");
		shrinkTutorial.getPosition().set(2500 - shrinkTutorial.getBoundaryWidth() -10, -shrinkTutorial.getBoundaryHeight() - 190);
		nonsurfaces.add(shrinkTutorial);
		
		Sprite brickObstacle1 = new Sprite();
		brickObstacle1.setImage("Pictures/Brick.png");
		brickObstacle1.getPosition().set(2500, -brickObstacle1.getBoundaryHeight() -100);
		brickObstacle1.setSolid(true);
		surfaces.add(brickObstacle1);
		
		PlayerObject slime = new PlayerObject(1, .50, 200);
		slime.setImage("Pictures/Slime.png");
		slime.getPosition().set(20, 0 - slime.getBoundaryHeight());
		player = slime;
		surfaces.add(slime);
		
		//Tutorial for moving boxes at normal size
		Sprite movTutorial3 = new Sprite();
		movTutorial3.setImage("Pictures/MoveTutorial.png");
		movTutorial3.getPosition().set(brickObstacle1.getBoundaryX() + brickObstacle1.getBoundaryWidth() + 200, -movTutorial3.getBoundaryHeight() - 190);
		nonsurfaces.add(movTutorial3);
		
		PhysicsSprite crate1 = new PhysicsSprite(1, .5);
		crate1.setImage("Pictures/Crate.png");
		crate1.getPosition().set(brickObstacle1.getBoundaryWidth() + brickObstacle1.getBoundaryX() + 400, -crate1.getBoundaryHeight());
		surfaces.add(crate1);
		
		Sprite brickObstacle2 = new Sprite();
		brickObstacle2.setImage("Pictures/Brick.png");
		brickObstacle2.getPosition().set(crate1.getBoundaryX() + crate1.getBoundaryWidth() + 400, -brickObstacle2.getBoundaryHeight());
		brickObstacle2.setSolid(true);
		surfaces.add(brickObstacle2);
		
		//Tutorial for how gem collections work
		Sprite gemTutorial = new Sprite();
		gemTutorial.setImage("Pictures/GemTutorial.png");
		gemTutorial.getPosition().set(brickObstacle2.getBoundaryX() + brickObstacle2.getBoundaryWidth() + 500, -gemTutorial.getBoundaryHeight() - 190);
		nonsurfaces.add(gemTutorial);
		
		Sprite gem = new Sprite();
		gem.setImage("Pictures/GemStone.png");
		gem.getPosition().set(brickObstacle2.getBoundaryX() + brickObstacle2.getBoundaryWidth() + 500 + gem.getBoundaryWidth(), -gem.getBoundaryHeight());
		collectibles.add(gem);
		
		//Tutorial for growing with the 'E' key
		Sprite hugeTutorial = new Sprite();
		hugeTutorial.setImage("Pictures/HugeTutorial.png");
		hugeTutorial.getPosition().set(brickObstacle2.getBoundaryX() + brickObstacle2.getBoundaryWidth() + 1300, -hugeTutorial.getBoundaryHeight() - 190);
		nonsurfaces.add(hugeTutorial);
		
		PhysicsSprite bigHeavyCrate = new PhysicsSprite(2, .5);
		bigHeavyCrate.setImage("Pictures/GiantMetalCrate.png");
		bigHeavyCrate.getPosition().set(brickObstacle2.getBoundaryX() + brickObstacle2.getBoundaryWidth() + 2200, -bigHeavyCrate.getBoundaryHeight());
		surfaces.add(bigHeavyCrate);
		
		//Tutorial for how to beat a level
		Sprite portalTutorial = new Sprite();
		portalTutorial.setImage("Pictures/PortalTutorial.png");
		portalTutorial.getPosition().set(floor3.getBoundaryX(), -portalTutorial.getBoundaryHeight() - 190);
		nonsurfaces.add(portalTutorial);
		
		Sprite portal = new Sprite();
		portal.setImage("Pictures/Finish.png");
		portal.getPosition().set(floor3.getBoundaryX() + 300 + portal.getBoundaryWidth(), -portal.getBoundaryHeight());
		surfaces.add(portal);
		finish = portal;
	}//End of createLevel1Entities
	
	//PRE: none.
	//POST: All entities for level 2 are created and "loaded" onto their corresponding lists.
	private void createLevel2Entities() {
		
		Sprite floor0 = new Sprite();
		floor0.setImage("Pictures/Floor.png");
		floor0.getPosition().set(-floor0.getBoundaryWidth(), 0);
		floor0.setSolid(true);
		surfaces.add(floor0);
		
		Sprite floor1 = new Sprite();
		floor1.setImage("Pictures/Floor.png");
		floor1.getPosition().set(0, 0);
		floor1.setSolid(true);
		surfaces.add(floor1);
		
		Sprite leftBoundaryWall = new Sprite();
		leftBoundaryWall.setImage("Pictures/Brick.png");
		leftBoundaryWall.getPosition().set(-leftBoundaryWall.getBoundaryWidth(), -leftBoundaryWall.getBoundaryHeight());
		leftBoundaryWall.setSolid(true);
		surfaces.add(leftBoundaryWall);
		
		Sprite enemyTutorial = new Sprite();
		enemyTutorial.setImage("Pictures/EnemyTutorial.png");
		enemyTutorial.getPosition().set(40, -enemyTutorial.getBoundaryHeight() - 190);
		nonsurfaces.add(enemyTutorial);
		
		Sprite block1 = new Sprite();
		block1.setImage("Pictures/Block.png");
		block1.getPosition().set(700, -block1.getBoundaryHeight());
		block1.setSolid(true);
		surfaces.add(block1);
		
		Sprite spike1 = new Sprite();
		spike1.setImage("Pictures/Spikes.png");
		spike1.getPosition().set(block1.getBoundaryX() + block1.getBoundaryWidth() + 150, -spike1.getBoundaryHeight());
		spike1.setSolid(true);
		surfaces.add(spike1);
		hazards.add(spike1);
		
		Sprite spike2 = new Sprite();
		spike2.setImage("Pictures/Spikes.png");
		spike2.getPosition().set(spike1.getBoundaryX() + spike1.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike2.setSolid(true);
		surfaces.add(spike2);
		hazards.add(spike2);
		
		Sprite spike3 = new Sprite();
		spike3.setImage("Pictures/Spikes.png");
		spike3.getPosition().set(spike2.getBoundaryX() + spike2.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike3.setSolid(true);
		surfaces.add(spike3);
		hazards.add(spike3);
		
		Sprite spike4 = new Sprite();
		spike4.setImage("Pictures/Spikes.png");
		spike4.getPosition().set(spike3.getBoundaryX() + spike3.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike4.setSolid(true);
		surfaces.add(spike4);
		hazards.add(spike4);
		
		Sprite spike5 = new Sprite();
		spike5.setImage("Pictures/Spikes.png");
		spike5.getPosition().set(spike4.getBoundaryX() + spike4.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike5.setSolid(true);
		surfaces.add(spike5);
		hazards.add(spike5);
		
		Sprite spike6 = new Sprite();
		spike6.setImage("Pictures/Spikes.png");
		spike6.getPosition().set(spike5.getBoundaryX() + spike5.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike6.setSolid(true);
		surfaces.add(spike6);
		hazards.add(spike6);
		
		Sprite spike7 = new Sprite();
		spike7.setImage("Pictures/Spikes.png");
		spike7.getPosition().set(spike6.getBoundaryX() + spike6.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike7.setSolid(true);
		surfaces.add(spike7);
		hazards.add(spike7);
		
		Sprite spike8 = new Sprite();
		spike8.setImage("Pictures/Spikes.png");
		spike8.getPosition().set(spike7.getBoundaryX() + spike7.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike8.setSolid(true);
		surfaces.add(spike8);
		hazards.add(spike8);
		
		Sprite spike9 = new Sprite();
		spike9.setImage("Pictures/Spikes.png");
		spike9.getPosition().set(spike8.getBoundaryX() + spike8.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike9.setSolid(true);
		surfaces.add(spike9);
		hazards.add(spike9);
		
		Sprite spike10 = new Sprite();
		spike10.setImage("Pictures/Spikes.png");
		spike10.getPosition().set(spike9.getBoundaryX() + spike9.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike10.setSolid(true);
		surfaces.add(spike10);
		hazards.add(spike10);
		
		Sprite spike11 = new Sprite();
		spike11.setImage("Pictures/Spikes.png");
		spike11.getPosition().set(spike10.getBoundaryX() + spike10.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike11.setSolid(true);
		surfaces.add(spike11);
		hazards.add(spike11);
		
		Sprite spike12 = new Sprite();
		spike12.setImage("Pictures/Spikes.png");
		spike12.getPosition().set(spike11.getBoundaryX() + spike11.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike12.setSolid(true);
		surfaces.add(spike12);
		hazards.add(spike12);
		
		Sprite spike13 = new Sprite();
		spike13.setImage("Pictures/Spikes.png");
		spike13.getPosition().set(spike12.getBoundaryX() + spike12.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike13.setSolid(true);
		surfaces.add(spike13);
		hazards.add(spike13);
		
		Sprite spike14 = new Sprite();
		spike14.setImage("Pictures/Spikes.png");
		spike14.getPosition().set(spike13.getBoundaryX() + spike13.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike14.setSolid(true);
		surfaces.add(spike14);
		hazards.add(spike14);
		
		Sprite spike15 = new Sprite();
		spike15.setImage("Pictures/Spikes.png");
		spike15.getPosition().set(spike14.getBoundaryX() + spike14.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike15.setSolid(true);
		surfaces.add(spike15);
		hazards.add(spike15);
		
		Sprite spike16 = new Sprite();
		spike16.setImage("Pictures/Spikes.png");
		spike16.getPosition().set(spike15.getBoundaryX() + spike15.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike16.setSolid(true);
		surfaces.add(spike16);
		hazards.add(spike16);
		
		Sprite spike17 = new Sprite();
		spike17.setImage("Pictures/Spikes.png");
		spike17.getPosition().set(spike16.getBoundaryX() + spike16.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike17.setSolid(true);
		surfaces.add(spike17);
		hazards.add(spike17);
		
		Sprite spike18 = new Sprite();
		spike18.setImage("Pictures/Spikes.png");
		spike18.getPosition().set(spike17.getBoundaryX() + spike17.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike18.setSolid(true);
		surfaces.add(spike18);
		hazards.add(spike18);
		
		Sprite spike19 = new Sprite();
		spike19.setImage("Pictures/Spikes.png");
		spike19.getPosition().set(spike18.getBoundaryX() + spike18.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike19.setSolid(true);
		surfaces.add(spike19);
		hazards.add(spike19);
		
		Sprite spike20 = new Sprite();
		spike20.setImage("Pictures/Spikes.png");
		spike20.getPosition().set(spike19.getBoundaryX() + spike19.getBoundaryWidth(), -spike1.getBoundaryHeight());
		spike20.setSolid(true);
		surfaces.add(spike20);
		hazards.add(spike20);
		
		Sprite block2 = new Sprite();
		block2.setImage("Pictures/Block.png");
		block2.getPosition().set(block1.getBoundaryX() + block1.getBoundaryWidth() + 400, -block2.getBoundaryHeight() - 300);
		block2.setSolid(true);
		surfaces.add(block2);
		
		Sprite block3 = new Sprite();
		block3.setImage("Pictures/Block.png");
		block3.getPosition().set(block2.getBoundaryX() + block2.getBoundaryWidth() + 400, -block3.getBoundaryHeight() - 300);
		block3.setSolid(true);
		surfaces.add(block3);
		
		Sprite block4 = new Sprite();
		block4.setImage("Pictures/Block.png");
		block4.getPosition().set(block3.getBoundaryX() + block3.getBoundaryWidth() + 400, -block4.getBoundaryHeight() - 300);
		block4.setSolid(true);
		surfaces.add(block4);
		
		Sprite block5 = new Sprite();
		block5.setImage("Pictures/Block.png");
		block5.getPosition().set(block4.getBoundaryX() + block4.getBoundaryWidth() + 400, -block5.getBoundaryHeight() - 300);
		block5.setSolid(true);
		surfaces.add(block5);
		
		Sprite brickObstacle1 = new Sprite();
		brickObstacle1.setImage("Pictures/Brick.png");
		brickObstacle1.getPosition().set(spike20.getBoundaryX() + spike20.getBoundaryWidth(), -brickObstacle1.getBoundaryHeight());
		brickObstacle1.setSolid(true);
		surfaces.add(brickObstacle1);
		
		Sprite blockStep = new Sprite();
		blockStep.setImage("Pictures/Block.png");
		blockStep.getPosition().set(brickObstacle1.getBoundaryX() + brickObstacle1.getBoundaryWidth(), -blockStep.getBoundaryHeight());
		blockStep.setSolid(true);
		surfaces.add(blockStep);
		
		Sprite gem1 = new Sprite();
		gem1.setImage("Pictures/GemStone.png");
		gem1.getPosition().set(blockStep.getBoundaryX() + blockStep.getBoundaryWidth() + 150, -gem1.getBoundaryHeight());
		collectibles.add(gem1);
		
		Sprite gem2 = new Sprite();
		gem2.setImage("Pictures/GemStone.png");
		gem2.getPosition().set(gem1.getBoundaryX() + gem1.getBoundaryWidth() + 30, -gem1.getBoundaryHeight());
		collectibles.add(gem2);
		
		Sprite gem3 = new Sprite();
		gem3.setImage("Pictures/GemStone.png");
		gem3.getPosition().set(gem2.getBoundaryX() + gem2.getBoundaryWidth() + 30, -gem1.getBoundaryHeight());
		collectibles.add(gem3);
		
		Sprite gem4 = new Sprite();
		gem4.setImage("Pictures/GemStone.png");
		gem4.getPosition().set(gem3.getBoundaryX() + gem3.getBoundaryWidth() + 30, -gem1.getBoundaryHeight());
		collectibles.add(gem4);
		
		Sprite gem5 = new Sprite();
		gem5.setImage("Pictures/GemStone.png");
		gem5.getPosition().set(gem4.getBoundaryX() + gem4.getBoundaryWidth() + 30, -gem1.getBoundaryHeight());
		collectibles.add(gem5);
		
		Sprite gem6 = new Sprite();
		gem6.setImage("Pictures/GemStone.png");
		gem6.getPosition().set(gem5.getBoundaryX() + gem5.getBoundaryWidth() + 30, -gem1.getBoundaryHeight());
		collectibles.add(gem6);
		
		Sprite brickObstacle2 = new Sprite();
		brickObstacle2.setImage("Pictures/Brick.png");
		brickObstacle2.getPosition().set(brickObstacle1.getBoundaryX() + 200, brickObstacle1.getBoundaryY() - brickObstacle2.getBoundaryHeight() - 130);
		brickObstacle2.setSolid(true);
		surfaces.add(brickObstacle2);
		
		Sprite block6 = new Sprite();
		block6.setImage("Pictures/Block.png");
		block6.getPosition().set(brickObstacle1.getBoundaryX(), brickObstacle2.getBoundaryY() + brickObstacle2.getBoundaryHeight()/2 + 40);
		block6.setSolid(true);
		surfaces.add(block6);
		
		Sprite brickFloor = new Sprite();
		brickFloor.setImage("Pictures/Brick.png");
		brickFloor.getPosition().set(floor1.getBoundaryX() + floor1.getBoundaryWidth(), 0);
		brickFloor.setSolid(true);
		surfaces.add(brickFloor);
		
		Sprite floor2 = new Sprite();
		floor2.setImage("Pictures/Floor.png");
		floor2.getPosition().set(brickObstacle2.getBoundaryX() + brickObstacle2.getBoundaryWidth(), brickObstacle1.getBoundaryY() - floor2.getBoundaryHeight() - 130);
		floor2.setSolid(true);
		surfaces.add(floor2);
		
		Enemy evilEye1 = new Enemy(1, .50, 200, "Pictures/EyeRight.png", "Pictures/Eye.png");
		evilEye1.setImage("Pictures/Eye.png");
		evilEye1.getPosition().set(floor2.getBoundaryX() + 300, floor2.getBoundaryY() - evilEye1.getBoundaryHeight());
		surfaces.add(evilEye1);
		
		PhysicsSprite crate1 = new PhysicsSprite(1, .50);
		crate1.setImage("Pictures/Crate.png");
		crate1.getPosition().set(evilEye1.getBoundaryX() + evilEye1.getBoundaryWidth() + 900, floor2.getBoundaryY() - crate1.getBoundaryHeight());
		surfaces.add(crate1);
		
		Sprite gem7 = new Sprite();
		gem7.setImage("Pictures/GemStone.png");
		gem7.getPosition().set(crate1.getBoundaryX() + crate1.getBoundaryWidth()/2 - gem7.getBoundaryWidth()/2, crate1.getBoundaryY() - gem7.getBoundaryHeight());
		collectibles.add(gem7);
		
		PhysicsSprite heavyCrate = new PhysicsSprite(2, .50);
		heavyCrate.setImage("Pictures/GiantMetalCrate.png");
		heavyCrate.getPosition().set(crate1.getBoundaryX() + crate1.getBoundaryWidth() + 300, floor2.getBoundaryY() - heavyCrate.getBoundaryHeight());
		surfaces.add(heavyCrate);
		
		Enemy evilEye2 = new Enemy(1, .50, 200, "Pictures/EyeRight.png", "Pictures/Eye.png");
		evilEye2.setImage("Pictures/Eye.png");
		evilEye2.getPosition().set(heavyCrate.getBoundaryX() + heavyCrate.getBoundaryWidth()/2 - evilEye2.getBoundaryWidth()/2, heavyCrate.getBoundaryY() - evilEye2.getBoundaryHeight());
		surfaces.add(evilEye2);
		
		Sprite floor3 = new Sprite();
		floor3.setImage("Pictures/Floor.png");
		floor3.getPosition().set(heavyCrate.getBoundaryX() + heavyCrate.getBoundaryWidth() + 1000, floor2.getBoundaryY() - floor3.getBoundaryHeight());
		floor3.setSolid(true);
		surfaces.add(floor3);
		
		Sprite portal = new Sprite();
		portal.setImage("Pictures/Finish.png");
		portal.getPosition().set(floor3.getBoundaryX() + 500, floor3.getBoundaryY() - portal.getBoundaryHeight());
		surfaces.add(portal);
		finish = portal;
		
		PlayerObject slime = new PlayerObject(1, .50, 200);
		slime.setImage("Pictures/Slime.png");
		slime.getPosition().set(0, floor1.getBoundaryY() - slime.getBoundaryHeight());
		player = slime;
		surfaces.add(slime);

		
		
	}//End of createLevel2Entities
	
	/**Adds a new observer to this Level object.
	 * @param o The observer being added
	 * */
	@Override
	//PRE: none.
	//POST: A new observer is added onto this level.
	public void subscribeObserver(LevelObserver o) {
		observers.add(o);
	}//end subscribeObserver
	
	/**notifyObservers calls for update() in all observers with a given switchCase, levelScore, and levelTime.
	 * @param switchCase The integer value an observer will use to determine what to do.
	 * */
	@Override
	//PRE: Called with a number from 0-2
	//POST: GameFrame will be notified what scene to change to based on switchCase with a corresponding score and time
	public void notifyObservers(int switchCase) {
		
		//Pauses music on level exit
		this.backgroundMusicPlayer.pause();
		
		for(LevelObserver observer : observers) {
			observer.update(switchCase, levelScore, levelTime);
		}//end for
		
	}//end notifyObservers
	
	/**updateVolume changes the volume of all sounds on this Level object.
	 * @param volume The value that will be used to change the volume of every sound.
	 * */
	//PRE: Called with a number from 0-100
	//POST: Volume for ALL sounds are adjusted based on a ratio from the number given.
	public void updateVolume(double volume) {
		this.backgroundMusicPlayer.setVolume(volume * 0.003);
		PlayerObject.changeVolume(volume);
		Enemy.changeVolume(volume);
		this.gemCollected.setVolume(volume * 0.003);
		this.levelBeaten.setVolume(volume * 0.0015);
	}
	
	/** startGame will resume all relevant timers on a level including the game loop and background music */ 
	//PRE: The level has already been generated.
	//POST: Timers, music, and the gameloop are all resumed with fresh input.
	public void startGame() {
		this.resumeTimer();
		this.inputs.clear();
		this.backgroundMusicPlayer.play();
		gameLoop.start();
	}
	
	public void pauseGame() {
		this.timer.cancel();
		this.gameLoop.stop();
		this.backgroundMusicPlayer.stop();
		this.inputs.clear();
	}
	
	//PRE: A timer has been created.
	//POST: The level timer is "resumed" at the same time the old one was canceled.
	private void resumeTimer() {
		//There is no pause function so a new timer must be created, that counts
		//down from where the old one left off.
		this.timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				levelTime--;
				timerText.setText("Time Remaining:\t" + Double.toString(levelTime));
				if (levelTime == 0)
					timer.cancel();
			}//end run
		}, 0,1000);
	}//end resumeTimer
	
	//PRE: The levelScene has already been set.
	//POST: The levelScene is returned; for purposes of continuing the game.
	public Scene getLevelScene() {
		return levelScene;
	}

	public void setLevelScene(Scene levelScene) {
		this.levelScene = levelScene;
	}

}
