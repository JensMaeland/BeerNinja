package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.List;

public class SamfNinja extends ApplicationAdapter {
	SpriteBatch beerDrawer;
	Socket socket;
	GeneratedBeerData generatedSprites;
	private float timer = -2;
	private float gameEndTime = 5000;
	private int numberOfBottles;

	@Override
	public void create () {
		// instancing a new batch drawer
		beerDrawer = new SpriteBatch();
		// connect the socket and receive generated sprites from the server
		socket = new Socket();
		generatedSprites = socket.generateSprites();

		// play sound to start off the game
		Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("crack.mp3"));
		beerPop.play();
	}

	@Override
	public void render () {
		timer += Gdx.graphics.getDeltaTime();

		beerDrawer.begin();
		beerDrawer.draw(new Texture("map1.png"), 0, 0);
		beerDrawer.end();
		createBeerSprites();
	}

	private void createBeerSprites() {
		if (numberOfBottles == generatedSprites.size() - 1) {
			gameEndTime = timer + 5;
		}

		if (timer >= gameEndTime) {
			gameOver();
			return;
		}

		List<Bottle> beerBottles = generatedSprites.spawn(timer);
		numberOfBottles = beerBottles.size();

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
