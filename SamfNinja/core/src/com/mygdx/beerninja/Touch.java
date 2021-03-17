package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Touch {
    int id;
    public int x;
    public int y;
    public Texture texture;
    public boolean display;

    public Touch(int touchId, int screenX, int screenY) {
        id = touchId;
        x = screenX;
        y = Gdx.graphics.getHeight() - screenY;
        texture = new Texture("touch2.png");
        display = false;
    }
}
