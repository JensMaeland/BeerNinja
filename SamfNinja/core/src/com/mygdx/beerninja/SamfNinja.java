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
	SpriteBatch batch;
	Socket socket;
	List<List<Integer>> generatedSprites;
	private float timer;
	private float gameEndTime = 5000;
	final HashMap<Integer, Bottle> bottles = new HashMap<>();

	@Override
	public void create () {
		Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("crack.mp3"));
		beerPop.play();
		batch = new SpriteBatch();
		socket = new Socket();
		generatedSprites = socket.generateSprites();
		timer=-2;
	}

	private void drawBottle(int index) {
		Bottle bottle = bottles.get(index);
		batch.begin();
		batch.draw(bottle.beerTexture, bottle.getXOffset(timer), bottle.getYOffset(timer));
		batch.end();
	}

	private void createBeerSprites() {
		int numberOfBeer = bottles.size();

		if (numberOfBeer == generatedSprites.size() - 1) {
			gameEndTime = timer + 5;
		}

		if (timer>=gameEndTime) {
			gameOver();
			return;
		}

		for (int i = 0; i < generatedSprites.size(); i++) {
			List<Integer> beer = generatedSprites.get(i);

			float beerSpawnTime = beer.get(0)/2;
			if(timer>=beerSpawnTime){
				int yPos = beer.get(1);
				int beerPlayer = beer.get(2);
				int bottleVelocity = beer.get(3);
				Bottle bottle = new Bottle(beerPlayer, yPos, bottleVelocity, beerSpawnTime);

				bottles.put(i, bottle);
			}
		}

		if (bottles.size() > numberOfBeer) {
			//Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("pop.mp3"));
			//beerPop.play();
		}

		for (int i = 0; i < bottles.size(); i++) {
			drawBottle(i);
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		timer += Gdx.graphics.getDeltaTime();

		batch.begin();
		batch.draw(new Texture("map1.png"), 0, 0);
		batch.end();
		createBeerSprites();
	}

	public void gameOver() {
		batch.begin();
		batch.draw(new Texture("gameOver.png"), 50, 500);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
