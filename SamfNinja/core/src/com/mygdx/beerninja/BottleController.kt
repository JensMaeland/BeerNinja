package com.mygdx.beerninja

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class BottleController(game: GameView) {
    var playerID: String? = null
    var enemyID: String? = null
    @JvmField
    var myPoints = 0
    @JvmField
    var enemyPoints = 0
    var enemyTouchIndex = 0
    @JvmField
    var enemyTouches = HashMap<Int, Touch>()
    private var socket: Socket? = null
    var enemyBottlesData: JSONArray? = null
    var enemyBottles = ArrayList<JSONObject>()
    var parsedBottleData: ArrayList<JSONObject>
    var parsedPowerupData: ArrayList<JSONObject>
    var parsedTouchData: ArrayList<JSONObject>

    //String socketUrl = "http://46.101.52.4:8080";
    var socketUrl = "http://192.168.1.173:8080"
    var mapper: ObjectMapper
    var myResult: JSONObject? = null
    var enemyResult: JSONObject? = null
    fun connect(): Boolean {
        socket!!.on("connection") { args ->
            val receivedData = args[0] as JSONObject
            try {
                val connection = receivedData["connection"] as Boolean
                println(connection)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun setUpGame(multiplayer: Boolean) {
        socket!!.emit("setUpGame", multiplayer)
        socket!!.on("setUpGame") { args ->
            val receivedData = args[0] as JSONObject
            parsedBottleData = ArrayList()
            parsedPowerupData = ArrayList()
            parsedTouchData = ArrayList()
            try {
                playerID = receivedData.getString("playerID")
                enemyID = receivedData.getString("enemyID")
                val bottleData = receivedData["bottleList"] as JSONArray
                val powerupData = receivedData["powerupList"] as JSONArray
                for (i in 0 until bottleData.length()) {
                    parsedBottleData.add(bottleData[i] as JSONObject)
                    parsedPowerupData.add(powerupData[i] as JSONObject)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun sendCaughtBottle(bottle: CaughtBottle?) {
        try {
            val json = mapper.writeValueAsString(bottle)
            socket!!.emit("caughtBottle", json)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
    }

    fun sendTouches(gameState: BottleModel, multiplayer: Boolean) {
        if (!multiplayer) return
        try {
            val touchObject = JSONObject()
            val touches = mapper.writeValueAsString(gameState.touches)
            touchObject.put("touches", touches)
            touchObject.put("currentTouchIndex", gameState.currentTouchIndex)
            socket!!.emit("touches", touchObject)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getTouches(multiplayer: Boolean) {
        if (!multiplayer) return
        socket!!.on("touches") { args ->
            val receivedData = args[0] as JSONObject
            parsedTouchData.clear()
            try {
                enemyTouchIndex = receivedData.getInt("currentTouchIndex")
                val touchData = receivedData["touches"] as JSONObject
                for (i in 0 until touchData.length()) {
                    val touch = touchData[Integer.toString(i)] as JSONObject
                    val touchId = touch["id"] as Int
                    val touchXPos = touch["x"] as Int
                    val touchYPos = touch["y"] as Int
                    val touchDisplay = touch["display"] as Boolean
                    val enemyTouch = enemyTouches[touchId]
                    enemyTouch!!.x = touchXPos
                    enemyTouch.y = touchYPos
                    enemyTouch.display = touchDisplay
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun getPoints(game: GameView) {
        socket!!.on("points", Emitter.Listener { args ->
            if (game.gameState == null) return@Listener
            val receivedData = args[0] as JSONObject
            try {
                myPoints = receivedData.getInt(playerID)
                if (enemyID != "null") {
                    enemyPoints = receivedData.getInt(enemyID)
                    enemyBottlesData = receivedData["enemyBottles"] as JSONArray
                    if (enemyBottlesData != null) {
                        for (i in 0 until enemyBottlesData!!.length()) {
                            val newBottle = !enemyBottles.contains(enemyBottlesData!![i])
                            if (newBottle) {
                                val bottle = enemyBottlesData!![i] as JSONObject
                                enemyBottles.add(enemyBottlesData!![i] as JSONObject)
                                val bottleTime = bottle["time"] as Float
                                val caughtBottle = CaughtBottle(bottle["id"] as Int, bottleTime, 0f, 0f, (bottle["playerID"] as String))
                                game.gameState!!.caughtBottle(caughtBottle, game)
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
    }

    fun gameSummary(): Boolean {
        socket!!.on("gameSummary") { args ->
            val receivedData = args[0] as JSONObject
            try {
                myResult = receivedData["player"] as JSONObject
                println(myResult)
                if (enemyID != null) {
                    enemyResult = receivedData["enemy"] as JSONObject
                }
                playerID = null
                enemyID = null
                parsedBottleData = ArrayList()
                parsedPowerupData = ArrayList()
                parsedTouchData = ArrayList()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return false
    }

    init {
        try {
            socket = IO.socket(socketUrl).connect()
        } catch (e: Exception) {
            println(e)
        }
        for (i in 0 until (game.gameState?.tailLength ?: 0)) {
            enemyTouches[i] = Touch(i, true)
        }
        mapper = ObjectMapper()
        parsedBottleData = ArrayList()
        parsedPowerupData = ArrayList()
        parsedTouchData = ArrayList()
    }
}