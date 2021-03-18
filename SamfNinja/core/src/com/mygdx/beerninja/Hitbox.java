package com.mygdx.beerninja;

import com.badlogic.gdx.graphics.Texture;

public class Hitbox {
    double left;
    double top;
    double right;
    double bottom;
    Texture hitboxTexture;

    public Hitbox() {
        hitboxTexture = new Texture("touch.png");
    }

    private Double rotateX(double x, double y, double centerX, double centerY, double angle) {
        double originX = x - centerX;
        double originY = y - centerY;

        double rotatedX = Math.cos((angle/360)*6.28) * originX + Math.sin((angle/360)*6.28) * originY;
        return rotatedX + originX + x;
    }

    private Double rotateY(double x, double y, double centerX, double centerY, double angle) {
        double originX = x - centerX;
        double originY = y - centerY;

        double rotatedY = -Math.sin((angle/360)*6.28) * originX + Math.cos((angle/360)*6.28) * originY;
        return rotatedY + originY + y;
    }

    public void updateHitbox(double minX, double minY, double width, double height, double spin) {
        double maxX = minX + width;
        double maxY = minY + height;

        double rotatedMinX = rotateX(minX, minY, minX - width/2, minY - height/2, spin);
        double rotatedMinY = rotateY(minX, minY, minX - width/2, minY - height/2, spin);

        double rotatedMaxX = rotateX(maxX, maxY, maxX + width/2, maxY + height/2, spin);
        double rotatedMaxY = rotateY(maxX, maxY, maxX + width/2, maxY + height/2, spin);

        if (rotatedMaxX > rotatedMinX) {
            right = rotatedMaxX;
            left = rotatedMinX;
        }
        else {
            right = rotatedMinX;
            left = rotatedMaxX;
        }

        if (rotatedMaxY > rotatedMinY) {
            top = rotatedMaxY;
            bottom = rotatedMinY;
        }
        else {
            top = rotatedMinY;
            bottom = rotatedMaxY;
        }
    }

}
