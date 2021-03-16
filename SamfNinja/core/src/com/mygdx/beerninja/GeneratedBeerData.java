package com.mygdx.beerninja;

import java.util.ArrayList;
import java.util.List;

public class GeneratedBeerData {
    List<List<Integer>> data;
    Integer size;
    Socket socket;

    public GeneratedBeerData(List<List<Integer>> input, Socket inputSocket) {
        data = input;
        size = input.size();
        socket = inputSocket;
    }

    public List<Bottle> spawn(float gameTime, int screenHeight) {
        List<Bottle> bottles = new ArrayList<>();

        for (List<Integer> spriteData : data) {
            float beerSpawnTime = spriteData.get(1);

            if(gameTime > beerSpawnTime) {
                int bottleId = spriteData.get(0);
                int yPos = spriteData.get(2);
                int beerPlayer = spriteData.get(3);
                int bottleVelocity = spriteData.get(4);

                Bottle bottle = new Bottle(bottleId, beerPlayer, yPos, bottleVelocity, beerSpawnTime, screenHeight);
                bottles.add(bottle);
            }
        }

        return bottles;
    }

    public void caughtBottle(int id) {
        List<Integer> slicedBeer = null;
        
        for (List<Integer> spriteData : data) {
            int bottleId = spriteData.get(0);
            if (bottleId == id) {
                slicedBeer = spriteData;    
            }
        }

        socket.caughtBottle(id);
        data.remove(slicedBeer);
    }
}
