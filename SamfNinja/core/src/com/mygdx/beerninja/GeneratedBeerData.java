package com.mygdx.beerninja;

import java.util.ArrayList;
import java.util.List;

public class GeneratedBeerData {
    List<List<Integer>> data;

    public GeneratedBeerData(List<List<Integer>> input) {
        data = input;
    }

    public int size() {
        return data.size();
    }

    public List<Bottle> spawn(float gameTime, int screenHeight) {
        List<Bottle> bottles = new ArrayList<>();

        for (List<Integer> spriteData : data) {
            float beerSpawnTime = spriteData.get(0);

            if(gameTime > beerSpawnTime) {
                int yPos = spriteData.get(1);
                int beerPlayer = spriteData.get(2);
                int bottleVelocity = spriteData.get(3);

                Bottle bottle = new Bottle(beerPlayer, yPos, bottleVelocity, beerSpawnTime, screenHeight);
                bottles.add(bottle);
            }
        }

        return bottles;
    }
}
