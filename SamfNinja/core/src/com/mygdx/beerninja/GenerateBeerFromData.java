package com.mygdx.beerninja;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GenerateBeerFromData {
    List<Bottle> bottles;
    List<Bottle> powerupBottles;
    Bottle powerup;
    Integer size;
    AssetManager assetManager;
    final double powerUpTimer = 18; // set when the powerUp should spawn

    public GenerateBeerFromData(ArrayList<JSONObject> bottleInput, ArrayList<JSONObject> powerupInput, String playerID, int scale) {
        size = bottleInput.size();

        List<Bottle> inputBottles = new ArrayList<>();
        List<Bottle> powerupInputBottles = new ArrayList<>();
        try {
            for (JSONObject spriteData : bottleInput) {
                    int bottleId = (int) spriteData.get("id");
                    double beerSpawnTime = (double) spriteData.get("secondsToSpawn");
                    int yPos = (int) spriteData.get("offsetY");
                    String beerPlayer = (String) spriteData.get("playerID");
                    int bottleVelocity = (int) spriteData.get("velocity");
                    double bottleSpin = (double) spriteData.get("spin");
                    Bottle bottle = new Bottle(bottleId, beerPlayer, yPos, bottleVelocity, bottleSpin, beerSpawnTime, scale, playerID);
                    inputBottles.add(bottle);
                }
            for (JSONObject spriteData : powerupInput) {
                int bottleId = (int) spriteData.get("id");
                double beerSpawnTime = (double) spriteData.get("secondsToSpawn");
                int yPos = (int) spriteData.get("offsetY");
                String beerPlayer = (String) spriteData.get("playerID");
                int bottleVelocity = (int) spriteData.get("velocity");
                double bottleSpin = (double) spriteData.get("spin");
                Bottle bottle = new Bottle(bottleId, beerPlayer, yPos, bottleVelocity, bottleSpin, beerSpawnTime + powerUpTimer + 1, scale, playerID);
                powerupInputBottles.add(bottle);
            }
            } catch (JSONException e) {
            e.printStackTrace();
        }
        bottles = inputBottles;
        powerupBottles = powerupInputBottles;

        powerup = new Bottle(69, "420", scale*150, 500, 1, powerUpTimer, scale, playerID);
        bottles.add(powerup);

        assetManager = new AssetManager();
        assetManager.load("break.mp3", Sound.class);
        assetManager.finishLoading();
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

    public void caughtBottle(CaughtBottle caughtBottle, BeerSocket socket, boolean devMode) {
        for (Bottle bottle : bottles) {
            if (bottle.bottleId == caughtBottle.id) {
                bottles.remove(bottle);
                if (caughtBottle.id != 69) {
                    socket.sendCaughtBottle(caughtBottle);
                }
                else {
                    bottles.addAll(powerupBottles);
                }
                break;
            }
        }

        if (!devMode && assetManager.isLoaded("break.mp3")) {
            Sound sound = assetManager.get("break.mp3", Sound.class);
            sound.play();
        }
    }

    public void caughtBottle(CaughtBottle caughtBottle, SamfNinja game) {
        for (Bottle bottle : bottles) {
            if (bottle.bottleId == caughtBottle.id) {
                bottles.remove(bottle);
                caughtBottle.xcoor = bottle.getXOffset(game.gameTimer);
                caughtBottle.ycoor = bottle.getYOffset(game.gameTimer);
                game.latestCaughtBottle = caughtBottle;
                break;
            }
        }
    }
}
