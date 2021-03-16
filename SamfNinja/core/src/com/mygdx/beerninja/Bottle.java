package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;

public class Bottle extends ApplicationAdapter {
    Texture beerTexture;
    Integer bottleId;
    float beerSpawnTime;
    String playerString;
    String myPlayerString;
    Integer xPos;
    Integer yPos;
    Integer bottleVelocity;
    final int screenWidth = 500;

    public Bottle(int id, String bottlePlayer, int y, int velocity, float spawnTime, int screenHeight, String myPlayer) {
        bottleId = id;
        playerString = bottlePlayer;
        beerTexture = getTexture(bottlePlayer, myPlayer);
        beerSpawnTime = spawnTime;
        xPos = getXPos(bottlePlayer, myPlayer);
        yPos = screenHeight - y;
        bottleVelocity = velocity;
        myPlayerString = myPlayer;
    }

    private Texture getTexture(String player, String me) {
        if (player.equals(me)) {
            return new Texture("pils.png");
        }
        else {
            return new Texture("fatol.png");
        }
    }

    private int getXPos(String player, String me) {
        int margin = 80;

        if (player.equals(me)) {
            return 0 - margin;
        }
        else {
            return screenWidth + margin;
        }
    }

    public float getXOffset(float gameTime) {
        float offset = gameTime - beerSpawnTime;

        if (playerString.equals(myPlayerString)) {
            return xPos + offset*bottleVelocity;
        }
        else {
            return xPos - offset*bottleVelocity;
        }
    }

    public float getYOffset(float gameTime) {
        float offset = (gameTime - beerSpawnTime)/2;

        return yPos - offset*bottleVelocity;
    }
}