package com.mygdx.beerninja;

public class CaughtBottle {
    public int id;
    public double time;
    public double xcoor;
    public String playerId;

    public CaughtBottle(int bottleId, double timestamp, double xPos, String player) {
        id = bottleId;
        time = timestamp;
        xcoor = xPos;
        playerId = player;
    }
}
