package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;

public class SamfNinja extends ApplicationAdapter {
	// imported help-classes
	SpriteBatch screenDrawer;
	Texture background;
	Texture gameOverTexture;
	Texture splash;
	BitmapFont font;
	// our own help-classes
	MainMenu mainMenu;
	BeerSocket socket;
	GenerateBeerFromData generatedSprites = null;
	final HashMap<Integer, Touch> touches = new HashMap<>();
	CaughtBottle latestCaughtBottle = null;
	// variables;
	boolean loading = false;
	boolean multiplayer = true;
	int currentTouchIndex = 0;
	int screenWidth;
	int screenHeight;
	// game-settings as variables
	boolean devMode = false; // set devMode for easier testing
	double gameTimer = -2; // set seconds until game start
	final double gameEndTime = 38; // set duration of each game
	final double powerUpTimer = 20; // set when the powerUp should spawn
	final int tailLength = 35; // set length of tail when touching the screen
	final String backgroundImage = "map3.png"; // set which background-image should display

	@Override
	public void create () {
		// instancing a new batch drawer
		screenDrawer = new SpriteBatch();
		//
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		// instancing a new font drawer
		font = new BitmapFont();
		font.getData().setScale(2, 2);
		// instancing the main menu
		mainMenu = new MainMenu();
		// connect the socket and receive generated sprites from the server
		socket = new BeerSocket(tailLength);

		// instancing objects for touch feature
		for (int i = 0; i < tailLength; i++) {
			touches.put(i, new Touch(i, 0, 0, 0, false));
		}

		// setting textures to correct images
		background = new Texture(backgroundImage);
		gameOverTexture = new Texture("gameOver.png");
		splash = new Texture("splash.png");
	}

	@Override
	public void render () {
		if (generatedSprites == null) {
			mainMenu.renderMainMenu(this, socket, screenDrawer);
			return;
		}

		gameTimer += Gdx.graphics.getDeltaTime();

		if (gameTimer < gameEndTime) {
			renderGUI();
			renderBeerSprites();
			getAndRenderUserTouches();
			checkHitboxes();
			socket.sendTouches(touches, currentTouchIndex, multiplayer);
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
		screenDrawer.draw(background, 0, 0, screenWidth, screenHeight);
		screenDrawer.setColor(1F, 1F, 1F, 1F);

		font.setColor(1,1,0.2F,1);
		if (!multiplayer) {
			font.draw(screenDrawer, "Poeng: " + socket.myPoints, 50, screenHeight - 50);
		}
		else {
			font.draw(screenDrawer, "Meg: " + socket.myPoints, 50, screenHeight - 50);
			font.draw(screenDrawer, "P2: " + socket.enemyPoints, screenWidth-80, screenHeight - 50);
		}

		if (latestCaughtBottle != null && latestCaughtBottle.time > gameTimer - 5 && !devMode) {
			brightness = (float) (1.0-(gameTimer - latestCaughtBottle.time));
			screenDrawer.setColor(brightness, brightness, brightness, brightness);
			screenDrawer.draw(splash, (float) latestCaughtBottle.xcoor - 80, (float) latestCaughtBottle.ycoor + 40);
			screenDrawer.setColor(1F, 1F, 1F, 1F);
		}
		screenDrawer.end();
	}

	private void renderBeerSprites() {
		int scale = 1;
		for (Bottle beerBottle : generatedSprites.spawn(gameTimer)) {
			float x = (float) beerBottle.getXOffset(gameTimer);
			float y = (float) beerBottle.getYOffset(gameTimer);
			int width = beerBottle.beerTexture.getRegionWidth();
			int height = beerBottle.beerTexture.getRegionHeight();

			screenDrawer.begin();
			screenDrawer.draw(beerBottle.beerTexture, x, y, (width*scale)/2F, (height*scale)/2F, width, height, scale, scale, (float) beerBottle.getSpin(gameTimer));
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
				touch.y = screenHeight - screenY;
				touch.time = gameTimer;
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

		// render enemy touches
		if (multiplayer) {
			for (Touch touch : socket.enemyTouches.values()) {
				if (touch.display) {
					screenDrawer.begin();
					screenDrawer.draw(touch.texture, screenWidth-touch.x, touch.y);
					screenDrawer.end();
				}
			}
		}
	}

	private void checkHitboxes() {
		// get latest touch object for player and enemy
		Touch touch = touches.get(tailLength-1);
		Touch enemyTouch = socket.enemyTouches.get(tailLength-1);
		if (currentTouchIndex > 0) {
			touch = touches.get(currentTouchIndex-1);
		}
		if (socket.enemyTouchIndex > 0) {
			enemyTouch = socket.enemyTouches.get(socket.enemyTouchIndex-1);
		}

		// check all bottle hitboxes
		for (Bottle beerBottle : generatedSprites.spawn(gameTimer)) {
			Hitbox hitbox = beerBottle.getHitbox(gameTimer, screenDrawer, devMode);

			// check touch hits with bottles
			if (touch.display && hitbox.left <= touch.x && touch.x <= hitbox.right) {
				if (hitbox.bottom <= touch.y && touch.y <= hitbox.top) {
					CaughtBottle caughtBottle = new CaughtBottle(beerBottle.bottleId, gameTimer, beerBottle.getXOffset(gameTimer), beerBottle.getYOffset(gameTimer), beerBottle.bottlePlayerId);
					latestCaughtBottle = caughtBottle;
					generatedSprites.caughtBottle(caughtBottle, false, devMode);
				}
			}

			// check enemy touch hits with bottles
			if (enemyTouch.display && hitbox.left <= screenWidth-enemyTouch.x && screenWidth-enemyTouch.x <= hitbox.right) {
				if (hitbox.bottom <= enemyTouch.y && enemyTouch.y <= hitbox.top) {
					CaughtBottle caughtBottle = new CaughtBottle(beerBottle.bottleId, gameTimer, beerBottle.getXOffset(gameTimer), beerBottle.getYOffset(gameTimer), beerBottle.bottlePlayerId);
					latestCaughtBottle = caughtBottle;
					generatedSprites.caughtBottle(caughtBottle, true, devMode);
				}
			}

			// check bottle hitbox with other bottles
			if (hitbox.left > 0 && hitbox.top > 0 && hitbox.left < screenWidth && hitbox.top < screenHeight) {
				for (Bottle compareBottle : generatedSprites.spawn(gameTimer)) {
					Hitbox obstacle = compareBottle.getHitbox(gameTimer, screenDrawer, devMode);

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
