package com.mygdx.beerninja

import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.fasterxml.jackson.databind.ObjectMapper
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class GameController {
    private lateinit var socket: Socket
    private var socketUrl = "http://192.168.1.173:8080"
    private var mapper: ObjectMapper = ObjectMapper()

    private var newGameModel: GameModel? = null
    private var enemyCaughtBottles = ArrayList<JSONObject>()

    //String socketUrl = "http://46.101.52.4:8080";


    // socket connects to server initially
    private fun connect() {
        socket.on("connection") { args ->
            val receivedData = args[0] as JSONObject
            val connection = receivedData["connection"] as Boolean
            if (connection) {
                println("Tilkoblet server..")
            }
        }
    }

    fun setUpGame(multiplayer: Boolean, devMode: Boolean, username: String, scale: Int) : GameModel? {
        // requests the setup of a new game from server
        socket.emit("setUpGame", multiplayer, username)

        // waiting to receive a new game with details from the server
        socket.on("setUpGame") { args ->
            val bottleData = ArrayList<JSONObject>()
            val powerupData = ArrayList<JSONObject>()

            val receivedData = args[0] as JSONObject
            try {
                val playerID = receivedData.getString("playerID")
                val enemyID = receivedData.getString("enemyID")
                val enemyUsername = receivedData.getString("enemyUsername")
                val gameDuration = receivedData["gameDuration"] as Int
                val bottles = receivedData["bottleList"] as JSONArray
                val powerups = receivedData["powerupList"] as JSONArray

                for (i in 0 until bottles.length()) {
                    bottleData.add(bottles[i] as JSONObject)
                    powerupData.add(powerups[i] as JSONObject)
                }
                // create a new game model with provided data
                newGameModel = GameModel(this, playerID, enemyID, enemyUsername, bottleData, powerupData, multiplayer, devMode, gameDuration, scale)
            } catch (e: JSONException) {
                println(e)
            }
        }

        delay(500f)

        if (newGameModel != null) {
            // setup all socket/controller functions that will continuously communicate with server through the game
            sendTouches(newGameModel!!)
            getTouches(newGameModel!!)
            getPoints(newGameModel!!)
            listenForGameOver(newGameModel!!)
        }
        return newGameModel
    }

    private fun sendTouches(currentGameModel: GameModel) {
        if (!currentGameModel.multiplayer) return

        val touchObject = JSONObject()
        val touches = mapper.writeValueAsString(currentGameModel.touches)
        touchObject.put("touches", touches)
        touchObject.put("currentTouchIndex", currentGameModel.currentTouchIndex)
        socket.emit("touches", touchObject)
    }

    private fun getTouches(currentGameModel: GameModel) {
        if (!currentGameModel.multiplayer) return

        socket.on("touches") { args ->
            val receivedData = args[0] as JSONObject
            currentGameModel.enemyTouchIndex = receivedData.getInt("currentTouchIndex")
            val touchData = receivedData["touches"] as JSONObject
            currentGameModel.updateEnemyTouches(touchData)
        }
    }

    private fun getPoints(currentGameModel: GameModel) {
        socket.on("points") { args ->
            val receivedData = args[0] as JSONObject
            currentGameModel.myPoints = receivedData.getInt(currentGameModel.playerID)

            if (currentGameModel.enemyID != "null") {
                currentGameModel.enemyPoints = receivedData.getInt(currentGameModel.enemyID)
                val newEnemyBottles = receivedData["enemyBottles"] as JSONArray
                for (i in 0 until newEnemyBottles.length()) {
                    val newBottle = newEnemyBottles[i] as JSONObject
                    if (!enemyCaughtBottles.contains(newBottle)) {
                        enemyCaughtBottles.add(newBottle)
                        val bottleTime = (newBottle["time"] as Double).toFloat()
                        val caughtBottle = CaughtBottle(newBottle["id"] as Int, bottleTime, 0f, 0f, (newBottle["playerID"] as String), 0)
                        currentGameModel.enemyCaughtBottle(caughtBottle)
                    }
                }
            }
        }
    }

    fun sendCaughtBottle(bottle: CaughtBottle) {
        socket.emit("caughtBottle", mapper.writeValueAsString(bottle))
    }

    private fun listenForGameOver(currentGameModel: GameModel) {
        socket.on("gameSummary") { args ->
            val receivedData = args[0] as JSONObject
            currentGameModel.myResult = receivedData["player"] as JSONObject
            if (currentGameModel.enemyID != "") {
                currentGameModel.enemyResult = receivedData["enemy"] as JSONObject
            }
        }
    }

    init {
        try {
            socket = IO.socket(socketUrl).connect()
            connect()
        } catch (e: Exception) {
            println(e)
        }
    }
}