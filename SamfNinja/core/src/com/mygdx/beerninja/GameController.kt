package com.mygdx.beerninja

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.fasterxml.jackson.databind.ObjectMapper
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class GameController {
    private lateinit var socket: Socket
    private var socketUrl = "http://192.168.1.112:8080"
    private var mapper: ObjectMapper = ObjectMapper()
    var connected = false

    private var enemyCaughtBottles = ArrayList<JSONObject>()
    var newGameModel: GameModel? = null
    var loadingGame: RouteRequest? = null

    //String socketUrl = "http://46.101.52.4:8080";


    // socket connects to server initially
    private fun connect() {
        socket.on("connection") { args ->
            val receivedData = args[0] as JSONObject
            val connection = receivedData["connection"] as Boolean
            if (connection) {
                connected = true
            }
        }
    }

    fun setUpGame(gameRequest: RouteRequest, username: String, scale: Int, drawer: SpriteBatch, soundManager: AssetManager, textures: HashMap<String, Texture>) {
        // requests the setup of a new game from server
        loadingGame = gameRequest
        socket.emit("setUpGame", gameRequest.multiplayer, username)

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
                loadingGame = null
                newGameModel = GameModel(this, playerID, enemyID, username, enemyUsername, bottleData, powerupData, gameRequest.multiplayer, gameRequest.devMode, gameDuration, scale, drawer, soundManager, textures)
                if (newGameModel != null) {
                    // setup all socket/controller functions that will continuously communicate with server through the game
                    getTouches(newGameModel!!)
                    getPoints(newGameModel!!)
                    listenForGameOver(newGameModel!!)
                }
            } catch (e: JSONException) {
                println(e)
            }
        }
    }

    fun sendTouches(currentGameModel: GameModel) {
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
            println(receivedData)
            currentGameModel.enemyTouchIndex = receivedData.getInt("currentTouchIndex")
            val touchData = receivedData["touches"] as JSONObject
            currentGameModel.updateEnemyTouches(touchData)
        }
    }

    private fun getPoints(currentGameModel: GameModel) {
        socket.on("points") { args ->
            val receivedData = args[0] as JSONObject
            currentGameModel.myPoints = receivedData.getInt(currentGameModel.playerID)

            if (currentGameModel.enemyID.isNotEmpty()) {
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
            newGameModel = null
            socket.off("touches");
            socket.off("points");
            socket.off("gameSummary");
            val receivedData = args[0] as JSONObject
            currentGameModel.myResult = receivedData["player"] as JSONObject
            if (currentGameModel.enemyID.isNotEmpty()) {
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