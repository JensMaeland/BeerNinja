package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;

public class SamfNinja extends ApplicationAdapter {
	// imported help-classes
	SpriteBatch screenDrawer;
	Texture background;
	Texture homeScreen;
	Texture loadingScreen;
	Texture hitboxTexture;
	Texture gameOverTexture;
	Texture splash;
	BitmapFont font;
	// our own help-classes
	BeerSocket socket;
	GenerateBeerFromData generatedSprites = null;
	HashMap<Integer, Touch> touches = new HashMap<>();
	CaughtBottle latestCaughtBottle = null;
	// variables;
	private boolean loading = false;
	int currentTouchIndex = 0;

	// game-settings as variables
	private boolean devMode = false; // set devmode for easier testing
	private double gameTimer = -2; // set seconds until game start
	final double gameEndTime = 38; // set duration of each game
	final double powerUpTimer = 20; // set when the powerUp shoud spawn
	final int tailLength = 35; // set length of tail when touching the screen
	final int screenWidth = 500; // set width of game screen
	String backgroundImage = "map3.png"; // set which background-image should display

	@Override
	public void create () {
		// instancing a new batch drawer
		screenDrawer = new SpriteBatch();
		// instancing a new font drawer
		font = new BitmapFont();
		font.getData().setScale(2, 2);
		// connect the socket and receive generated sprites from the server
		socket = new BeerSocket();

		// instancing objects for touch feature
		for (int i = 0; i < tailLength; i++) {
			touches.put(i, new Touch(i, 0, 0));
		}

		// setting textures to correct images
		background = new Texture(backgroundImage);
		homeScreen = new Texture("home2.png");
		loadingScreen = new Texture("loading.png");
		gameOverTexture = new Texture("gameOver.png");
		splash = new Texture("splash.png");
	}

	@Override
	public void render () {
		if (generatedSprites == null) {
			mainMenu();
			return;
		}

		gameTimer += Gdx.graphics.getDeltaTime();

		if (gameTimer < gameEndTime) {
			renderGUI();
			renderBeerSprites();
			getAndRenderUserTouches();
			checkHitboxes();
			socket.getPoints();
		}
		else {
			gameOver();
		}
	}

	public void renderGUI() {
		float brightness;
		if (!devMode) {
			brightness = (float) Math.max(0.6, (gameTimer - Math.round(gameTimer)) + 0.5);
		}
		else {
			brightness = 0.6F;
		}

		screenDrawer.begin();
		screenDrawer.setColor(brightness, brightness, 1F, 1F);
		screenDrawer.draw(background, 0, 0);
		screenDrawer.setColor(1F, 1F, 1F, 1F);

		font.setColor(1,1,0.2F,1);
		font.draw(screenDrawer, "Meg: " + socket.myPoints, 50, Gdx.graphics.getHeight() - 50);
		font.draw(screenDrawer, "P2: " + socket.enemyPoints, screenWidth-80, Gdx.graphics.getHeight() - 50);
		if (latestCaughtBottle != null && latestCaughtBottle.time > gameTimer - 5 && !devMode) {
			brightness = (float) (1.0-(gameTimer - latestCaughtBottle.time));
			screenDrawer.setColor(brightness, brightness, brightness, brightness);
			screenDrawer.draw(splash, (float) latestCaughtBottle.xcoor - 80, (float) latestCaughtBottle.ycoor + 40);
			screenDrawer.setColor(1F, 1F, 1F, 1F);
		}
		screenDrawer.end();
	}

	public void mainMenu () {
		screenDrawer.begin();
		if (!loading) {
			screenDrawer.draw(homeScreen, 0, 0);
		}
		else {
			screenDrawer.draw(loadingScreen, 0, 0);
		}
		screenDrawer.end();

		final int buttonsStartPos = 515;
		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				// multiplayer game clicked
				if (screenY >= buttonsStartPos && screenY <= buttonsStartPos + 100) {
					loading = true;
				}
				// solo game clicked
				else if (screenY >= buttonsStartPos + 100 && screenY <= buttonsStartPos + 200) {
					loading = true;
				}
				// dev game clicked
				else if (screenY >= buttonsStartPos + 200 && screenY <= buttonsStartPos + 300) {
					loading = true;
					devMode = true;
				}
				return true;
			};

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				// multiplayer game clicked
				if (screenY >= buttonsStartPos && screenY <= buttonsStartPos + 100) {
					socket.setUpGame();
					generatedSprites = socket.generateSprites();
					gameTimer = -2;
				}
				// solo game clicked
				else if (screenY >= buttonsStartPos + 100 && screenY <= buttonsStartPos + 200) {
					//	socket.setUpGame( solo );
					gameTimer = -2;
				}
				// dev game clicked
				else if (screenY >= buttonsStartPos + 200 && screenY <= buttonsStartPos + 300) {
					hitboxTexture = new Texture("touch.png");
					socket.setUpGame();
					generatedSprites = socket.generateSprites();
					gameTimer = -2;
				}

