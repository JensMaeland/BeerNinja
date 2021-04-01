package com.mygdx.beerninja

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.TextInputListener
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import java.util.*

class MenuView (game: GameView) {
    private var drawer: SpriteBatch = SpriteBatch()
    private var menuBackground: Texture = Texture("home.png")
    private var gameoverBackground: Texture = Texture("map1.png")
    private var smallFont: BitmapFont
    private var largeFont: BitmapFont
    private var soundManager: AssetManager = AssetManager()

    var gameModes: ArrayList<GameMode> = ArrayList()
    var gameMode: GameMode? = null
    private var gamesummaryTimer = 0f

    // placement of text and button elements on screen
    val buttonHeight = (Gdx.graphics.width * 0.14).toInt()
    private val buttonMargin = (Gdx.graphics.width * 0.05).toFloat()
    private val headerY = (3 * Gdx.graphics.height / 4).toFloat()
    private val buttonStartY = Gdx.graphics.height / 2
    val buttonsTopY = buttonStartY + buttonHeight / 2
    var buttonsBottomY = 0

    fun render(game: GameView) {
        // if gameMode is chosen, render the loadingScreen
        if (gameMode != null) {
            renderLoadingScreen(game)
            return
        }

        // draw the menu background and header
        drawer.begin()
        drawer.draw(menuBackground, 0f, 0f, game.screenWidth.toFloat(), game.screenHeight.toFloat())
        largeFont.draw(drawer, "SamfNinja", buttonMargin, headerY)
        smallFont.draw(drawer, "Gruppe 20", buttonMargin, headerY - buttonHeight)

        // draw buttons for each gameMode
        for (i in gameModes.indices) {
            val buttonYPos = (buttonStartY - buttonHeight * (i + 1)).toFloat()
            smallFont.draw(drawer, "> " + gameModes[i].description, buttonMargin, buttonYPos)
        }
        drawer.end()

        // check for user touches on buttons, and set clicked gameMode
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val y = Gdx.graphics.height - screenY
                if (y in buttonsBottomY until buttonsTopY) {
                    val gameModeIndex = (gameModes.size - 1) - ((y - buttonsBottomY) / buttonHeight)
                    gameMode = gameModes[gameModeIndex]
                }
                return true
            }
        }
    }

    private fun renderLoadingScreen(game: GameView) {
        drawer.begin()
        drawer.draw(menuBackground, 0f, 0f, game.screenWidth.toFloat(), game.screenHeight.toFloat())

        if (gameMode!!.multiplayer) {
            largeFont.draw(drawer, "Flerspiller", buttonMargin, headerY)
        } else {
            largeFont.draw(drawer, "Enspiller", buttonMargin, headerY)
        }
        smallFont.draw(drawer, "Laster..", buttonMargin, headerY - buttonHeight)
        drawer.end()

        when {
            gameMode?.settings == false -> {
                game.currentGameModel = game.controller.setUpGame(gameMode!!.multiplayer, gameMode!!.devMode, game.username, game.scale)
            }
            gameMode!!.name == "Brukernavn" -> {
                val usernameFile = Gdx.files.local("name.txt")
                val usernameInput = UsernameInput()
                Gdx.input.getTextInput(usernameInput, "Brukernavn", "", "")
                game.username = usernameFile.readString().split("\\r?\\n")[0]
            }
            gameMode!!.name == "Toppliste" -> {
                //highscoreList()
            }
        }

        // play sounds starting and during the game
        if (!gameMode!!.devMode && gameMode?.settings == false && soundManager.isLoaded("theMidnight.mp3")) {
            soundManager.get("crack.mp3", Sound::class.java).play()
            soundManager.get("theMidnight.mp3", Music::class.java).play()
        }
    }

    fun renderGameoverScreen(game: GameView) {
        if (game.currentGameModel!!.myResult == null) {
            return
        }

        val gameSummary = game.currentGameModel!!.getGameSummary()

        drawer.begin()
        drawer.draw(gameoverBackground, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        largeFont.draw(drawer, "Game over!", buttonMargin, headerY)
        smallFont.draw(drawer, gameSummary, buttonMargin, headerY - buttonHeight)
        drawer.end()

        gamesummaryTimer += Gdx.graphics.deltaTime
        if (gamesummaryTimer > 5) {
            game.currentGameModel = null
        }
    }

    init {
        // check if username file exists on client
        val usernameFile = Gdx.files.local("name.txt")
        if (!usernameFile.exists() || usernameFile.readString().split("\\r?\\n")[0].isEmpty()) {
            // if username file does not exist, create new one
            usernameFile.writeString("", false)
            val usernameInput = UsernameInput()
            Gdx.input.getTextInput(usernameInput, "Brukernavn", "", "")
        }
        // set game username from file
        game.username = usernameFile.readString().split("\\r?\\n")[0]

        // instancing and adding all gameModes to the list
        gameModes.add(GameMode("Flerspiller", "Finn motspiller", true, false, false))
        gameModes.add(GameMode("Enspiller", "Spill alene", false, false, false))
        gameModes.add(GameMode("Utviklermodus", "Dev", true, true, false))
        gameModes.add(GameMode("Brukernavn", "Endre brukernavn", true, true, true))
        gameModes.add(GameMode("Toppliste", "Toppliste", false, false, true))
        buttonsBottomY = buttonsTopY - ((gameModes.size + 1) * buttonHeight)

        // loading the sounds for the menu and game
        soundManager.load("crack.mp3", Sound::class.java)
        soundManager.load("theMidnight.mp3", Music::class.java)
        soundManager.finishLoading()

        // instancing small and large fonts for the menu
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("DelaGothicOne.ttf"))
        val largeFontParameter = FreeTypeFontParameter()
        val smallFontParameter = FreeTypeFontParameter()
        largeFontParameter.size = (Gdx.graphics.width * 0.14).toInt()
        smallFontParameter.size = (Gdx.graphics.width * 0.05).toInt()
        smallFont = fontGenerator.generateFont(smallFontParameter)
        largeFont = fontGenerator.generateFont(largeFontParameter)
        smallFont.setColor(1f, 1f, 1f, 0.5f)
        fontGenerator.dispose()
    }
}

private class UsernameInput : TextInputListener {
    override fun input(text: String) {
        val nameFile = Gdx.files.local("name.txt")
        nameFile.writeString(text, false)
    }
    override fun canceled() {}
}