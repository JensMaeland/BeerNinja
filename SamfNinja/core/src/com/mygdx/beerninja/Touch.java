package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Touch {
    public int id;
    public int x;
    public int y;
    Texture texture;
    public boolean display;
    public double time;

    public Touch(int touchId, int screenX, int screenY, double timestamp, boolean enemy) {
        id = touchId;
        x = screenX;
        y = Gdx.graphics.getHeight() - screenY;
        texture = getTexture(enemy);
        display = false;
        time = timestamp;
    }

    private Texture getTexture(boolean enemy) {
        if (!enemy) {
            return new Texture("touch2.png");
        }
        return new Texture("touch.png");
    }
}
