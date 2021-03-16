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
	GeneratedBeerData generatedSprites;
	List<Bottle> beerBottles = new ArrayList<>();
	List<Touch> touches = new ArrayList<>();
	private float timer = -2;
	private float gameEndTime = 5000;
	private int screenHeight = 1050;

	@Override
	public void create () {
		// instancing a new batch drawer
		beerDrawer = new SpriteBatch();
		// connect the socket and receive generated sprites from the server
		socket = new BeerSocket();
		socket.connect();
		generatedSprites = socket.generateSprites();

		// play sound to start off the game
		Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("crack.mp3"));
		beerPop.play();

		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				touches.clear();
				return true;
			};

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				Touch touch = new Touch(screenX, screenHeight - screenY);
				touches.add(touch);
				checkHitboxes(touch);
				return false;
			};
		});
	}

	@Override
	public void render () {
		timer += Gdx.graphics.getDeltaTime();

		beerDrawer.begin();
		beerDrawer.draw(new Texture("map1.png"), 0, 0);
		beerDrawer.end();
		renderBeerSprites();
		renderUserTouches();
	}

	private void renderBeerSprites() {
		int numberOfBottles = beerBottles.size();

		if (numberOfBottles + 1 == generatedSprites.size) {
			gameEndTime = timer + 5;
		}

		if (timer >= gameEndTime) {
			gameOver();
			return;
		}

		beerBottles = generatedSprites.spawn(timer, screenHeight);
		for (Bottle beerBottle : beerBottles) {
			beerDrawer.begin();
			beerDrawer.draw(beerBottle.beerTexture, beerBottle.getXOffset(timer), beerBottle.getYOffset(timer));
			beerDrawer.end();
		}

		if (beerBottles.size() > numberOfBottles) {
			//Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("pop.mp3"));
			//beerPop.play();
		}
	}

	private void renderUserTouches() {
		for (Touch touch : touches) {
			beerDrawer.begin();
			beerDrawer.draw(new Texture("touch.png"), touch.x, touch.y);
			beerDrawer.end();
		}
	}

	private void checkHitboxes(Touch touch) {
		int beerWidth = 65;
		int beerHeight = 200;

		for (Bottle beerBottle : beerBottles) {
			float minX = beerBottle.getXOffset(timer);
			float minY = beerBottle.getYOffset(timer);
			float maxX = minX + beerWidth;
			float maxY = minY + beerHeight;

			if (minX <= touch.x && touch.x <= maxX) {
				if (minY <= touch.y && touch.y <= maxY) {
					generatedSprites.caughtBottle(beerBottle.bottleId, minX);
				}
			}
		}
	}

	public void gameOver() {
		beerDrawer.begin();
		beerDrawer.draw(new Texture("gameOver.png"), 50, 500);
		beerDrawer.end();
	}
	
	@Override
	public void dispose () {
		beerDrawer.dispose();
	}
}
