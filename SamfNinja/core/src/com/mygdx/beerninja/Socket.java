package com.mygdx.beerninja;

import java.util.ArrayList;
import java.util.List;

public class Socket {

    public boolean connect() {
        return false;
    }

    public List<List<Integer>> generateSprites() {
        //testing function for now
        //
        // TODO: call server to set up connection
        final ArrayList<Integer> test1 = new ArrayList<Integer>()  {{
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
            add(240);
            add(2);
            add(300);
        }};

        final ArrayList<Integer> test3 = new ArrayList<Integer>()  {{
            add(2);
            add(200);
            add(1);
            add(300);

        }};

        final ArrayList<Integer> test4 = new ArrayList<Integer>()  {{
            add(4);
            add(130);
            add(2);
            add(200);
        }};

        final ArrayList<Integer> test5 = new ArrayList<Integer>()  {{
            add(5);
            add(20);
            add(1);
            add(400);
        }};

        final ArrayList<Integer> test6 = new ArrayList<Integer>()  {{
            add(7);
            add(400);
            add(2);
            add(200);
        }};

        return new ArrayList<List<Integer>>() {{
            add(test1);
            add(test2);
            add(test3);
            add(test4);
            add(test5);
            add(test6);
        }};
    }

}
