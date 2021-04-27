package com.mygdx.beerninja

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.TextInputListener
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.mygdx.beerninja.Entities.RouteRequest
import org.json.JSONArray
import java.util.*

/*
Menu VIEW, included in the Main View and part of the MVC View

 */

class MenuView (val game: GameView) {
    private var smallFont: BitmapFont
    private var largeFont: BitmapFont
    private var soundManager: AssetManager = AssetManager()
    private var tutorial: TutorialView? = null

    var routeRequests: ArrayList<RouteRequest> = ArrayList()
    private var loadingscreenTimer = 0f
    private var gamesummaryTimer = 0f
    var showTutorial = false

    // placement of text and button elements on screen
    val buttonHeight = (Gdx.graphics.width * 0.14).toInt()
    private val buttonMargin = (Gdx.graphics.width * 0.05).toFloat()
    private val headerY = ((3 * Gdx.graphics.height / 4)  + buttonHeight).toFloat()
    private val buttonStartY = (Gdx.graphics.height / 2) + buttonHeight
    val buttonsTopY = buttonStartY + buttonHeight / 2
    var buttonsBottomY = 0

    // menu renders only if no currentGameMode is initialized
    fun render() {
        // if a newGameModel is created after controller has received data, set this to the current one
        if (game.controller.newGameModel != null) {
            loadingscreenTimer = 0f
            game.currentGameModel = game.controller.newGameModel
            // play sounds starting and during the game
            if (!game.controller.newGameModel!!.devMode && soundManager.isLoaded("theMidnight.mp3")) {
                soundManager.get("crack.mp3", Sound::class.java).play()
                soundManager.get("theMidnight.mp3", Music::class.java).play()
            }
            return
        }
        // if no newGameModel exists, but a game is still loading, render the loading screen
        else if (game.controller.loadingGame != null) {
            loadingscreenTimer -= Gdx.graphics.deltaTime

            if (loadingscreenTimer < 0f) {
                game.controller.loadingGame = null
            }
            else {
                renderLoadingScreen()
                return
            }
        }

        // draw the menu background and header
        game.drawer.draw(game.textures["menuBkg"], 0f, 0f, game.screenWidth.toFloat(), game.screenHeight.toFloat())
        largeFont.draw(game.drawer, "SamfNinja", buttonMargin, headerY)
        smallFont.setColor(1f, 1f, 1f, 0.5f)

        if (!game.controller.connected) {
            smallFont.draw(game.drawer, "Kobler til server..", buttonMargin, headerY - buttonHeight)
        }
        // tutorial
        else if (showTutorial) {
            tutorial?.render(this, game, smallFont, buttonStartY.toFloat() - 200f, buttonMargin, headerY - buttonHeight)
            return
        }
        // highscore list fetches every time user clicks the button. If fetched, show the new list
        else if (game.controller.highscoreList != null) {
            renderHighscoreList()
        }
        else {
            smallFont.draw(game.drawer, "Gruppe 20", buttonMargin, headerY - buttonHeight)
            smallFont.setColor(1f, 1f, 1f, 1f)
            // draw buttons for each routeRequest, meaning a game or a setting
            for (i in routeRequests.indices) {
                val buttonYPos = (buttonStartY - buttonHeight * (i + 1)).toFloat()
                if (routeRequests[i].devMode) {
                    smallFont.setColor(1f, 1f, 1f, 0.5f)
                }
                smallFont.draw(game.drawer, "> " + routeRequests[i].description, buttonMargin, buttonYPos)
            }
        }

        // check for user touches on buttons, and act on specified route request
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (!game.controller.connected) return true

                if (game.controller.loadingGame != null && screenY >= game.screenHeight - 120f * game.scale) {
                    loadingscreenTimer = 0f
                    game.controller.loadingGame = null
                    return true
                }

                if (game.controller.highscoreList != null) {
                    game.controller.highscoreList = null
                    return true
                }

                val y = Gdx.graphics.height - screenY
                if (y in buttonsBottomY until buttonsTopY) {
                    val gameModeIndex = (routeRequests.size - 1) - ((y - buttonsBottomY) / buttonHeight)
                    onButtonClicked(routeRequests[gameModeIndex])
                }
                return true
            }
        }
    }

    private fun onButtonClicked(routeRequest: RouteRequest) {
        when {
            !routeRequest.settings -> {
                loadingscreenTimer = 30f
                game.controller.setUpGame(routeRequest, getUsername(), game.scale, game.drawer, soundManager, game.textures)
            }
            routeRequest.name == "Brukernavn" -> {
                val usernameInput = UsernameInput()
                Gdx.input.getTextInput(usernameInput, "Brukernavn", getUsername(), "")
            }
            routeRequest.name == "Toppliste" -> {
                game.controller.getHighscoreList()
            }
            routeRequest.name == "Tutorial" -> {
                showTutorial = true
            }
        }
    }

    private fun renderLoadingScreen() {
        game.drawer.draw(game.textures["menuBkg"], 0f, 0f, game.screenWidth.toFloat(), game.screenHeight.toFloat())

        if (game.controller.loadingGame!!.multiplayer) {
            largeFont.draw(game.drawer, "Flerspiller", buttonMargin, headerY)
            smallFont.draw(game.drawer, "Ser etter motspiller..", buttonMargin, headerY - buttonHeight)

            smallFont.setColor(1f, 1f, 1f, 0.5f)
            smallFont.draw(game.drawer, "< Tilbake", buttonMargin, 100f * game.scale)
        } else {
            largeFont.draw(game.drawer, "Enspiller", buttonMargin, headerY)
            smallFont.draw(game.drawer, "Laster..", buttonMargin, headerY - buttonHeight)

            smallFont.setColor(1f, 1f, 1f, 0.5f)
            smallFont.draw(game.drawer, "< Tilbake", buttonMargin, 100f * game.scale)
        }
    }

    private fun renderHighscoreList() {
        val scores: JSONArray? = game.controller.highscoreList?.names()

        smallFont.draw(game.drawer, "Toppliste", buttonMargin, headerY - buttonHeight)
        smallFont.setColor(1f, 1f, 1f, 1f)

        if (scores != null) {
            for (i in 0 until scores.length()) {
                val username: String = scores.getString(i)
                val score: String = game.controller.highscoreList!!.getString(username)
                smallFont.draw(game.drawer, (i + 1).toString() + ".plass: " + username + " - " + score , buttonMargin, buttonStartY - (i - 2) * 60f * game.scale)
            }
            smallFont.setColor(1f, 1f, 1f, 0.5f)
            smallFont.draw(game.drawer, "< Tilbake", buttonMargin, buttonStartY - (scores.length() - 2) * 60f * game.scale)
        }
    }

    fun renderGameoverScreen() {
        if (game.currentGameModel!!.myResult == null) {
            return
        }

        val gameSummary = game.currentGameModel!!.getGameSummary()

        game.drawer.draw(game.textures["gameoverBkg"], 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        largeFont.draw(game.drawer, "Game over", buttonMargin, headerY)
        smallFont.draw(game.drawer, gameSummary, buttonMargin, headerY - buttonHeight)

        // display the gameOver screen only for specified time
        gamesummaryTimer += Gdx.graphics.deltaTime
        if (gamesummaryTimer > 5) {
            // set gameModel to null, making space for a new game
            game.currentGameModel = null
        }
    }

    // username is stored locally on device in a text file
    private fun getUsername() : String {
        val usernameFile = Gdx.files.local("name.txt")
        return usernameFile.readString().split("\\r?\\n")[0]
    }

    init {
        // check if username file exists on client
        val usernameFile = Gdx.files.local("name.txt")
        if (!usernameFile.exists() || usernameFile.readString().split("\\r?\\n")[0].isEmpty()) {
            // if username file does not exist, create new one
            usernameFile.writeString("", false)
            val usernameInput = UsernameInput()
            Gdx.input.getTextInput(usernameInput, "Brukernavn", "", "Hvordan skal andre spillere se deg?")
            // no username indicates a new player, therefore showing tutorial
            showTutorial = true
        }

        // instancing and adding all route requests to the list. These dictate the buttons on screen and what they do
        routeRequests.add(RouteRequest("Flerspiller", "Start flerspiller", multiplayer = true, devMode = false, settings = false))
        routeRequests.add(RouteRequest("Enspiller", "Start solo", multiplayer = false, devMode = false, settings = false))
        routeRequests.add(RouteRequest("Toppliste", "Toppliste", multiplayer = false, devMode = false, settings = true))
        routeRequests.add(RouteRequest("Brukernavn", "Endre brukernavn", multiplayer = true, devMode = false, settings = true))
        routeRequests.add(RouteRequest("Tutorial", "Tutorial", multiplayer = false, devMode = false, settings = true))
        routeRequests.add(RouteRequest("Utviklermodus", "Dev modus", false, true, false))
        buttonsBottomY = buttonsTopY - ((routeRequests.size + 1) * buttonHeight)

        // loading the sounds for the menu and game
        soundManager.load("crack.mp3", Sound::class.java)
        soundManager.load("theMidnight.mp3", Music::class.java)
        soundManager.load("break.mp3", Sound::class.java)
        soundManager.load("crush.mp3", Sound::class.java)
        soundManager.load("pop.mp3", Sound::class.java)
        soundManager.finishLoading()

        // instancing small and large fonts for the menu
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("DelaGothicOne.ttf"))
        val largeFontParameter = FreeTypeFontParameter()
        val smallFontParameter = FreeTypeFontParameter()
        largeFontParameter.size = (Gdx.graphics.width * 0.14).toInt()
        smallFontParameter.size = (Gdx.graphics.width * 0.05).toInt()
        smallFont = fontGenerator.generateFont(smallFontParameter)
        largeFont = fontGenerator.generateFont(largeFontParameter)
        fontGenerator.dispose()

        tutorial = TutorialView(game)
    }
}

// helping class to have user input text
private class UsernameInput : TextInputListener {
    override fun input(text: String) {
        if (text.isNotEmpty()) {
            val nameFile = Gdx.files.local("name.txt")
            nameFile.writeString(text, false)
        }
    }
    override fun canceled() {}
}