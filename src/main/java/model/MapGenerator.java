package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates the initial game map.
 * Places a set of cities (3×3) and facilities (2×2) at non-overlapping positions,
 * leaving everything else as EMPTY tiles.
 */
public class MapGenerator {

    private static final int CITY_W  = 3;
    private static final int CITY_H  = 3;
    private static final int FAC_W   = 2;
    private static final int FAC_H   = 2;
    private static final int PADDING = 2; // minimum gap between placed entities

    // Predefined city names and facility data
    private static final String[] CITY_NAMES = {
        "Budapest", "Debrecen", "Pecs", "Gyongyos", "Nyiregyhaza"
    };

    // { name, produces, consumes }
    private static final String[][] FACILITY_DATA = {
        { "Iron Mine",   "IRON",  "" },
        { "Lumber Mill", "WOOD",  "" },
        { "Steel Works", "STEEL", "IRON" },
        { "Paper Mill",  "PAPER", "WOOD" },
        { "Farm",        "WOOD",  "" },
    };

    private final Random rng;

    public MapGenerator() {
        this.rng = new Random();
    }

    public MapGenerator(long seed) {
        this.rng = new Random(seed);
    }

    /**
     * Generates a new Game on a map of the given size.
     *
     * @param width  map width in tiles
     * @param height map height in tiles
     * @param numCities    how many cities to place
     * @param numFacilities how many facilities to place
     */
    public Game generate(int width, int height, int numCities, int numFacilities) {
        Game game = new Game(width, height);

        // Track occupied rectangles to avoid overlap
        List<int[]> occupied = new ArrayList<>(); // [x, y, w, h]

        // --- Place cities ---
        for (int i = 0; i < numCities; i++) {
            String name = CITY_NAMES[i % CITY_NAMES.length];
            String id   = "city_" + i;
            int[] pos   = findFreePosition(width, height, CITY_W, CITY_H, occupied);
            if (pos == null) continue; // map is full — skip
            City city = new City(id, name, pos[0], pos[1], CITY_W, CITY_H);
            game.addCity(city);
            occupied.add(new int[]{ pos[0] - PADDING, pos[1] - PADDING,
                                    CITY_W + PADDING * 2, CITY_H + PADDING * 2 });
        }

        // --- Place facilities ---
        for (int i = 0; i < numFacilities && i < FACILITY_DATA.length; i++) {
            String[] data = FACILITY_DATA[i];
            String id   = "facility_" + i;
            String name = data[0];
            List<String> produces = new ArrayList<>();
            List<String> consumes = new ArrayList<>();
            if (!data[1].isEmpty()) produces.add(data[1]);
            if (!data[2].isEmpty()) consumes.add(data[2]);

            int[] pos = findFreePosition(width, height, FAC_W, FAC_H, occupied);
            if (pos == null) continue;
            Facility fac = new Facility(id, name, pos[0], pos[1], FAC_W, FAC_H, produces, consumes);
            game.addFacility(fac);
            occupied.add(new int[]{ pos[0] - PADDING, pos[1] - PADDING,
                                    FAC_W + PADDING * 2, FAC_H + PADDING * 2 });
        }

        return game;
    }

    /**
     * Tries up to 200 random positions to find one that doesn't overlap
     * any already-occupied rectangle.
     * Returns [x, y] or null if no free spot was found.
     */
    private int[] findFreePosition(int mapW, int mapH, int entityW, int entityH,
                                   List<int[]> occupied) {
        for (int attempt = 0; attempt < 200; attempt++) {
            int x = 1 + rng.nextInt(mapW - entityW - 2);
            int y = 1 + rng.nextInt(mapH - entityH - 2);
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
