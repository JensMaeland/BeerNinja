package com.mygdx.beerninja

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

class GameView : ApplicationAdapter() {
    // imported help-classes for graphics and texture
    lateinit var drawer: SpriteBatch
    private lateinit var fontDrawer: BitmapFont
    lateinit var textures: HashMap<String, Texture>
    private lateinit var scoreBars: ArrayList<Texture>

    private lateinit var splash: TextureRegion
    // main menu for the game
    private lateinit var menu: MenuView
    // game-controller responsible for communicating with server
    lateinit var controller: GameController
    // model or state of an ongoing game
    var currentGameModel: GameModel? = null

    // game-settings as variables
    var screenWidth = 0
    var screenHeight = 0
    var scale = 0

    // function to create or start up the application
    override fun create() {
        // set screenWidth and height for device
        screenWidth = Gdx.graphics.width
        screenHeight = Gdx.graphics.height
        // set scale for graphics based on device screen width
        scale = Gdx.graphics.width / 540
        // instancing a new batch drawer
        drawer = SpriteBatch()
        // instancing a new font drawer
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("DelaGothicOne.ttf"))
        val fontParameter = FreeTypeFontParameter()
        fontParameter.size = (Gdx.graphics.width * 0.05).toInt()
        fontDrawer = fontGenerator.generateFont(fontParameter)
        fontGenerator.dispose()
        // instancing the main menu
        menu = MenuView()
        // instancing a game controller, automatically connecting to server
        controller = GameController()

