package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.Text;

public class SamfNinja extends ApplicationAdapter {
	SpriteBatch beerDrawer;
	Socket socket;
	GeneratedBeerData generatedSprites;
	List<Touch> touches;
	private float timer = -2;
	private float gameEndTime = 5000;
	private int numberOfBottles;
	private int screenHeight = 1100;

	@Override
	public void create () {
		// instancing a new batch drawer
		beerDrawer = new SpriteBatch();
		// connect the socket and receive generated sprites from the server
		socket = new Socket();
		generatedSprites = socket.generateSprites();
		// instancing empty list for touches
		touches = new ArrayList<>();

		// play sound to start off the game
		Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("crack.mp3"));
		beerPop.play();

		Gdx.input.setInputProcessor(new InputAdapter(){
		//	@Override
		//	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		//		System.out.println("touchDown");
		//		return true;
		//	};

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				touches.clear();
				return true;
			};

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				Touch touch = new Touch(screenX, screenY);
				touches.add(touch);

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
		createBeerSprites();

		for (Touch touch : touches) {
			beerDrawer.begin();
			beerDrawer.draw(new Texture("touch.png"), touch.x, screenHeight - touch.y);
			beerDrawer.end();
		}
	}

	private void createBeerSprites() {
		if (numberOfBottles == generatedSprites.size() - 1) {
			gameEndTime = timer + 5;
		}

		if (timer >= gameEndTime) {
			gameOver();
			return;
		}

		List<Bottle> beerBottles = generatedSprites.spawn(timer, screenHeight);
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
