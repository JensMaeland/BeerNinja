package com.mygdx.beerninja;

public class CaughtBottle {
    int id;
    double time;
    double xcoor;
    String playerId;

    public CaughtBottle(int bottleId, double timestamp, double xPos, String player) {
        id = bottleId;
        time = timestamp;
        xcoor = xPos;
        playerId = player;
    }
}
