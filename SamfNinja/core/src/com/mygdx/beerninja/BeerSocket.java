package com.mygdx.beerninja;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jdk.nashorn.api.scripting.JSObject;

public class BeerSocket {
    int player;
    private Socket socket;

    public void connect() {
        try {
            socket = IO.socket("http://localhost:8080");
            socket.connect();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public GeneratedBeerData generateSprites() {
        final List<List<Integer>> data;

        socket.on("bottleList", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
                System.out.println(args[0]);
                //data = receivedData.getString("bottleList");
            }
        });
        //testing function for now
        //
        // TODO: call server to set up connection
        final ArrayList<Integer> test1 = new ArrayList<Integer>()  {{
            // id of bottle
            add(0);
            // seconds to spawn
            add(0);
            // offset Y from top
            add(200);
            // player 1 or player 2
            add(1);
            // bottle velocity
            add(200);
        }};

        final ArrayList<Integer> test2 = new ArrayList<Integer>()  {{
            add(1);
            add(1);
            add(240);
            add(2);
            add(300);
        }};

        final ArrayList<Integer> test3 = new ArrayList<Integer>()  {{
            add(2);
            add(2);
            add(200);
            add(1);
            add(300);

        }};

        final ArrayList<Integer> test4 = new ArrayList<Integer>()  {{
            add(3);
            add(4);
            add(130);
            add(2);
            add(200);
        }};

        final ArrayList<Integer> test5 = new ArrayList<Integer>()  {{
            add(4);
            add(5);
            add(20);
            add(1);
            add(400);
        }};

        final ArrayList<Integer> test6 = new ArrayList<Integer>()  {{
            add(5);
            add(7);
            add(400);
            add(2);
            add(200);
        }};

        final ArrayList<List<Integer>> testData = new ArrayList<List<Integer>>() {{
            add(test1);
            add(test2);
            add(test3);
            add(test4);
            add(test5);
            add(test6);
        }};

        return new GeneratedBeerData(testData, this);
    }

    public boolean caughtBottle(int id, float xPos) {
        // send bottle id, player, current time and x-coordinates
        return false;
    }

}
