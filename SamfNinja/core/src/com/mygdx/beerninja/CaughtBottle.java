package com.mygdx.beerninja;

public class CaughtBottle {
    public int id;
    public double time;
    public double xcoor;
    public double ycoor;
    public String playerID;

    public CaughtBottle(int bottleId, double timestamp, double xPos, double yPos, String playerId) {
        id = bottleId;
        time = timestamp;
        xcoor = xPos;
        ycoor = yPos;
        playerID = playerId;
    }
}
