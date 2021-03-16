package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;

public class Bottle extends ApplicationAdapter {
    Texture beerTexture;
    Integer bottleId;
    float beerSpawnTime;
    Integer playerNumber;
    Integer xPos;
    Integer yPos;
    Integer bottleVelocity;
    final int screenWidth = 500;

    public Bottle(int id, int player, int y, int velocity, float spawnTime, int screenHeight) {
        bottleId = id;
        playerNumber = player;
        beerTexture = getTexture(player);
        beerSpawnTime = spawnTime;
        xPos = getXPos(player);
        yPos = screenHeight - y;
        bottleVelocity = velocity;
    }

    private Texture getTexture(int player) {
        if (player == 1) {
            return new Texture("pils.png");
        }
        else if (player == 2) {
            return new Texture("fatol.png");
        }

        return new Texture("");
    }

    private int getXPos(int player) {
        int margin = 80;

        if (player == 1) {
            return 0 - margin;
        }
        else if (player == 2) {
            return screenWidth + margin;
        }

        return 0;
    }

    public float getXOffset(float gameTime) {
        float offset = gameTime - beerSpawnTime;

        if (playerNumber == 1) {
            return xPos + offset*bottleVelocity;
        }
        else if (playerNumber == 2) {
            return xPos - offset*bottleVelocity;
        }
        return 0;
    }

    public float getYOffset(float gameTime) {
        float offset = (gameTime - beerSpawnTime)/2;

        return yPos - offset*bottleVelocity;
    }
}