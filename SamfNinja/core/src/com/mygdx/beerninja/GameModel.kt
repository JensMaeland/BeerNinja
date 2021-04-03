package com.mygdx.beerninja

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sign

class GameModel(private var controller: GameController, var playerID: String, var enemyID: String, var username: String, var enemyUsername: String, bottleData: ArrayList<JSONObject>, powerupData: ArrayList<JSONObject>, var multiplayer: Boolean, var devMode: Boolean, var gameDuration: Int, scale: Int, private var drawer: SpriteBatch, private var soundManager: AssetManager, textures: HashMap<String, Texture>) {
    // bottle state
    private var bottles: ArrayList<Bottle>
    private var powerupBottles: ArrayList<Bottle>
    var latestCaughtBottle: CaughtBottle? = null

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
    private var numberOfSpawnedBottles = 0
    var streak = 0

    private var tailLength = 35 // set length of tail when touching the screen
    var timer = -2f // set seconds until game start
    val powerUpTimer = 18f // set when the powerUp should appear

    fun spawn(devMode: Boolean): List<Bottle> {
        val currentBottles: ArrayList<Bottle> = ArrayList()
        var newNumberOfBottles = 0

        for (bottle in bottles) {
            if (timer > bottle.beerSpawnTime) {
                if (timer < bottle.beerSpawnTime + 10) {
                    currentBottles.add(bottle)
                }
                newNumberOfBottles += 1
            }
        }

        // play sound when spawning new bottles
        if (!devMode && newNumberOfBottles > numberOfSpawnedBottles && !devMode) {
            soundManager.get("pop.mp3", Sound::class.java).play()
            numberOfSpawnedBottles = newNumberOfBottles
        }

        return currentBottles
    }

    private fun playerCaughtBottle(caughtBottle: CaughtBottle) {
        for (bottle in bottles) {
            if (bottle.bottleId == caughtBottle.id) {
                bottles.remove(bottle)
                if (caughtBottle.id != 420) {
                    controller.sendCaughtBottle(caughtBottle)
                } else {
                    bottles.addAll(powerupBottles)
                }
                break
            }
        }

        if (!devMode) {
            if (caughtBottle.playerID == playerID || caughtBottle.playerID == "420") {
                soundManager.get("break.mp3", Sound::class.java).play()
            }
            else {
                soundManager.get("crush.mp3", Sound::class.java).play()
            }
        }
    }

    fun enemyCaughtBottle(caughtBottle: CaughtBottle) {
        for (bottle in bottles) {
            if (bottle.bottleId == caughtBottle.id) {
                bottles.remove(bottle)
                caughtBottle.xcoor = bottle.getXOffset(timer)
                caughtBottle.ycoor = bottle.getYOffset(timer)
                latestCaughtBottle = caughtBottle
                break
            }
        }
    }

