package com.mygdx.beerninja;

public class Hitbox {
    double left;
    double top;
    double right;
    double bottom;

    public Double rotateX(double x, double y, double centerX, double centerY, double angle) {
        double originX = x - centerX;
        double originY = y - centerY;

        double rotatedX = Math.cos((angle/360)*6.28) * originX + Math.sin((angle/360)*6.28) * originY;
        return rotatedX + originX + x;
    }

    public Double rotateY(double x, double y, double centerX, double centerY, double angle) {
        double originX = x - centerX;
        double originY = y - centerY;

        double rotatedY = -Math.sin((angle/360)*6.28) * originX + Math.cos((angle/360)*6.28) * originY;
        return rotatedY + originY + y;
    }

}
