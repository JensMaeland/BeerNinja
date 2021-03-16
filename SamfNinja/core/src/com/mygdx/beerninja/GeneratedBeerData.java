package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeneratedBeerData {
    List<JSONObject> data;
    Integer size;
    BeerSocket socket;

    public GeneratedBeerData(ArrayList<JSONObject> input, BeerSocket inputSocket) {
        data = input;
        size = input.size();
        socket = inputSocket;
    }

    public List<Bottle> spawn(double gameTime, int screenHeight) {
        List<Bottle> bottles = new ArrayList<>();
        String player = socket.player;

        System.out.println(player);

        try {
            for (JSONObject spriteData : data) {
                double beerSpawnTime = (double) spriteData.get("secondsToSpawn");

                if(gameTime > beerSpawnTime) {
                    int bottleId = (int) spriteData.get("id");
                    int yPos = (int) spriteData.get("offsetY");
                    String beerPlayer = (String) spriteData.get("player");
                    int bottleVelocity = (int) spriteData.get("velocity");

                    Bottle bottle = new Bottle(bottleId, beerPlayer, yPos, bottleVelocity, beerSpawnTime, screenHeight, player);
                    bottles.add(bottle);
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return bottles;
    }

    public void caughtBottle(int id, double xPos) {
        JSONObject slicedBeer = null;
        
        for (JSONObject spriteData : data) {
            try {
                int bottleId = (int) spriteData.get("id");
                if (bottleId == id) {
                    slicedBeer = spriteData;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("break.mp3"));
        beerPop.play();

        socket.caughtBottle(id, xPos);
        data.remove(slicedBeer);
    }
}