    fun checkHitboxes(gameTimer: Float, scale: Int, devMode: Boolean) {
        // get latest touch object for player
        var touch = touches[tailLength - 1]
        if (currentTouchIndex > 0) {
            touch = touches[currentTouchIndex - 1]
        }
        val timestamp = System.currentTimeMillis().toFloat()
        val scaleX = Gdx.graphics.width / 100
        val scaleY = Gdx.graphics.height / 100
        val touchX = touch!!.x * scaleX
        val touchY = Gdx.graphics.height - touch.y * scaleY

        val beerBottles = spawn(devMode)
        val numberOfBottles = 50
        // check all bottle hitboxes
        for (beerBottle in beerBottles) {
            val hitbox = beerBottle.getHitbox(gameTimer, drawer, devMode, scale)

            // check touch hits with bottles
            if (touch.display && hitbox.left <= touchX && touchX <= hitbox.right) {
                if (hitbox.bottom <= touchY && touchY <= hitbox.top) {
                    // if touch hits a bottle, create a new caughtBottle
                    val caughtBottle = CaughtBottle(beerBottle.bottleId, gameTimer, beerBottle.getXOffset(gameTimer), beerBottle.getYOffset(gameTimer), beerBottle.bottlePlayerId, streak + 1)
                    playerCaughtBottle(caughtBottle)
                    latestCaughtBottle = caughtBottle
                    streak = (streak + 1).coerceAtMost(5)
                }
            }

            // check bottle hitbox with other bottles
            if (hitbox.left > 0 && hitbox.top > 0 && hitbox.left < Gdx.graphics.width && hitbox.top < Gdx.graphics.height && !beerBottle.collision) {
                for (compareBottle in beerBottles) {
                    val obstacle = compareBottle.getHitbox(gameTimer, drawer, devMode, scale)
                    if (beerBottle.bottlePlayerId != compareBottle.bottlePlayerId && beerBottle.bottleId < numberOfBottles && compareBottle.bottleId < numberOfBottles) {
                        if (hitbox.right > obstacle.left && hitbox.left < obstacle.right) {
                            if (hitbox.top > obstacle.bottom && hitbox.bottom < obstacle.top) {
                                beerBottle.xStartPos = beerBottle.getXOffset(gameTimer).toInt()
                                beerBottle.collision = true
                            }
                        }
                    }
                }
            }
        }
    }

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
        currentTouchIndex = (currentTouchIndex + 1) % tailLength
        return touch
    }

    fun checkTouchDirectionChange(x: Int, y: Int) : Boolean {
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

        val directionChange = prevPrevTouch != null && prevTouch != null && prevPrevTouch.display && prevTouch.display &&
                ((sign((prevTouch.x - prevPrevTouch.x).toDouble()) != sign((x - prevTouch.x).toDouble())) && (prevTouch.x != prevPrevTouch.x) && (x != prevTouch.x)
                || (sign((prevTouch.y - prevPrevTouch.y).toDouble()) != sign((y - prevTouch.y).toDouble())) && (prevTouch.y != prevPrevTouch.y) && (y != prevTouch.y))

        if (directionChange) {
            streak = 0
        }

        return directionChange
    }

    fun hideTouches() {
        for (i in 0 until touches.size) {
            val touch = touches[i]
            touch!!.display = false
        }
    }

    fun getGameSummary() : String {
        if (!multiplayer) {
            return myResult!!.getString("username") + " fikk " + myResult!!.getInt("score") + " poeng!"
        }

        return when {
            myPoints > enemyPoints -> {
                myResult!!.getString("username") + " vant med " + myResult!!.getInt("score") + " poeng!"
            }
            myPoints < enemyPoints -> {
                enemyResult!!.getString("username") + " vant med " + enemyResult!!.getInt("score") + "poeng!"
            }
            else -> {
                "Spillet ble uavgjort med " + myResult!!.getInt("score") + " poeng!"
            }
        }
    }

    init {
        val inputBottles: MutableList<Bottle> = ArrayList()
        val powerupInputBottles: MutableList<Bottle> = ArrayList()
        try {
            for (spriteData in bottleData) {
                val bottle = Bottle(spriteData["id"] as Int, (spriteData["playerID"] as String), spriteData["offsetY"] as Int, spriteData["velocity"] as Int,
                        (spriteData["spin"] as Double).toFloat(), (spriteData["secondsToSpawn"] as Double).toFloat(), scale, playerID, textures)
                inputBottles.add(bottle)
            }
            for (spriteData in powerupData) {
                val bottle = Bottle(spriteData["id"] as Int, (spriteData["playerID"] as String), spriteData["offsetY"] as Int, spriteData["velocity"] as Int,
                        (spriteData["spin"] as Double).toFloat(), (spriteData["secondsToSpawn"] as Double + powerUpTimer + 1).toFloat(), scale, playerID, textures)
                powerupInputBottles.add(bottle)
            }
        } catch (e: JSONException) {
            println(e)
        }

        bottles = inputBottles as ArrayList<Bottle>
        powerupBottles = powerupInputBottles as ArrayList<Bottle>
        bottles.add(Bottle(420, "420", scale * 150, 500, 1f, powerUpTimer, scale, playerID, textures))

        // instancing objects for touch feature
        for (i in 0 until tailLength) {
            touches[i] = Touch(i, false, textures)
            enemyTouches[i] = Touch(i, true, textures)
        }
    }
}