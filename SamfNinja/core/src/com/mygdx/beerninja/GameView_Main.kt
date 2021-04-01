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
    private lateinit var drawer: SpriteBatch
    private lateinit var fontDrawer: BitmapFont
    private lateinit var background: Texture
    private lateinit var secondaryBackground: Texture
    private lateinit var splash: TextureRegion
    private lateinit var scoreBars: ArrayList<Texture>

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
    var username = ""
    // set which background-image should display
    private val backgroundImage = "map2.png"

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
        menu = MenuView(this)
        // instancing a game controller, automatically connecting to server
        controller = GameController()

        // setting backgrounds to correct png images
        background = Texture(backgroundImage)
        secondaryBackground = Texture("map3.png")
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
        // as long as no gameModel exists, display the main menu only
        if (currentGameModel == null) {
            menu.render(this)
            return
        }

        // check if game is ongoing, meaning the gameModel timer is less than game duration
        if (currentGameModel!!.timer < currentGameModel!!.gameDuration) {
            // increment the gameModel timer
            currentGameModel!!.timer += Gdx.graphics.deltaTime
            // render all elements of the gameView
            renderGUI()
            renderBeerSprites()
            renderUserTouches()
            // continuously check hitBoxes in the gameModel
            currentGameModel!!.checkHitboxes(currentGameModel!!.timer, scale)
            return
        }

        // after the game is done, display the gameOver screen
        menu.renderGameoverScreen(this)
    }

    // function to render all game GUI
    private fun renderGUI() {
        // create a pulsating background light by setting brightness based on the game timer
        val currentMillisec = currentGameModel!!.timer - currentGameModel!!.timer.roundToInt()
        var brightness = when {
            currentGameModel!!.devMode -> { 0.8f }
            currentGameModel!!.timer > currentGameModel!!.powerUpTimer + 1 -> {
                0.1.coerceAtLeast(currentMillisec + 0.5).toFloat()
            }
            else -> {
                0.6.coerceAtLeast(currentMillisec + 0.5).toFloat()
            }
        }
        drawer.setColor(brightness, brightness, 1f, 1f)
        // set font color for game GUI
        fontDrawer.setColor(1f, 1f, 0.2f, 1f)

        drawer.begin()
        // draw game background, and change to secondaryBackground after powerUp has spawned
        if (currentGameModel!!.timer <= currentGameModel!!.powerUpTimer + 1) {
            drawer.draw(background, 0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
        } else {
            drawer.draw(secondaryBackground, 0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
        }
        drawer.setColor(1f, 1f, 1f, 1f)

        // define the offsets or margins from border for the font and GUI
        val fontOffsetX = scale * 15f
        val fontOffsetY = scale * 15f + 80f
        // draw the points on screen, either for one player or both players if multiplayer
        fontDrawer.draw(drawer, username + ": " + currentGameModel!!.myPoints, fontOffsetX, screenHeight - fontOffsetY)
        if (currentGameModel!!.multiplayer) {
            fontDrawer.draw(drawer, currentGameModel!!.enemyUsername + ": " + currentGameModel!!.enemyPoints, screenWidth/2f, screenHeight - fontOffsetY)
        }

        // draw background for the score-strike bar
        drawer.draw(scoreBars[0], fontOffsetX, screenHeight - fontOffsetY, screenWidth - 2*fontOffsetX, 60f*scale)

        // draw splash effects when a bottle has been caught
        if (currentGameModel?.latestCaughtBottle != null) {
            // set brightness of drawer based on time since caughtBottle, so splash effect fades out
            brightness = (2.0 - (currentGameModel!!.timer - currentGameModel?.latestCaughtBottle!!.time)).toFloat()
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
            drawer.draw(scoreBars[currentGameModel!!.streak], fontOffsetX, screenHeight - 65f*scale, screenWidth - 2*fontOffsetX, 50f*scale)

            // draw a number in a different color to indicate the scoreStreak
            fontDrawer.setColor(1f, 0.1f, 0.5f, 1f)
            fontDrawer.draw(drawer, "+ " + currentGameModel!!.streak, screenWidth/2f - fontOffsetX, screenHeight - fontOffsetY)
        }

        drawer.setColor(1f, 1f, 1f, 1f)
        drawer.end()
    }

    private fun renderBeerSprites() {
        drawer.begin()
        for (beerBottle in currentGameModel!!.spawn()) {
            val width = (beerBottle.texture!!.regionWidth * scale).toFloat()
            val height = (beerBottle.texture!!.regionHeight * scale).toFloat()
            val bottleX = beerBottle.getXOffset(currentGameModel!!.timer)
            val bottleY = beerBottle.getYOffset(currentGameModel!!.timer)
            val spin = beerBottle.getSpin(currentGameModel!!.timer)
            drawer.draw(beerBottle.texture, bottleX, bottleY, width / 2, height / 2, width, height, 1f, 1f, spin)
        }
        drawer.end()
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

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                currentGameModel?.checkTouchDirectionChange(screenX / scaleX, screenY / scaleY)
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
        drawer.begin()

        var prevX = 0
        var prevY = 0
        for (touch in currentGameModel?.touches?.values!!) {
            if (touch!!.display) {
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
                drawer.draw(touch.texture, touchX.toFloat(), touchY.toFloat(), 12f, 12f)
            }
        }
        drawer.end()

        // render enemy touches
        if (currentGameModel!!.multiplayer) {
            drawer.begin()
            for (touch in currentGameModel!!.enemyTouches.values) {
                if (touch.display) {
                    drawer.draw(touch.texture, (screenWidth - touch.x * scaleX).toFloat(), (screenHeight - touch.y * scaleY).toFloat())
                }
            }
            drawer.end()
        }
    }

    override fun dispose() {
        drawer.dispose()
    }
}