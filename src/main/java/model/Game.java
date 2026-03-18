package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level model object. Holds the tile grid and all game entities.
 * This is what gets passed around between controller, model services, and view.
 */
public class Game {
    private final int width;
    private final int height;
    private final Tile[][] grid;

    private final List<City> cities;
    private final List<Facility> facilities;

    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Tile[width][height];
        this.cities = new ArrayList<>();
        this.facilities = new ArrayList<>();

        // Initialise every tile as EMPTY
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Tile(x, y);
            }
        }
    }

    // --- Grid access ---
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return grid[x][y];
    }

    public Tile getTile(Position p) {
        return getTile(p.getX(), p.getY());
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    // --- Dimensions ---
    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    // --- Entity lists ---
    public List<City> getCities()           { return cities; }
    public List<Facility> getFacilities()   { return facilities; }

    public void addCity(City city) {
        cities.add(city);
        for (Position p : city.getTiles()) {
            Tile t = getTile(p);
            if (t != null) {
                t.setType(TileType.CITY);
                t.setEntityId(city.getId());
                t.setEntityName(city.getName());
            }
        }
    }

    public void addFacility(Facility facility) {
        facilities.add(facility);
        for (Position p : facility.getTiles()) {
            Tile t = getTile(p);
            if (t != null) {
                t.setType(TileType.FACILITY);
                t.setEntityId(facility.getId());
                t.setEntityName(facility.getName());
            }
        }
    }
}
