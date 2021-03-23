package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainMenu {
    Texture homeScreen;
    Texture loadingScreen;
    AssetManager assetManager;

    public MainMenu() {
        homeScreen = new Texture("home2.png");
        loadingScreen = new Texture("loading.png");

        assetManager = new AssetManager();
        assetManager.load("crack.mp3", Sound.class);
        assetManager.load("theMidnight.mp3", Music.class);
        assetManager.finishLoading();
    }

    public void renderMainMenu (final SamfNinja game, final BeerSocket socket, SpriteBatch screenDrawer) {
        screenDrawer.begin();
        final int gameCountDown = 2;
        final int buttonsStartPos = Gdx.graphics.getHeight() / 2;

        if (game.loading) {
            if (game.multiplayer && socket.enemyID == null) {
                screenDrawer.draw(homeScreen, 0,(Gdx.graphics.getHeight() - Gdx.graphics.getWidth()*2)/2, Gdx.graphics.getWidth(), Gdx.graphics.getWidth() * 2);
                screenDrawer.end();
                System.out.println("Venter på motspiller..");
                return;
            }
            else if (!game.multiplayer && socket.playerID == null) {
                screenDrawer.draw(homeScreen, 0,(Gdx.graphics.getHeight() - Gdx.graphics.getWidth()*2)/2, Gdx.graphics.getWidth(), Gdx.graphics.getWidth() * 2);
                screenDrawer.end();
                System.out.println("Setter opp spill..");
                return;
            }

            game.generatedSprites = socket.generateSprites(game.scale);
            game.gameTimer = -gameCountDown;
            game.loading = false;
            socket.getTouches(game.multiplayer);
            socket.getPoints();
            System.out.println("Spillet starter..");

            if (!game.devMode && assetManager.isLoaded("crack.mp3")) {
                // play sound to start off the game
                Sound beerPop = assetManager.get("crack.mp3", Sound.class);
                beerPop.play();
                // play background music
                Music backgroundMusic = assetManager.get("theMidnight.mp3", Music.class);
                backgroundMusic.play();
            }
        }

        screenDrawer.draw(homeScreen, 0,(Gdx.graphics.getHeight() - Gdx.graphics.getWidth()*2)/2, Gdx.graphics.getWidth(), Gdx.graphics.getWidth() * 2);
        screenDrawer.end();

        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int buttonHeight = 180*(2220/Gdx.graphics.getHeight());
                boolean multiplayerButton = screenY >= buttonsStartPos && screenY <= buttonsStartPos + buttonHeight;
                boolean soloGameButton = screenY >= buttonsStartPos + buttonHeight && screenY <= buttonsStartPos + 2*buttonHeight;
                boolean devModeButton = screenY >= buttonsStartPos + 2*buttonHeight && screenY <= buttonsStartPos + 3*buttonHeight;

                // game button clicked
                if (multiplayerButton || soloGameButton) {
                    game.loading = true;
                }
                // dev game clicked
                if (devModeButton) {
                    game.loading = true;
                    game.devMode = true;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                boolean multiplayerButton = screenY >= buttonsStartPos && screenY <= buttonsStartPos + 200;
                boolean soloGameButton = screenY >= buttonsStartPos + 200 && screenY <= buttonsStartPos + 400;
                boolean devModeButton = screenY >= buttonsStartPos + 400 && screenY <= buttonsStartPos + 600;

                // multiplayer game clicked
                if (multiplayerButton || devModeButton) {
                    game.multiplayer = true;
                    socket.setUpGame(true);
                }
                // solo game clicked
                else if (soloGameButton) {
                    game.multiplayer = false;
                    socket.setUpGame(false);
                }
                return true;
            };
        });
    }
}
