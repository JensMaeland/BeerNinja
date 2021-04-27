package com.mygdx.beerninja

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.beerninja.Entities.Bottle
import com.mygdx.beerninja.Entities.CaughtBottle
import com.mygdx.beerninja.Entities.Touch
import com.mygdx.beerninja.Systems.MatrixRotation
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.math.sign

/*
Game MODEL, contains the data and functions to alter the state
Part of the MVC Pattern

Represents the current game, maximum one at a time
 */

class GameModel(
        private var controller: GameController,
        var playerID: String,
        var enemyID: String,
        var username: String,
        var enemyUsername: String,
        bottleData: ArrayList<JSONObject>,
        powerupData: ArrayList<JSONObject>,
        var multiplayer: Boolean,
        var devMode: Boolean,
        var gameDuration: Int,
        var powerupTimer: Float,
        private var drawer: SpriteBatch,
        private var soundManager: AssetManager,
        scale: Int,
        textures: HashMap<String, Texture>) {

    // bottle state
    private var bottles: ArrayList<Bottle>
    private var powerupBottles: ArrayList<Bottle>
    var latestCaughtBottle: CaughtBottle? = null
    private var rotationSystem: MatrixRotation

    // touch state
    val touches = HashMap<Int, Touch>()
    var enemyTouches = HashMap<Int, Touch>()
    var currentTouchIndex = 0
    var enemyTouchIndex = 0

    // point/score state
    var myPoints = 0
    var enemyPoints = 0
    var myResult: JSONObject? = null
    var enemyResult: JSONObject? = null

    // other game state
    private var totalNumberOfBottles = 0
    private var numberOfSpawnedBottles = 0
    var streak = 0 // streak indicates bottles caught in a single movement
    var timer = -2f // set seconds until game start
    private var streakStartTime = 0f
    private var tailLength = 35 // set length of tail when touching the screen
    private var powerupId = 420 // the bottle-object used to activate powerUps if caught

    // function to return the bottles currently on screen
    fun spawn(devMode: Boolean): List<Bottle> {
        val currentBottles: ArrayList<Bottle> = ArrayList()
        var newNumberOfBottles = 0

        // the bottles list contain all bottles not yet taken
        for (bottle in bottles) {
            // if spawnTime is reached, and bottle has been active for less than 10 secs
            if (timer > bottle.beerSpawnTime) {
                if (timer < bottle.beerSpawnTime + 10) {
                    currentBottles.add(bottle)
                }
                // count all the bottles currently spawned
                newNumberOfBottles += 1
            }
        }

        // play sound when spawning new bottles, using new count of bottles
        if (newNumberOfBottles > numberOfSpawnedBottles && !devMode) {
            soundManager.get("pop.mp3", Sound::class.java).play()
            numberOfSpawnedBottles = newNumberOfBottles
        }

        return currentBottles
    }

    fun updateTouches(scaleX: Int, scaleY: Int) {
        // get touches
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                streak = 0
                return true
            }

            // built in method for touch drag, which is most useful for our swipe/drag-based game
            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                checkScoreStreak(screenX / scaleX, screenY / scaleY)
                val touch = getCurrentTouch()
                if (touch != null) {
                    touch.x = screenX / scaleX
                    touch.y = screenY / scaleY
                    touch.display = true
                }
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                hideTouches()
                return false
            }
        }
    }

    // remove any caught bottle form the bottles list, and send to the controller and server
    private fun playerCaughtBottle(caughtBottle: CaughtBottle) {
        for (bottle in bottles) {
            if (bottle.id == caughtBottle.id) {
                bottles.remove(bottle)
                // powerUp activation bottle should not be sent to server
                if (caughtBottle.id != powerupId) {
                    controller.sendCaughtBottle(caughtBottle)
                } else {
                    // if powerUp bottle is caught, add all the powerUp bottles to the bottles list
                    bottles.addAll(powerupBottles)
                }
                break
            }
        }

        // play a sound to give the player feedback
        if (!devMode) {
            if (caughtBottle.playerID == playerID || caughtBottle.id == powerupId) {
                soundManager.get("break.mp3", Sound::class.java).play()
            }
            else {
                soundManager.get("crush.mp3", Sound::class.java).play()
            }
        }
    }

    // function that the controller can use after receiving data from server
    fun enemyCaughtBottle(caughtBottle: CaughtBottle) {
        for (bottle in bottles) {
            if (bottle.id == caughtBottle.id) {
                bottles.remove(bottle)
                caughtBottle.xcoor = bottle.getXOffset(timer, 1)
                caughtBottle.ycoor = bottle.getYOffset(timer)
                latestCaughtBottle = caughtBottle
                break
            }
        }
    }

    // function to determine if hitBoxes overlap, and how to act upon it
    fun checkHitboxes(gameTimer: Float, scale: Int, devMode: Boolean) {
        // get latest touch object for player
        var touch = touches[tailLength - 1]
        if (currentTouchIndex > 0) {
            touch = touches[currentTouchIndex - 1]
        }

        val scaleX = Gdx.graphics.width / 100
        val scaleY = Gdx.graphics.height / 100
        val touchX = touch!!.x * scaleX
        val touchY = Gdx.graphics.height - touch.y * scaleY

        val beerBottles = spawn(devMode)
        val powerupCollisions = false

        // check bottle hitBox for all spawned bottles
        for (beerBottle in beerBottles) {
            val hitbox = beerBottle.getHitbox(gameTimer, drawer, devMode, scale, rotationSystem)

            // check touch hits with bottles
            if (touch.display && hitbox.left <= touchX && touchX <= hitbox.right) {
                if (hitbox.bottom <= touchY && touchY <= hitbox.top) {
                    // if touch hits a bottle, create a new caughtBottle, and call the belonging function
                    val caughtBottle = CaughtBottle(beerBottle.id, gameTimer, beerBottle.getXOffset(gameTimer, 1), beerBottle.getYOffset(gameTimer), beerBottle.bottlePlayerId, streak + 1)
                    playerCaughtBottle(caughtBottle)
                    latestCaughtBottle = caughtBottle
                    // the streak increases for each bottle caught
                    streak = (streak + 1).coerceAtMost(5)
                    if (streak == 1) {
                        streakStartTime = timer
                    }
                }
            }

            // check bottle hitBox with other bottles, to potentially create a collision
            // only check if bottle is on-screen and not already collided
            if (hitbox.left > 0 && hitbox.top > 0 && hitbox.left < Gdx.graphics.width && hitbox.top < Gdx.graphics.height && !beerBottle.collision) {
                // iterate through all the other spawned bottles
                for (compareBottle in beerBottles) {
                    val obstacle = compareBottle.getHitbox(gameTimer, drawer, devMode, scale, rotationSystem)
                    // only compare the two bottles if they are different, and not part of the main bottles. PowerUp-bottles appended later are meant to overlap unless powerupCollisions is true
                    if (beerBottle.id != compareBottle.id && beerBottle.id != powerupId && (beerBottle.bottlePlayerId != compareBottle.bottlePlayerId || (powerupCollisions && beerBottle.id > totalNumberOfBottles))) {
                        // check if the two bottles hitBoxes overlap in both x and y
                        if (hitbox.right > obstacle.left && hitbox.left < obstacle.right) {
                            if (hitbox.top > obstacle.bottom && hitbox.bottom < obstacle.top) {
                                // set x-origin to collision point and set collision flag for the bottle
                                beerBottle.xStartPos = beerBottle.getXOffset(gameTimer, 1).toInt()
                                beerBottle.collision = true
                            }
                        }
                    }
                }
            }
        }
    }

    // update the values for all enemy touch objects, with input from controller
    fun updateEnemyTouches(touchData: JSONObject) {
        for (i in 0 until touchData.length()) {
            val touch = touchData[i.toString()] as JSONObject
            val touchId = touch["id"] as Int
            val touchXPos = touch["x"] as Int
            val touchYPos = touch["y"] as Int
            val touchDisplay = touch["display"] as Boolean

            val enemyTouch = enemyTouches[touchId]
            enemyTouch!!.x = touchXPos
            enemyTouch.y = touchYPos
            enemyTouch.display = touchDisplay
        }
    }

    fun getCurrentTouch() : Touch? {
        val touch = touches[currentTouchIndex]
        // increments the index, making sure next call to function returns the next touch
        currentTouchIndex = (currentTouchIndex + 1) % tailLength
        return touch
    }

    // if touch direction changes or too long since streak started, current streak is cancelled
    fun checkScoreStreak(x: Int, y: Int) {
        if (streak == 0) return

        // find previous touches to compare direction
        var prevPrevTouchIndex = currentTouchIndex - 4
        var prevTouchIndex = currentTouchIndex - 2

        if (prevPrevTouchIndex < 0) {
            prevPrevTouchIndex = tailLength - prevPrevTouchIndex
            if (prevTouchIndex < 0) {
                prevTouchIndex = tailLength - prevTouchIndex
            }
        }

        val prevPrevTouch = touches[prevPrevTouchIndex]
        val prevTouch = touches[prevTouchIndex]

        // direction change is based on the directions of x or y for two previous touches, compared to the current touch and the previous one
        val directionChange = prevPrevTouch != null && prevTouch != null && prevPrevTouch.display && prevTouch.display &&
                ((sign((prevTouch.x - prevPrevTouch.x).toDouble()) != sign((x - prevTouch.x).toDouble())) && (prevTouch.x != prevPrevTouch.x) && (x != prevTouch.x)
                || (sign((prevTouch.y - prevPrevTouch.y).toDouble()) != sign((y - prevTouch.y).toDouble())) && (prevTouch.y != prevPrevTouch.y) && (y != prevTouch.y))

        // reset the score streak if called for
        if (directionChange || streakStartTime + 3 < timer) {
            streak = 0
        }
    }

    // function to be called when user stops touching the screen
    fun hideTouches() {
        for (i in 0 until touches.size) {
            val touch = touches[i]
            touch!!.display = false
        }
    }

    // create a string to display on screen based on the game summary
    fun getGameSummary() : String {
        val myScore = myResult!!.getInt("score")

        if (!multiplayer) {
            return "$username fikk $myScore poeng!"
        }

        val enemyScore = enemyResult!!.getInt("score")
        val myCount = (myResult?.get("bottles") as JSONArray).length()
        val enemyCount = (enemyResult?.get("bottles") as JSONArray).length()

        return when {
            myPoints > enemyPoints -> {
                "Du vant med totalt $myCount flasker og $myScore poeng! \n $enemyUsername fanget $enemyCount flasker og $enemyScore poeng!"
            }
            myPoints < enemyPoints -> {
                "$enemyUsername vant med totalt $enemyCount flasker og $enemyScore poeng! \n Du fanget $myCount flasker og $myScore poeng!"
            }
            else -> {
                "Spillet ble uavgjort med $myScore poeng!"
            }
        }
    }

    init {
        val inputBottles: MutableList<Bottle> = ArrayList()
        val powerupInputBottles: MutableList<Bottle> = ArrayList()

        // create arrayLists with bottles and powerUps parsed from JSONObjects from the controller
        try {
            for (spriteData in bottleData) {
                val bottle = Bottle(spriteData["id"] as Int, (spriteData["playerID"] as String), spriteData["offsetY"] as Int, spriteData["velocity"] as Int,
                        (spriteData["spin"] as Double).toFloat(), (spriteData["secondsToSpawn"] as Double).toFloat(), scale, playerID, textures)
                inputBottles.add(bottle)
            }
            for (spriteData in powerupData) {
                val bottle = Bottle(spriteData["id"] as Int, (spriteData["playerID"] as String), spriteData["offsetY"] as Int, spriteData["velocity"] as Int,
                        (spriteData["spin"] as Double).toFloat(), (spriteData["secondsToSpawn"] as Double + powerupTimer + 1).toFloat(), scale, playerID, textures)
                powerupInputBottles.add(bottle)
            }
        } catch (e: JSONException) {
            println(e)
        }

        bottles = inputBottles as ArrayList<Bottle>
        powerupBottles = powerupInputBottles as ArrayList<Bottle>

        totalNumberOfBottles = bottles.size
        bottles.add(Bottle(powerupId, playerID, scale * 150, 500, 1f, powerupTimer, scale, playerID, textures))

        // instancing objects for touch features
        for (i in 0 until tailLength) {
            touches[i] = Touch(i, false, textures)
            enemyTouches[i] = Touch(i, true, textures)
        }

        rotationSystem = MatrixRotation()
    }
}