package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.List;

public class GeneratedBeerData {
    List<List<Integer>> data;
    Integer size;
    BeerSocket socket;

    public GeneratedBeerData(List<List<Integer>> input, BeerSocket inputSocket) {
        data = input;
        size = input.size();
        socket = inputSocket;
    }

    public List<Bottle> spawn(float gameTime, int screenHeight) {
        List<Bottle> bottles = new ArrayList<>();

        for (List<Integer> spriteData : data) {
            float beerSpawnTime = spriteData.get(1);

            if(gameTime > beerSpawnTime) {
                int bottleId = spriteData.get(0);
                int yPos = spriteData.get(2);
                int beerPlayer = spriteData.get(3);
                int bottleVelocity = spriteData.get(4);

                Bottle bottle = new Bottle(bottleId, beerPlayer, yPos, bottleVelocity, beerSpawnTime, screenHeight);
                bottles.add(bottle);
            }
        }

        return bottles;
    }

    public void caughtBottle(int id, float xPos) {
        List<Integer> slicedBeer = null;
        
        for (List<Integer> spriteData : data) {
            int bottleId = spriteData.get(0);
            if (bottleId == id) {
                slicedBeer = spriteData;    
            }
        }

        Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("break.mp3"));
        beerPop.play();

        socket.caughtBottle(id, xPos);
        data.remove(slicedBeer);
    }
}
