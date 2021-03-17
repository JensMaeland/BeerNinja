package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class SamfNinja extends ApplicationAdapter {
	SpriteBatch beerDrawer;
	BeerSocket socket;
	GenerateBeerFromData generatedSprites = null;
	List<Bottle> beerBottles = new ArrayList<>();
	List<Touch> touches = new ArrayList<>();
	private double gameTimer = -2;
	final double gameEndTime = 35;
	private boolean loading = false;

	@Override
	public void create () {
		// instancing a new batch drawer
		beerDrawer = new SpriteBatch();
		// connect the socket and receive generated sprites from the server
		socket = new BeerSocket();

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
				Texture text = new Texture("map2.png");
				float brightness = (float) Math.max(0.7, (gameTimer - Math.round(gameTimer)) + 0.5);
				beerDrawer.begin();
				beerDrawer.setColor(brightness, brightness, 1F, 1F);
				beerDrawer.draw(text, 0, 0);
				beerDrawer.setColor(1F, 1F, 1F, 1F);
				beerDrawer.end();

				renderBeerSprites();
				renderUserTouches();
				socket.getPoints();
				// System.out.println(socket.points);
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
				if (screenY >= 515 && screenY <= 615) {
					socket.setUpGame();
					generatedSprites = socket.generateSprites();
				}
				// solo game clicked
				else if (screenY >= 615 && screenY <= 715) {
					//	socket.setUpGame( solo );

				}
				return true;
			};
		});
	}

	private void renderBeerSprites() {
		int numberOfBottles = beerBottles.size();
		beerBottles.clear();

		beerBottles = generatedSprites.spawn(gameTimer);
		for (Bottle beerBottle : beerBottles) {
			double x = beerBottle.getXOffset(gameTimer);
			double y = beerBottle.getYOffset(gameTimer);

			beerDrawer.begin();
			beerDrawer.draw(beerBottle.beerTexture, (float) x, (float) y);
			beerDrawer.end();
		}

		if (beerBottles.size() > numberOfBottles) {
			//Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("pop.mp3"));
			//beerPop.play();
		}
	}

	private void renderUserTouches() {
		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				Touch touch = new Touch(screenX, screenY);
				touches.add(touch);
				checkHitboxes(touch);
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				touches.clear();
				return true;
			}
		});

		for (Touch touch : touches) {
			beerDrawer.begin();
			beerDrawer.draw(new Texture("touch2.png"), touch.x, touch.y);
			beerDrawer.end();
		}
	}

	private void checkHitboxes(Touch touch) {
		int beerWidth = 65;
		int beerHeight = 200;

		for (Bottle beerBottle : beerBottles) {
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