				if (screenY >= buttonsStartPos && screenY <= buttonsStartPos + 200) {
					// play sound to start off the game
					Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("crack.mp3"));
					beerPop.play();
					// play background music
					Sound backgroundMusic = Gdx.audio.newSound(Gdx.files.internal("theMidnight.mp3"));
					backgroundMusic.play();
				}
				return true;
			};
		});
	}

	private void renderBeerSprites() {
		for (Bottle beerBottle : generatedSprites.spawn(gameTimer)) {
			float x = (float) beerBottle.getXOffset(gameTimer);
			float y = (float) beerBottle.getYOffset(gameTimer);
			int width = beerBottle.beerTexture.getRegionWidth();
			int height = beerBottle.beerTexture.getRegionHeight();

			screenDrawer.begin();
			screenDrawer.draw(beerBottle.beerTexture, x, y, width/2F, height/2F, width, height, 1, 1, (float) beerBottle.getSpin(gameTimer));
			screenDrawer.end();
		}
	}

	private void getAndRenderUserTouches() {
		// get touches
		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				Touch touch = touches.get(currentTouchIndex);
				touch.x = screenX;
				touch.y = Gdx.graphics.getHeight() - screenY;
				touch.display = true;
				currentTouchIndex = (currentTouchIndex + 1) % tailLength;
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				for (int i = 0; i < tailLength; i++) {
					Touch touch = touches.get(i);
					touch.display = false;
				}
				return true;
			}
		});

		// render touches
		Touch prevTouch = null;
		for (Touch touch : touches.values()) {
			if (touch.display) {
				screenDrawer.begin();

				if (prevTouch != null) {
					float deltaX = (float) touch.x - prevTouch.x;
					float deltaY = (float) touch.y - prevTouch.y;

					while ((Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) && prevTouch.x != 0 && touch.id - prevTouch.id == 1) {
						screenDrawer.draw(touch.texture, touch.x-deltaX/2, touch.y-deltaY/2);
						screenDrawer.draw(touch.texture, prevTouch.x+deltaX/2, prevTouch.y+deltaY/2);
						deltaX = Math.signum(deltaX)*Math.max(Math.abs(deltaX) - 2, 2);
						deltaY = Math.signum(deltaY)*Math.max(Math.abs(deltaY) - 2, 2);
					}
				}
				prevTouch = touch;

				screenDrawer.draw(touch.texture, touch.x, touch.y);
				screenDrawer.end();
			}
		}
	}

	private void checkHitboxes() {
		for (Bottle beerBottle : generatedSprites.spawn(gameTimer)) {
			Hitbox hitbox = beerBottle.getHitbox(gameTimer);
			Touch touch;
			if (currentTouchIndex > 0) {
				touch = touches.get(currentTouchIndex-1);
			}
			else {
				touch = touches.get(tailLength-1);
			}

			// check touch hits with bottles
			if (touch.display && hitbox.left <= touch.x && touch.x <= hitbox.right) {
				if (hitbox.bottom <= touch.y && touch.y <= hitbox.top) {
					CaughtBottle caughtBottle = new CaughtBottle(beerBottle.bottleId, gameTimer, beerBottle.getXOffset(gameTimer), beerBottle.getYOffset(gameTimer), socket.playerID);
					latestCaughtBottle = caughtBottle;
					generatedSprites.caughtBottle(caughtBottle, devMode);
				}
			}

			// check bootle hitbox with other bottles
			if (hitbox.left > 0 && hitbox.top > 0 && hitbox.left < screenWidth && hitbox.top < Gdx.graphics.getHeight()) {
				for (Bottle compareBottle : generatedSprites.spawn(gameTimer)) {
					Hitbox obstacle = compareBottle.getHitbox(gameTimer);

					if (beerBottle != compareBottle && !beerBottle.collision && !beerBottle.bottlePlayerId.equals(compareBottle.bottlePlayerId)) {
						if (hitbox.right > obstacle.left && hitbox.left < obstacle.right) {
							if (hitbox.top > obstacle.bottom && hitbox.bottom < obstacle.top) {
								beerBottle.xStartPos = (int) beerBottle.getXOffset(gameTimer);
								beerBottle.collision = true;
							}
						}
					}
				}
			}

			// draw the hitboxes in devMode
			if (devMode) {
				screenDrawer.begin();
				screenDrawer.draw(hitboxTexture, (int) (hitbox.left), (int) (hitbox.top));
				screenDrawer.draw(hitboxTexture, (int) (hitbox.right), (int) (hitbox.bottom));
				screenDrawer.draw(hitboxTexture, (int) (hitbox.left), (int) (hitbox.bottom));
				screenDrawer.draw(hitboxTexture, (int) (hitbox.right), (int) (hitbox.top));
				screenDrawer.end();
			}
		}
	}

	public void gameOver() {
		loading = false;
		devMode = false;

		screenDrawer.begin();
		screenDrawer.draw(gameOverTexture, 50, 500);
		screenDrawer.end();

		if (gameTimer > gameEndTime + 5) {
			generatedSprites = null;
		}
	}
	
	@Override
	public void dispose () {
		screenDrawer.dispose();
	}
}
