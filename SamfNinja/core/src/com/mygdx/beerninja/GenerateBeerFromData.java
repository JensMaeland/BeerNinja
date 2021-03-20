package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GenerateBeerFromData {
    List<Bottle> bottles;
    Integer size;
    BeerSocket socket;

    public GenerateBeerFromData(ArrayList<JSONObject> input, BeerSocket inputSocket) {
        size = input.size();
        socket = inputSocket;

        List<Bottle> inputBottles = new ArrayList<>();
        try {
            for (JSONObject spriteData : input) {
                    int bottleId = (int) spriteData.get("id");
                    double beerSpawnTime = (double) spriteData.get("secondsToSpawn");
                    int yPos = (int) spriteData.get("offsetY");
                    String beerPlayer = (String) spriteData.get("playerID");
                    int bottleVelocity = (int) spriteData.get("velocity");
                    double bottleSpin = (double) spriteData.get("spin");
                    Bottle bottle = new Bottle(bottleId, beerPlayer, yPos, bottleVelocity, bottleSpin, beerSpawnTime, inputSocket.playerID);
                    inputBottles.add(bottle);
                }
            } catch (JSONException e) {
            e.printStackTrace();
        }

        bottles = inputBottles;
    }

    public List<Bottle> spawn(double gameTime) {
        List<Bottle> currentBottles = new ArrayList<>();

        for (Bottle bottle : bottles) {
            double beerSpawnTime = bottle.beerSpawnTime;

            if(gameTime > beerSpawnTime && gameTime < beerSpawnTime + 10) {
                currentBottles.add(bottle);
            }
        }

        // Play sound when spawning new bottles
        //if (beerBottles.size() > numberOfBottles) {
            //Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("pop.mp3"));
            //beerPop.play();
        //}

        return currentBottles;
    }

    public void caughtBottle(CaughtBottle caughtBottle, boolean devMode) {
        //float timestamp = System.currentTimeMillis();
        //caughtBottle.time = timestamp;

        for (Bottle bottle : bottles) {
            if (bottle.bottleId == caughtBottle.id) {
                bottles.remove(bottle);
                socket.sendCaughtBottle(caughtBottle);
                break;
            }
        }

        if (!devMode) {
        Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("break.mp3"));
        beerPop.play();
        }
    }
}
