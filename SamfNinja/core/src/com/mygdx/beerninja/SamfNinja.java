package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.List;

public class SamfNinja extends ApplicationAdapter {
	SpriteBatch beerDrawer;
	BeerSocket socket;
	GenerateBeerFromData generatedSprites = null;
	HashMap<Integer, Touch> touches = new HashMap<>();
	private double gameTimer = -2;
	final double gameEndTime = 35;
	final double powerUpTimer = 20;
	final int swordLength = 35;
	final int beerWidth = 65;
	final int beerHeight = 200;
	private boolean loading = false;
	int touchNumber = 0;
	Texture background;
	BitmapFont font;

	@Override
	public void create () {
		// instancing a new batch drawer
		beerDrawer = new SpriteBatch();
		// connect the socket and receive generated sprites from the server
		socket = new BeerSocket();

		font = new BitmapFont();

		// instancing objects for touch feature
		for (int i = 0; i < swordLength; i++) {
			touches.put(i, new Touch(i, 0, 0));
		}

		// adding background image
		background = new Texture("map2.png");

		// play sound to start off the game
		Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("crack.mp3"));
		beerPop.play();
	}

	@Override
	public void render () {
		if (generatedSprites == null) {
			mainMenu();
		}
		else {
			gameTimer += Gdx.graphics.getDeltaTime();
			
			if (gameTimer < gameEndTime) {
				float brightness = (float) Math.max(0.6, (gameTimer - Math.round(gameTimer)) + 0.5);
				beerDrawer.begin();
				beerDrawer.setColor(brightness, brightness, 1F, 1F);
				beerDrawer.draw(background, 0, 0);
				beerDrawer.setColor(1F, 1F, 1F, 1F);

				font.draw(beerDrawer, Integer.toString(socket.myPoints), 50, Gdx.graphics.getHeight() - 50);
				font.draw(beerDrawer, Integer.toString(socket.enemyPoints), 490, Gdx.graphics.getHeight() - 50);
				beerDrawer.end();

				renderBeerSprites();
				renderUserTouches();
				socket.getPoints();
			}
			else {
				gameOver();
			}
		}
	}

	public void mainMenu () {
		beerDrawer.begin();
		if (!loading) {
			beerDrawer.draw(new Texture("home.png"), 0, 0);
		}
		else {
			beerDrawer.draw(new Texture("loading.png"), 0, 0);
		}
		beerDrawer.end();

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
				return true;
			};
		});
	}

	private void renderPlayerScores() {

	}

	private void renderBeerSprites() {
		List<Bottle> beerBottles = generatedSprites.spawn(gameTimer);

		for (Bottle beerBottle : beerBottles) {
			double minX = beerBottle.getXOffset(gameTimer);
			double minY = beerBottle.getYOffset(gameTimer);

			for (Bottle compareBottle : beerBottles) {
				double compareX = compareBottle.getXOffset(gameTimer);
				double compareY = compareBottle.getYOffset(gameTimer);

				if (beerBottle != compareBottle) {
					if (minX <= compareX + beerWidth && compareX <= minX + beerWidth) {
						if (minY <= compareY + beerHeight && compareY <= minY + beerHeight) {
							beerBottle.xStartPos = (int) minX;
							beerBottle.collision = true;
						}
					}
				}
			}

			TextureRegion region = new TextureRegion(beerBottle.beerTexture);

			beerDrawer.begin();
			beerDrawer.draw(region, (float) minX, (float) minY,  (float) beerWidth/2,  (float) beerHeight/2, beerWidth, beerHeight, 1, 1, (float) beerBottle.getSpin(gameTimer));
			beerDrawer.end();
		}
	}

	private void renderUserTouches() {
		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				Touch touch = touches.get(touchNumber);
				touch.x = screenX;
				touch.y = Gdx.graphics.getHeight() - screenY;
				touch.display = true;
				checkTouchHitboxes(touch);
				touchNumber = (touchNumber + 1) % swordLength;
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				for (int i = 0; i < swordLength; i++) {
					Touch touch = touches.get(i);
					touch.display = false;
				}
				return true;
			}
		});

		Touch prevTouch = null;
		for (Touch touch : touches.values()) {
			if (touch.display) {
				beerDrawer.begin();

				if (prevTouch != null) {
					float deltaX = (float) touch.x - prevTouch.x;
					float deltaY = (float) touch.y - prevTouch.y;

					while ((Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) && prevTouch.x != 0 && touch.id - prevTouch.id == 1) {
						beerDrawer.draw(touch.texture, touch.x-deltaX/2, touch.y-deltaY/2);
						beerDrawer.draw(touch.texture, prevTouch.x+deltaX/2, prevTouch.y+deltaY/2);
						deltaX = Math.signum(deltaX)*Math.max(Math.abs(deltaX) - 2, 2);
						deltaY = Math.signum(deltaY)*Math.max(Math.abs(deltaY) - 2, 2);
					}
				}
				prevTouch = touch;

				beerDrawer.draw(touch.texture, touch.x, touch.y);
				beerDrawer.end();
			}
		}
	}

	private void checkTouchHitboxes(Touch touch) {
		for (Bottle beerBottle : generatedSprites.spawn(gameTimer)) {
			double minX = beerBottle.getXOffset(gameTimer);
			double minY = beerBottle.getYOffset(gameTimer);

			if (minX <= touch.x && touch.x <= minX + beerWidth) {
				if (minY <= touch.y && touch.y <= minY + beerHeight) {
					generatedSprites.caughtBottle(beerBottle.bottleId, minX);
				}
			}
		}
	}

	public void gameOver() {
		loading = false;

		beerDrawer.begin();
		beerDrawer.draw(new Texture("gameOver.png"), 50, 500);
		beerDrawer.end();

		if (gameTimer > gameEndTime + 3) {
			generatedSprites = null;
		}
	}
	
	@Override
	public void dispose () {
		beerDrawer.dispose();
	}
}
