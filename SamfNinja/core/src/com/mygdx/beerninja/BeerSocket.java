package com.mygdx.beerninja;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.parser.JSONParser;

public class BeerSocket {
    String player;
    private Socket socket;
    JSONArray data;

    public void connect() {
        try {
            socket = IO.socket("http://localhost:8080");
            socket.connect();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public String getPlayer() {
        return player;
    }

    public GeneratedBeerData generateSprites() {
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
            Thread.sleep(1000);
            System.out.println(data);

            ArrayList<JSONObject> result = new ArrayList<>();
            if (data != null) {
                for (int i=0;i<data.length();i++){
                    result.add((JSONObject) data.get(i));
                }
            }

            System.out.println(result);
            return new GeneratedBeerData(result, this);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean caughtBottle(int id, float xPos) {
        // send bottle id, player, current time and x-coordinates
        return false;
    }

}
