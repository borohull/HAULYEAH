package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MapGenerator {

    private static final int CITY_W  = 3;
    private static final int CITY_H  = 3;
    private static final int FAC_W   = 2;
    private static final int FAC_H   = 2;
    private static final int PADDING = 2; 

    private static final String[] CITY_NAMES = {
        "Budapest", "Debrecen", "Pecs", "Gyongyos", "Nyiregyhaza"
    };

    
    private static final String[][] FACILITY_DATA = {
        { "Iron Mine",   "IRON",  "" },
        { "Lumber Mill", "WOOD",  "" },
        { "Steel Works", "STEEL", "IRON" },
        { "Paper Mill",  "PAPER", "WOOD" },
        { "Farm",        "WOOD",  "" },
    };

    private static final String[] WATER_NAMES  = { "River Danube", "Blue Lake", "Crystal Creek", "Silent Pond" };
    private static final String[] FOREST_NAMES = { "Dark Wood", "Pine Forest", "Birch Grove", "Oak Thicket" };

    private final Random rng;

    public MapGenerator() {
        this.rng = new Random();
    }

    public MapGenerator(long seed) {
        this.rng = new Random(seed);
    }

    
    public Game generate(int width, int height, int numCities, int numFacilities) {
        Game game = new Game(width, height);

        
        List<int[]> occupied = new ArrayList<>();

        
        int numWater = 2 + rng.nextInt(2); 
        for (int i = 0; i < numWater; i++) {
            int ww = 3 + rng.nextInt(3); 
            int wh = 2 + rng.nextInt(3); 
            int[] pos = findFreePosition(width, height, ww, wh, occupied);
            if (pos == null) continue;
            String name = WATER_NAMES[i % WATER_NAMES.length];
            WaterBody water = new WaterBody("water_" + i, name, pos[0], pos[1], ww, wh);
            game.addWaterBody(water);
            occupied.add(new int[]{ pos[0] - 1, pos[1] - 1, ww + 2, wh + 2 });

            
            placeBridgesOnWater(game, water, i);
        }

        
        int numForests = 2 + rng.nextInt(3); 
        for (int i = 0; i < numForests; i++) {
            int fw = 2 + rng.nextInt(3); 
            int fh = 2 + rng.nextInt(3); 
            int[] pos = findFreePosition(width, height, fw, fh, occupied);
            if (pos == null) continue;
            String name = FOREST_NAMES[i % FOREST_NAMES.length];
            Forest forest = new Forest("forest_" + i, name, pos[0], pos[1], fw, fh);
            game.addForest(forest);
            occupied.add(new int[]{ pos[0] - PADDING, pos[1] - PADDING,
                                    fw + PADDING * 2, fh + PADDING * 2 });
        }

        
        for (int i = 0; i < numCities; i++) {
            String name = CITY_NAMES[i % CITY_NAMES.length];
            int[] pos = findFreePosition(width, height, CITY_W, CITY_H, occupied);
            if (pos == null) continue;
            City city = new City("city_" + i, name, pos[0], pos[1], CITY_W, CITY_H);
            game.addCity(city);
            occupied.add(new int[]{ pos[0] - PADDING, pos[1] - PADDING,
                                    CITY_W + PADDING * 2, CITY_H + PADDING * 2 });
        }

        
        for (int i = 0; i < numFacilities && i < FACILITY_DATA.length; i++) {
            String[] data = FACILITY_DATA[i];
            List<String> produces = new ArrayList<>();
            List<String> consumes = new ArrayList<>();
            if (!data[1].isEmpty()) produces.add(data[1]);
            if (!data[2].isEmpty()) consumes.add(data[2]);
            int[] pos = findFreePosition(width, height, FAC_W, FAC_H, occupied);
            if (pos == null) continue;
            Facility fac = new Facility("facility_" + i, data[0], pos[0], pos[1],
                                        FAC_W, FAC_H, produces, consumes);
            game.addFacility(fac);
            occupied.add(new int[]{ pos[0] - PADDING, pos[1] - PADDING,
                                    FAC_W + PADDING * 2, FAC_H + PADDING * 2 });
        }

        return game;
    }

    
    private void placeBridgesOnWater(Game game, WaterBody water, int index) {
        int ox = water.getOrigin().getX();
        int oy = water.getOrigin().getY();
        int ww = water.getWidth();
        int wh = water.getHeight();

        Bridge bridge;
        if (ww >= wh) {
            int midRow = oy + wh / 2;
            bridge = new Bridge(
                    "bridge_" + index,
                    water.getName() + " Bridge",
                    ox, midRow,
                    ww,
                    Bridge.Orientation.HORIZONTAL
            );
        } else {
            int midCol = ox + ww / 2;
            bridge = new Bridge(
                    "bridge_" + index,
                    water.getName() + " Bridge",
                    midCol, oy,
                    wh,
                    Bridge.Orientation.VERTICAL
            );
        }

        game.addBridge(bridge);
    }

    
    
    

    
    private int[] findFreePosition(int mapW, int mapH, int entityW, int entityH,
                                   List<int[]> occupied) {
        if (mapW - entityW - 2 <= 0 || mapH - entityH - 2 <= 0) {
            return null;
        }
        for (int attempt = 0; attempt < 200; attempt++) {
            int x = 1 + rng.nextInt(Math.max(1, mapW - entityW - 2));
            int y = 1 + rng.nextInt(Math.max(1, mapH - entityH - 2));
            if (!overlaps(x, y, entityW, entityH, occupied)) {
                return new int[]{ x, y };
            }
        }
        return null;
    }

    private boolean overlaps(int x, int y, int w, int h, List<int[]> occupied) {
        for (int[] rect : occupied) {
            int rx = rect[0], ry = rect[1], rw = rect[2], rh = rect[3];
            if (x < rx + rw && x + w > rx && y < ry + rh && y + h > ry) {
                return true;
            }
        }
        return false;
    }
}
