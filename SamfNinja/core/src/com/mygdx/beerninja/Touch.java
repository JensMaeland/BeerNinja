package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;

public class Touch {
    int x;
    int y;

    public Touch(int screenX, int screenY) {
        x = screenX;
        y = Gdx.graphics.getHeight() - screenY;
    }

}
