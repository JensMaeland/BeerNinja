package com.mygdx.beerninja;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;

public class SamfNinja extends ApplicationAdapter {
	// imported help-classes
	SpriteBatch screenDrawer;
	Texture background;
	Texture powerupBackground;
	Texture gameOverTexture;
	TextureRegion splash;
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
	int scale;
	// game-settings as variables
	boolean devMode = false; // set devMode for easier testing
	double gameTimer = -2; // set seconds until game start
	final double gameEndTime = 38; // set duration of each game
	final int tailLength = 35; // set length of tail when touching the screen
	final String backgroundImage = "map2.png"; // set which background-image should display
	final String powerupBackgroundImage = "map3.png"; // set which background-image should display

	@Override
	public void create () {
		// instancing a new batch drawer
		screenDrawer = new SpriteBatch();
		//
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		scale = Gdx.graphics.getWidth() / 540;
		// instancing a new font drawer
		FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("DelaGothicOne-Regular.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		fontParameter.size = (int) (Gdx.graphics.getWidth() * 0.05);
		font = fontGenerator.generateFont(fontParameter);
		font.setColor(1,1,0.2F,1);
		fontGenerator.dispose();
		// instancing the main menu
		mainMenu = new MainMenu();
		// connect the socket and receive generated sprites from the server
		socket = new BeerSocket(tailLength);
		socket.connect();

		// instancing objects for touch feature
		for (int i = 0; i < tailLength; i++) {
			touches.put(i, new Touch(i, 0, 0, false));
		}

		// setting textures to correct images
		background = new Texture(backgroundImage);
		powerupBackground = new Texture(powerupBackgroundImage);
		gameOverTexture = new Texture("gameOver.png");
		splash = new TextureRegion(new Texture("splash.png"));

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
		if (devMode) {
			brightness = 0.8F;
		}
		else if (gameTimer > generatedSprites.powerUpTimer + 1) {
			brightness = (float) Math.max(0.1, (gameTimer - Math.round(gameTimer)) + 0.5);
		}
		else {
			brightness = (float) Math.max(0.6, (gameTimer - Math.round(gameTimer)) + 0.5);
		}

		screenDrawer.begin();
		screenDrawer.setColor(brightness, brightness, 1F, 1F);
		if (gameTimer > generatedSprites.powerUpTimer + 1) {
			screenDrawer.draw(powerupBackground, 0, 0, screenWidth, screenHeight);
		}
		else {
			screenDrawer.draw(background, 0, 0, screenWidth, screenHeight);
		}
		screenDrawer.setColor(1F, 1F, 1F, 1F);

		int fontOffset = scale*15;
		if (!multiplayer) {
			font.draw(screenDrawer, "Poeng: " + socket.myPoints, fontOffset, screenHeight - fontOffset);
		}
		else {
			font.draw(screenDrawer, "Meg: " + socket.myPoints, fontOffset, screenHeight - fontOffset);
			font.draw(screenDrawer, "Motspiller: " + socket.enemyPoints, screenWidth-fontOffset*20, screenHeight - fontOffset);
		}

		if (latestCaughtBottle != null && latestCaughtBottle.time > gameTimer - 5 && !devMode) {
			brightness = (float) (1.0-(gameTimer - latestCaughtBottle.time));
			screenDrawer.setColor(brightness, brightness, brightness, brightness);
			screenDrawer.draw(splash, (float) latestCaughtBottle.xcoor - 80, (float) latestCaughtBottle.ycoor + 40, 0, 0, splash.getRegionWidth()*scale, splash.getRegionHeight()*scale, 1, 1, 0F);

			screenDrawer.setColor(1F, 1F, 1F, 1F);
		}
		screenDrawer.end();
	}

	private void renderBeerSprites() {
		//offsetX
		for (Bottle beerBottle : generatedSprites.spawn(gameTimer)) {
			float x = (float) beerBottle.getXOffset(gameTimer);
			float y = (float) beerBottle.getYOffset(gameTimer);
			int width = beerBottle.beerTexture.getRegionWidth();
			int height = beerBottle.beerTexture.getRegionHeight();

			screenDrawer.begin();
			screenDrawer.draw(beerBottle.beerTexture, x, y, (width*scale)/2F, (height*scale)/2F, width*scale, height*scale, 1, 1, (float) beerBottle.getSpin(gameTimer));
			screenDrawer.end();
		}
	}

	private void getAndRenderUserTouches() {
		final int scaleX = Gdx.graphics.getWidth() / 100;
		final int scaleY = Gdx.graphics.getHeight() / 100;

		// get touches
		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {

				Touch touch = touches.get(currentTouchIndex);
				touch.x = screenX/scaleX;
				touch.y = screenY/scaleY;
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
		int prevX = 0;
		int prevY = 0;
		for (Touch touch : touches.values()) {
			if (touch.display) {
				screenDrawer.begin();

				int touchX = touch.x*scaleX;
				int touchY = screenHeight-touch.y*scaleY;

				if (prevX > 0) {
					float deltaX = (float) touchX - prevX;
					float deltaY = (float) touchY - prevY;

					while ((Math.abs(deltaX) > 10 && Math.abs(deltaX) < 80) || (Math.abs(deltaY) > 10 && Math.abs(deltaY) < 80)) {
						screenDrawer.draw(touch.texture, touchX-deltaX/2, touchY-deltaY/2);
						screenDrawer.draw(touch.texture, prevX+deltaX/2, prevY+deltaY/2);
						deltaX = Math.signum(deltaX)*Math.max(Math.abs(deltaX) - 2, 2);
						deltaY = Math.signum(deltaY)*Math.max(Math.abs(deltaY) - 2, 2);
					}
				}
				prevX = touchX;
				prevY = touchY;

				screenDrawer.draw(touch.texture, touchX, touchY);
				screenDrawer.end();
			}
		}

		// render enemy touches
		if (multiplayer) {
			for (Touch touch : socket.enemyTouches.values()) {
				if (touch.display) {
					screenDrawer.begin();
					screenDrawer.draw(touch.texture, screenWidth-touch.x*scaleX, screenHeight-touch.y*scaleY);
					screenDrawer.end();
				}
			}
		}
	}

	private void checkHitboxes() {
		// get latest touch object for player and enemy
		Touch touch = touches.get(tailLength-1);
		if (currentTouchIndex > 0) {
			touch = touches.get(currentTouchIndex-1);
		}

		float timestamp = System.currentTimeMillis();

		int scaleX = Gdx.graphics.getWidth() / 100;
		int scaleY = Gdx.graphics.getHeight() / 100;
		int touchX = touch.x*scaleX;
		int touchY = screenHeight-touch.y*scaleY;

		// check all bottle hitboxes
		for (Bottle beerBottle : generatedSprites.spawn(gameTimer)) {
			Hitbox hitbox = beerBottle.getHitbox(gameTimer, screenDrawer, devMode, scale);

			// check touch hits with bottles
			if (touch.display && hitbox.left <= touchX && touchX <= hitbox.right) {
				if (hitbox.bottom <= touchY && touchY <= hitbox.top) {
					CaughtBottle caughtBottle = new CaughtBottle(beerBottle.bottleId, gameTimer, beerBottle.getXOffset(gameTimer), beerBottle.getYOffset(gameTimer), beerBottle.bottlePlayerId);
					latestCaughtBottle = caughtBottle;
					generatedSprites.caughtBottle(caughtBottle, socket, devMode);
				}
			}

			// check bottle hitbox with other bottles
			if (hitbox.left > 0 && hitbox.top > 0 && hitbox.left < screenWidth && hitbox.top < screenHeight) {
				for (Bottle compareBottle : generatedSprites.spawn(gameTimer)) {
					Hitbox obstacle = compareBottle.getHitbox(gameTimer, screenDrawer, devMode, scale);

					if (beerBottle != compareBottle && !beerBottle.collision && !beerBottle.bottlePlayerId.equals(compareBottle.bottlePlayerId) && beerBottle.bottleId < 69 && compareBottle.bottleId < 69) {
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
		socket.gameSummary();

		screenDrawer.begin();
		screenDrawer.draw(gameOverTexture, 50, 500, screenWidth-100, screenHeight/4);
		screenDrawer.end();

		if (gameTimer > gameEndTime + 5) {
			generatedSprites = null;
			gameTimer = -2;
		}
	}
	
	@Override
	public void dispose () {
		screenDrawer.dispose();
	}
}
