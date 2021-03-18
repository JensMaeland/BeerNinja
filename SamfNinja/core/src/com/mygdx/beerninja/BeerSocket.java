package com.mygdx.beerninja;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BeerSocket {
    String playerID = null;
    String enemyID = null;
    int myPoints;
    int enemyPoints;
    HashMap<Integer, Touch> enemyTouches = new HashMap<>();
    private Socket socket;
    JSONArray bottleData;
    JSONObject touchData;
    ArrayList<JSONObject> parsedBottleData;
    ArrayList<JSONObject> parsedTouchData;
    String socketUrl = "http://localhost:8080";
    ObjectMapper mapper;

    public BeerSocket(int tailLength) {
        try {
            socket = IO.socket(socketUrl).connect();
        } catch(Exception e) {
            System.out.println(e);
        }

        for (int i = 0; i < tailLength; i++) {
            enemyTouches.put(i, new Touch(i, 0, 0, 0, true));
        }

        mapper = new ObjectMapper();
        parsedBottleData = new ArrayList<>();
        parsedTouchData = new ArrayList<>();
    }

    public void setUpGame(boolean multiplayer) {
        socket.emit("setUpGame", multiplayer);
        socket.on("setUpGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                try {
                    playerID = receivedData.getString("playerID");
                    enemyID = receivedData.getString("enemyID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public GenerateBeerFromData generateSprites() {
        socket.emit("bottleList");
        socket.on("bottleList", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                parsedBottleData.clear();

                try {
                    bottleData = (JSONArray) receivedData.get("bottleList");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Thread.sleep(500);

            if (bottleData != null) {
                for (int i = 0; i< bottleData.length(); i++){
                    parsedBottleData.add((JSONObject) bottleData.get(i));
                }
            }

            return new GenerateBeerFromData(parsedBottleData, this);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void sendCaughtBottle(CaughtBottle bottle) {
        try {
            String json = mapper.writeValueAsString(bottle);
            socket.emit("caughtBottle", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void sendTouches(HashMap<Integer, Touch> myTouches) {
        try {
            String json = mapper.writeValueAsString(myTouches);
            socket.emit("touches", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void getTouches() {
        socket.on("touches", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                parsedTouchData.clear();

                try {
                    String touchDataString = (String) receivedData.get("touches");
                    JSONObject touchData = new JSONObject(touchDataString);

                    if (touchData != null) {
                        for (int i = 0; i< touchData.length(); i++){
                            parsedTouchData.add((JSONObject) touchData.get(Integer.toString(i)));
                        }
                    }

                    for (JSONObject touch : parsedTouchData) {
                        int touchId = (int) touch.get("id");
                        int touchXPos = (int) touch.get("x");
                        int touchYPos = (int) touch.get("y");
                        boolean touchDisplay = (boolean) touch.get("display");
                        double touchTime = (double) touch.get("time");

                        Touch enemyTouch = enemyTouches.get(touchId);
                        enemyTouch.x = touchXPos;
                        enemyTouch.y = touchYPos;
                        enemyTouch.display = touchDisplay;
                        enemyTouch.time = touchTime;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getPoints() {
        socket.on("getPoints", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject receivedData = (JSONObject) args[0];
                try {
                    myPoints = receivedData.getInt(playerID);
                    enemyPoints = receivedData.getInt(enemyID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean gameSummary() {

        return false;
    }
}
