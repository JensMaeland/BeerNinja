package com.mygdx.beerninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Bottle extends ApplicationAdapter {
    TextureRegion beerTexture;
    Integer bottleId;
    double beerSpawnTime;
    String bottlePlayerId;
    String myPlayerId;
    Integer xStartPos;
    Integer yStartPos;
    Integer bottleVelocity;
    double bottleSpin;
    boolean collision;
    final int screenWidth = 500;
    Hitbox hitbox;

    public Bottle(int id, String bottlePlayer, int y, int velocity, double spin, double spawnTime, String myPlayer) {
        bottleId = id;
        bottlePlayerId = bottlePlayer;
        beerTexture = getTexture(bottlePlayer, myPlayer);
        beerSpawnTime = spawnTime;
        xStartPos = getXPos(bottlePlayer, myPlayer);
        yStartPos = Gdx.graphics.getHeight() - y;
        bottleVelocity = velocity;
        bottleSpin = spin;
        collision = false;
        myPlayerId = myPlayer;
        hitbox = new Hitbox();
    }

    private TextureRegion getTexture(String player, String me) {
        Texture beerTexture;

        if (player.equals(me)) {
            beerTexture = new Texture("pils.png");
        }
        else {
            beerTexture = new Texture("dag.png");
        }
        return new TextureRegion(beerTexture);
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

    public double getXOffset(double gameTime) {
        double offset = gameTime - beerSpawnTime;

        if (bottlePlayerId.equals(myPlayerId)) {
            if (!collision) {
               return xStartPos + offset*bottleVelocity;
            }
            return xStartPos + offset*50;
        }
        else {
            if (!collision) {
                return xStartPos - offset*bottleVelocity;
            }
            return xStartPos - offset*50;
        }
    }

    public double getYOffset(Double gameTime) {
        double offset = (gameTime - beerSpawnTime)/2;
        return yStartPos - offset*bottleVelocity;
    }

    public Hitbox getHitbox(double gameTime, SpriteBatch screenDrawer, boolean devMode) {
        int beerWidth = beerTexture.getRegionWidth();
        int beerHeight = beerTexture.getRegionHeight();

        double minX = getXOffset(gameTime);
        double minY = getYOffset(gameTime);

        double spinAngle = getSpin(gameTime);

        hitbox.updateHitbox(minX, minY, beerWidth, beerHeight, spinAngle);

        // draw the hitboxes in devMode
        if (devMode) {
            screenDrawer.begin();
            screenDrawer.draw(hitbox.hitboxTexture, (int) (hitbox.left), (int) (hitbox.top));
            screenDrawer.draw(hitbox.hitboxTexture, (int) (hitbox.right), (int) (hitbox.bottom));
            screenDrawer.draw(hitbox.hitboxTexture, (int) (hitbox.left), (int) (hitbox.bottom));
            screenDrawer.draw(hitbox.hitboxTexture, (int) (hitbox.right), (int) (hitbox.top));
            screenDrawer.end();
        }

        return hitbox;
    }

    public double getSpin(Double gameTime) {
        double offset = gameTime - beerSpawnTime;

        if (bottlePlayerId.equals(myPlayerId)) {
            if (!collision) {
                return -offset*bottleSpin*200;
            }
            return bottleSpin*50;
        }
        else {
            if (!collision) {
                return offset*bottleSpin*200;
            }
            return -bottleSpin*50;
        }
    }

}