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
    String player;
    int points;
    private Socket socket;
    JSONArray data;
    String socketUrl = "http://localhost:8080";

    public BeerSocket() {
        try {
            socket = IO.socket(socketUrl).connect();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void setUpGame() {
        socket.emit("setUpGame");
        socket.on("setUpGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                try {
                    player = receivedData.getString("id");
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

                try {
                    data = (JSONArray) receivedData.get("bottleList");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Thread.sleep(500);

            ArrayList<JSONObject> result = new ArrayList<>();
            if (data != null) {
                for (int i=0; i<data.length(); i++){
                    result.add((JSONObject) data.get(i));
                }
            }

            return new GenerateBeerFromData(result, this);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void caughtBottle(int id, double xPos) {
        float timestamp = System.currentTimeMillis();
        CaughtBottle bottle = new CaughtBottle(id, timestamp, xPos, player);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(bottle);
            socket.emit("caughtBottle", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void exchangeTouches(HashMap<Integer, Touch> touches) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(touches);
            socket.emit("sendTouches", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        socket.on("getTouches", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                try {
                    JSONObject enemyTouches = (JSONObject) receivedData.get("getTouches");
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
                    points = (int) receivedData.getInt("points");
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
