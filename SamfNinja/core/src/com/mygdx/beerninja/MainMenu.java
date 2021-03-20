package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainMenu {
    Texture homeScreen;
    Texture loadingScreen;

    public MainMenu() {
        homeScreen = new Texture("home2.png");
        loadingScreen = new Texture("loading.png");
    }

    public void renderMainMenu (final SamfNinja game, final BeerSocket socket, SpriteBatch screenDrawer) {
        screenDrawer.begin();
        final int gameCountDown = 2;
        final int buttonsStartPos = 515;

        if (game.loading) {
            if (game.multiplayer && socket.enemyID == null) {
                screenDrawer.draw(loadingScreen, 0, 0);
                screenDrawer.end();
                System.out.println("Venter på motspiller..");
                return;
            }
            else if (!game.multiplayer && socket.playerID == null) {
                screenDrawer.draw(loadingScreen, 0, 0);
                screenDrawer.end();
                System.out.println("Setter opp spill..");
                return;
            }

            game.generatedSprites = socket.generateSprites();
            game.gameTimer = -gameCountDown;
            game.loading = false;
            socket.getTouches(game.multiplayer);
            socket.getPoints(game.multiplayer);
            System.out.println("Spillet starter..");

            if (!game.devMode) {
                // play sound to start off the game
                Sound beerPop = Gdx.audio.newSound(Gdx.files.internal("crack.mp3"));
                beerPop.play();
                // play background music
                Sound backgroundMusic = Gdx.audio.newSound(Gdx.files.internal("theMidnight.mp3"));
                backgroundMusic.play();
            }
        }

        screenDrawer.draw(homeScreen, 0, 0);
        screenDrawer.end();

        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                boolean multiplayerButton = screenY >= buttonsStartPos && screenY <= buttonsStartPos + 100;
                boolean soloGameButton = screenY >= buttonsStartPos + 100 && screenY <= buttonsStartPos + 200;
                boolean devModeButton = screenY >= buttonsStartPos + 200 && screenY <= buttonsStartPos + 300;

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
                boolean multiplayerButton = screenY >= buttonsStartPos && screenY <= buttonsStartPos + 100;
                boolean soloGameButton = screenY >= buttonsStartPos + 100 && screenY <= buttonsStartPos + 200;
                boolean devModeButton = screenY >= buttonsStartPos + 200 && screenY <= buttonsStartPos + 300;

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
