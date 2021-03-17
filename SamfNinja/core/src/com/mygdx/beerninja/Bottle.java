package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;

public class Bottle extends ApplicationAdapter {
    Texture beerTexture;
    Integer bottleId;
    double beerSpawnTime;
    String playerString;
    String myPlayerString;
    Integer xStartPos;
    Integer yStartPos;
    Integer bottleVelocity;
    final int screenWidth = 500;

    public Bottle(int id, String bottlePlayer, int y, int velocity, Double spawnTime, int screenHeight, String myPlayer) {
        bottleId = id;
        playerString = bottlePlayer;
        beerTexture = getTexture(bottlePlayer, myPlayer);
        beerSpawnTime = spawnTime;
        xStartPos = getXPos(bottlePlayer, myPlayer);
        yStartPos = screenHeight - y;
        bottleVelocity = velocity;
        myPlayerString = myPlayer;
    }

    private Texture getTexture(String player, String me) {
        if (player.equals(me)) {
            return new Texture("pils.png");
        }
        else {
            return new Texture("dag.png");
        }
    }

    private int getXPos(String player, String me) {
        int margin = 80;

        if (player.equals(me)) {
            return -margin;
        }
        else {
            return screenWidth + margin;
        }
    }

    public double getXOffset(Double gameTime) {
        double offset = gameTime - beerSpawnTime;

        if (playerString.equals(myPlayerString)) {
            return xStartPos + offset*bottleVelocity;
        }
        else {
            return xStartPos - offset*bottleVelocity;
        }
    }

    public double getYOffset(Double gameTime) {
        double offset = (gameTime - beerSpawnTime)/2;
        return yStartPos - offset*bottleVelocity;
    }
}