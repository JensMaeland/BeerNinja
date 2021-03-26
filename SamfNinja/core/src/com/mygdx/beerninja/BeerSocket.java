package com.mygdx.beerninja;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BeerSocket {
    String playerID = null;
    String enemyID = null;
    int myPoints;
    int enemyPoints;
    int enemyTouchIndex;
    HashMap<Integer, Touch> enemyTouches = new HashMap<>();
    private Socket socket;
    JSONArray enemyBottlesData;
    ArrayList<JSONObject> enemyBottles = new ArrayList<>();
    ArrayList<JSONObject> parsedBottleData;
    ArrayList<JSONObject> parsedPowerupData;
    ArrayList<JSONObject> parsedTouchData;
    String socketUrl = "http://46.101.52.4:8080";
    //String socketUrl = "http://192.168.1.173:8080";
    ObjectMapper mapper;

    JSONObject myResult;
    JSONObject enemyResult;

    public BeerSocket(int tailLength) {
        try {
            socket = IO.socket(socketUrl).connect();
        } catch(Exception e) {
            System.out.println(e);
        }

        for (int i = 0; i < tailLength; i++) {
            enemyTouches.put(i, new Touch(i, 0, 0, true));
        }

        mapper = new ObjectMapper();
        parsedBottleData = new ArrayList<>();
        parsedPowerupData = new ArrayList<>();
        parsedTouchData = new ArrayList<>();
    }

    public boolean connect() {
        socket.on("connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                try {
                    boolean connection = (boolean) receivedData.get("connection");
                    System.out.println(connection);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return false;
    }

    public void setUpGame(boolean multiplayer) {
        socket.emit("setUpGame", multiplayer);
        socket.on("setUpGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                parsedBottleData.clear();
                parsedPowerupData.clear();

                try {
                    playerID = receivedData.getString("playerID");
                    enemyID = receivedData.getString("enemyID");
                    JSONArray bottleData = (JSONArray) receivedData.get("bottleList");
                    JSONArray powerupData = (JSONArray) receivedData.get("powerupList");

                    if (bottleData != null) {
                        for (int i = 0; i< bottleData.length(); i++){
                            parsedBottleData.add((JSONObject) bottleData.get(i));
                            parsedPowerupData.add((JSONObject) powerupData.get(i));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendCaughtBottle(CaughtBottle bottle) {
        try {
            String json = mapper.writeValueAsString(bottle);
            socket.emit("caughtBottle", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void sendTouches(HashMap<Integer, Touch> myTouches, int currentTouchIndex, boolean multiplayer) {
        if (!multiplayer) return;

        try {
            JSONObject touchObject = new JSONObject();

            String touches = mapper.writeValueAsString(myTouches);
            touchObject.put("touches", touches);
            touchObject.put("currentTouchIndex", currentTouchIndex);

            socket.emit("touches", touchObject);
        } catch (JsonProcessingException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void getTouches(boolean multiplayer) {
        if (!multiplayer) return;

        socket.on("touches", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                parsedTouchData.clear();

                try {
                    enemyTouchIndex = receivedData.getInt("currentTouchIndex");
                    JSONObject touchData = (JSONObject) receivedData.get("touches");

                    for (int i = 0; i< touchData.length(); i++){
                        JSONObject touch = (JSONObject) touchData.get(Integer.toString(i));
                        int touchId = (int) touch.get("id");
                        int touchXPos = (int) touch.get("x");
                        int touchYPos = (int) touch.get("y");
                        boolean touchDisplay = (boolean) touch.get("display");

                        Touch enemyTouch = enemyTouches.get(touchId);
                        enemyTouch.x = touchXPos;
                        enemyTouch.y = touchYPos;
                        enemyTouch.display = touchDisplay;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getPoints(final SamfNinja game) {
        socket.on("points", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                try {
                    myPoints = receivedData.getInt(playerID);
                    if (!enemyID.equals("null")) {
                        enemyPoints = receivedData.getInt(enemyID);
                        enemyBottlesData = (JSONArray) receivedData.get("enemyBottles");

                        if (enemyBottlesData != null) {
                            for (int i = 0; i< enemyBottlesData.length(); i++){
                                boolean newBottle = !enemyBottles.contains(enemyBottlesData.get(i));
                                if (newBottle) {
                                    JSONObject bottle = (JSONObject) enemyBottlesData.get(i);
                                    enemyBottles.add((JSONObject) enemyBottlesData.get(i));

                                    double bottleTime = (double) bottle.get("time");

                                    CaughtBottle caughtBottle = new CaughtBottle((int) bottle.get("id"), bottleTime, 0, 0, (String) bottle.get("playerID"));
                                    game.generatedSprites.caughtBottle(caughtBottle, game);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean gameSummary() {
        socket.on("gameSummary", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                try {
                    myResult = (JSONObject) receivedData.get("player");
                    System.out.println(myResult);
                    if (enemyID != null) {
                        enemyResult = (JSONObject) receivedData.get("enemy");
                    }

                    playerID = null;
                    enemyID = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return false;
    }
}
