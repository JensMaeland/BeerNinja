package com.mygdx.beerninja;

public class GameMode {
    String name;
    String description;
    boolean multiplayer;
    boolean devMode;

    public GameMode(String gameName, String gameDescription, boolean multiplayerGame, boolean devModeGame) {
        name = gameName;
        description = gameDescription;
        multiplayer = multiplayerGame;
        devMode = devModeGame;
    }
}
