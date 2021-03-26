package com.mygdx.beerninja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    Texture homeScreen;
    Texture loadingScreenTexture;
    AssetManager assetManager;
    BitmapFont smallFont;
    BitmapFont largeFont;
    List<GameMode> gameModes = new ArrayList<>();
    // placement of text and button elements on screen
    final int buttonHeight = (int) (Gdx.graphics.getWidth() * 0.14);
    final int buttonMargin = (int) (Gdx.graphics.getWidth() * 0.05);
    final int headerY = 3*Gdx.graphics.getHeight()/4;
    final int buttonStartY = Gdx.graphics.getHeight()/2;

    public MainMenu() {
        // instancing and adding all gameModes to the list
        gameModes.add(new GameMode("Flerspiller", "Finn motspiller", true, false));
        gameModes.add(new GameMode("Enspiller", "Spill alene", false, false));
        gameModes.add(new GameMode("Utviklermodus", "Dev", true, true));

        // instancing textures with background-images
        homeScreen = new Texture("home.png");
        loadingScreenTexture = new Texture("home.png");

        // loading the sounds for the menu and game
        assetManager = new AssetManager();
        assetManager.load("crack.mp3", Sound.class);
        assetManager.load("theMidnight.mp3", Music.class);
        assetManager.finishLoading();

        // instancing small and large fonts for the menu
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("DelaGothicOne-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter largeFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        FreeTypeFontGenerator.FreeTypeFontParameter smallFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        largeFontParameter.size = (int) (Gdx.graphics.getWidth() * 0.14);
        smallFontParameter.size = (int) (Gdx.graphics.getWidth() * 0.05);
        smallFont = fontGenerator.generateFont(smallFontParameter);
        largeFont = fontGenerator.generateFont(largeFontParameter);
        fontGenerator.dispose();
    }

    public void renderMainMenu (final SamfNinja game, final BeerSocket socket, final SpriteBatch screenDrawer) {
        final int buttonsTopY = buttonStartY + buttonHeight/2;
        final int buttonsBottomY = buttonsTopY - (gameModes.size() + 1) * buttonHeight;

        if (game.loading) {
            loadingScreen(game, socket, screenDrawer);
            return;
        }

        screenDrawer.begin();
        screenDrawer.draw(homeScreen, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        largeFont.draw(screenDrawer, "SamfNinja", buttonMargin, headerY);
        smallFont.setColor(1,1,1,0.5F);
        smallFont.draw(screenDrawer, "Gruppe 20", buttonMargin, headerY - buttonHeight);

        for (int i = 0; i< gameModes.size(); i++) {
            smallFont.draw(screenDrawer, "> " + gameModes.get(i).description, buttonMargin, buttonStartY - buttonHeight*(i+1));
        }

        screenDrawer.end();

        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int y = Gdx.graphics.getHeight() - screenY;

                if (y > buttonsBottomY && y < buttonsTopY) {
                    GameMode gameMode = gameModes.get(gameModes.size()-1 - (y - buttonsBottomY) / buttonHeight);
                    game.loading = true;
                    game.multiplayer = gameMode.multiplayer;
                    game.devMode = gameMode.devMode;

                    socket.setUpGame(game.multiplayer);
                    socket.getTouches(game.multiplayer);
                }

                return true;
            }
        });
    }

    private void loadingScreen(final SamfNinja game, final BeerSocket socket, final SpriteBatch screenDrawer) {
        screenDrawer.begin();
        screenDrawer.draw(homeScreen, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // still loading..
        if (socket.playerID == null) {
            if (game.multiplayer) {
                largeFont.draw(screenDrawer, "FlerSpiller", buttonMargin, 3*Gdx.graphics.getHeight()/4);
                smallFont.setColor(1,1,1,0.5F);
                smallFont.draw(screenDrawer, "Venter..", buttonMargin, 3*Gdx.graphics.getHeight()/4 - 200);
            }
            else {
                largeFont.draw(screenDrawer, "EnSpiller", buttonMargin, 3*Gdx.graphics.getHeight()/4);
                smallFont.setColor(1,1,1,0.5F);
                smallFont.draw(screenDrawer, "Laster..", buttonMargin, 3*Gdx.graphics.getHeight()/4 - 200);
            }
            screenDrawer.end();
            return;
        }

        // all required players are loaded, starting up the game
        game.generatedSprites = new GenerateBeerFromData(socket.parsedBottleData, socket.parsedPowerupData, socket.playerID, game.scale);
        socket.getPoints(game);
        game.loading = false;
        screenDrawer.end();

        if (!game.devMode && assetManager.isLoaded("crack.mp3")) {
            // play sound to start off the game
            Sound beerPop = assetManager.get("crack.mp3", Sound.class);
            beerPop.play();
            // play background music
            Music backgroundMusic = assetManager.get("theMidnight.mp3", Music.class);
            backgroundMusic.play();
        }
    }
}
