package com.mygdx.beerninja;

public class CaughtBottle {
    public int id;
    public double time;
    public double xcoor;
    public String playerID;

    public CaughtBottle(int bottleId, double timestamp, double xPos, String playerId) {
        id = bottleId;
        time = timestamp;
        xcoor = xPos;
        playerID = playerId;
    }
}