        // setting backgrounds and textures to correct png images
        val textureNames = listOf("bkg", "secondaryBkg", "menuBkg", "gameoverBkg", "myTouch", "enemyTouch", "hitbox", "samfkort", "dahls", "pils", "dag", "tail1", "tail2")
        textures = HashMap()
        for (textureName: String in textureNames) {
            textures[textureName] = Texture("$textureName.png")

        }
        scoreBars = ArrayList()
        // adding the different textures for the scoreBar to a list
        scoreBars.add(Texture("bar2_0.png"))
        scoreBars.add(Texture("bar2_1.png"))
        scoreBars.add(Texture("bar2_2.png"))
        scoreBars.add(Texture("bar2_3.png"))
        scoreBars.add(Texture("bar2_4.png"))
        scoreBars.add(Texture("bar2_5.png"))
        splash = TextureRegion(Texture("splash.png"))
    }

    // render function called continuously to display content on screen
    override fun render() {
        drawer.begin()

        // as long as no gameModel exists, display the main menu only
        when {
            currentGameModel == null -> {
                menu.render(this)
            }

            // check if game is ongoing, meaning controller has not received gameSummary with result
            currentGameModel!!.myResult == null -> {
                // increment the gameModel timer
                currentGameModel!!.timer += Gdx.graphics.deltaTime
                // render the GAME SCENE with all its layers
                renderGUI()
                renderBeerSprites()
                renderUserTouches()
                // continuously check hitBoxes in the gameModel
                currentGameModel!!.checkHitboxes(currentGameModel!!.timer, scale, currentGameModel!!.devMode)
                controller.sendTouches(currentGameModel!!)
            }

            // after the game is done, display the gameOver screen
            else -> {
                menu.renderGameoverScreen(this)
            }
        }

        drawer.end()
    }

    // function to render all game GUI
    private fun renderGUI() {
        // create a pulsating background light by setting brightness based on the game timer
        val currentMillisec = currentGameModel!!.timer - currentGameModel!!.timer.roundToInt()
        var brightness = when {
            currentGameModel!!.devMode -> { 0.8f }
            currentGameModel!!.timer > currentGameModel!!.powerupTimer + 1 -> {
                0.1.coerceAtLeast(currentMillisec + 0.5).toFloat()
            }
            else -> {
                0.6.coerceAtLeast(currentMillisec + 0.5).toFloat()
            }
        }
        drawer.setColor(brightness, brightness, 1f, 1f)
        // set font color for game GUI
        fontDrawer.setColor(1f, 1f, 0.2f, 1f)

        // draw game background, and change to secondaryBackground after powerUp has spawned
        if (currentGameModel!!.timer <= currentGameModel!!.powerupTimer + 1) {
            drawer.draw(textures["bkg"], 0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
        } else {
            drawer.draw(textures["secondaryBkg"], 0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
        }
        drawer.setColor(1f, 1f, 1f, 1f)

        // define the offsets or margins from border for the font and GUI
        val fontOffsetX = scale * 15f
        val fontOffsetY = scale * 70f
        // draw the points on screen, either for one player or both players if multiplayer
        fontDrawer.draw(drawer, currentGameModel!!.username + ": " + currentGameModel!!.myPoints, fontOffsetX, screenHeight - fontOffsetY)
        if (currentGameModel!!.multiplayer) {
            fontDrawer.draw(drawer, currentGameModel!!.enemyUsername + ": " + currentGameModel!!.enemyPoints, screenWidth/2f, screenHeight - fontOffsetY)
        }

        // draw background for the score-strike bar
        drawer.draw(scoreBars[0], fontOffsetX, screenHeight - fontOffsetY, screenWidth - 2*fontOffsetX, 60f*scale)

        // draw splash effects when a bottle has been caught
        if (currentGameModel?.latestCaughtBottle != null) {
            // set brightness of drawer based on time since caughtBottle, so splash effect fades out
            brightness = (1.0 - (currentGameModel!!.timer - currentGameModel?.latestCaughtBottle!!.time)).toFloat()
            drawer.setColor(brightness, brightness, brightness, brightness)

            val caughtX = currentGameModel?.latestCaughtBottle!!.xcoor
            val caughtY = currentGameModel?.latestCaughtBottle!!.ycoor
            val splashWidth = (splash.regionWidth * scale).toFloat()
            val splashHeight = (splash.regionHeight * scale).toFloat()
            drawer.draw(splash, caughtX - 80, caughtY + 40, 0f, 0f, splashWidth, splashHeight, 1f, 1f, 0f)
        }

        // draw the countDown in seconds until game over
        val countDown = (currentGameModel!!.gameDuration - currentGameModel!!.timer).toInt()
        fontDrawer.draw(drawer, countDown.toString(), fontOffsetX, fontOffsetY)

        // if streak is higher than 0, draw the correct scoreStreak texture
        if (currentGameModel!!.streak > 0) {
            drawer.setColor(1.2f-brightness, 1.2f-brightness, 1f, 1f)
            drawer.draw(scoreBars[currentGameModel!!.streak], fontOffsetX, screenHeight - (fontOffsetY - 5f*scale), screenWidth - 2*fontOffsetX, 50f*scale)

            // draw a number in a different color to indicate the scoreStreak
            fontDrawer.setColor(1f, 0.1f, 0.5f, 1f)
            fontDrawer.draw(drawer, "+ " + currentGameModel!!.streak, screenWidth/2f - 3*fontOffsetX, screenHeight - fontOffsetY)
        }

        drawer.setColor(1f, 1f, 1f, 1f)
    }

    private fun renderBeerSprites() {
        for (beerBottle in currentGameModel!!.spawn(currentGameModel!!.devMode)) {
            val width = (beerBottle.texture!!.regionWidth * scale).toFloat()
            val height = (beerBottle.texture!!.regionHeight * scale).toFloat()
            val bottleX = beerBottle.getXOffset(currentGameModel!!.timer)
            val bottleY = beerBottle.getYOffset(currentGameModel!!.timer)
            val spin = beerBottle.getSpin(currentGameModel!!.timer)

            val tailWidth = (beerBottle.tail!!.regionWidth * scale * 2).toFloat()
            val tailHeight = (beerBottle.tail!!.regionHeight * scale * 2).toFloat()
            val tailOffset = beerBottle.getTailOffset(bottleX)
            drawer.draw(beerBottle.tail, tailOffset, bottleY + height / 2, tailWidth, tailHeight / 2, tailWidth, tailHeight, 1f, 1f, spin)
            drawer.draw(beerBottle.texture, bottleX, bottleY, width / 2, height / 2, width, height, 1f, 1f, spin)
        }
    }

    private fun renderUserTouches() {
        val scaleX = Gdx.graphics.width / 100
        val scaleY = Gdx.graphics.height / 100

        // get touches
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                currentGameModel?.streak = 0
                return true
            }

            // built in method for touch drag, which is most useful for our swipe/drag-based game
            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                currentGameModel?.checkScoreStreak(screenX / scaleX, screenY / scaleY)
                val touch = currentGameModel?.getCurrentTouch()
                if (touch != null) {
                    touch.x = screenX / scaleX
                    touch.y = screenY / scaleY
                    touch.display = true
                }
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                currentGameModel?.hideTouches()
                return false
            }
        }

        // render user touches
        var prevX = 0
        var prevY = 0
        for (touch in currentGameModel?.touches?.values!!) {
            if (touch.display) {
                val touchX = touch.x * scaleX
                val touchY = screenHeight - touch.y * scaleY
                if (prevX > 0) {
                    // fill in the gap if two subsequent touches are far apart
                    var deltaX = touchX.toFloat() - prevX
                    var deltaY = touchY.toFloat() - prevY
                    while (abs(deltaX) > 10 && abs(deltaX) < 80 || abs(deltaY) > 10 && abs(deltaY) < 80) {
                        drawer.draw(touch.texture, touchX - deltaX / 2, touchY - deltaY / 2, 16f * scale, 16f * scale)
                        drawer.draw(touch.texture, prevX + deltaX / 2, prevY + deltaY / 2, 16f * scale, 16f * scale)
                        deltaX = sign(deltaX) * (abs(deltaX) - 2).coerceAtLeast(2f)
                        deltaY = sign(deltaY) * (abs(deltaY) - 2).coerceAtLeast(2f)
                    }
                }
                prevX = touchX
                prevY = touchY
                drawer.draw(touch.texture, touchX.toFloat(), touchY.toFloat(), 16f * scale, 16f * scale)
            }
        }

        // render enemy touches
        if (currentGameModel!!.multiplayer) {
            for (touch in currentGameModel!!.enemyTouches.values) {
                if (touch.display) {
                    drawer.draw(touch.texture, (screenWidth - touch.x * scaleX).toFloat(), (screenHeight - touch.y * scaleY).toFloat(), 16f * scale, 16f * scale)
                }
            }
        }
    }

    override fun dispose() {
        drawer.dispose()
    }
}