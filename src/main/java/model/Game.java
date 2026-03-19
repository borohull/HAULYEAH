package model;

import java.util.ArrayList;
import java.util.List;


public class Game {
    private final int width;
    private final int height;
    private final Tile[][] grid;

    private final List<City>       cities;
    private final List<Facility>   facilities;
    private final List<WaterBody>  waterBodies;
    private final List<Forest>     forests;
    private final List<Bridge>     bridges;

    public Game(int width, int height) {
        this.width  = width;
        this.height = height;
        this.grid   = new Tile[width][height];
        this.cities      = new ArrayList<>();
        this.facilities  = new ArrayList<>();
        this.waterBodies = new ArrayList<>();
        this.forests     = new ArrayList<>();
        this.bridges     = new ArrayList<>();

        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Tile(x, y);
            }
        }
    }

    
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

    
    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    
    public List<City>      getCities()      { return cities; }
    public List<Facility>  getFacilities()  { return facilities; }
    public List<WaterBody> getWaterBodies() { return waterBodies; }
    public List<Forest>    getForests()     { return forests; }
    public List<Bridge>    getBridges()     { return bridges; }

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

    public void addWaterBody(WaterBody water) {
        waterBodies.add(water);
        for (Position p : water.getTiles()) {
            Tile t = getTile(p);
            if (t != null) {
                t.setType(TileType.WATER);
                t.setEntityId(water.getId());
                t.setEntityName(water.getName());
            }
        }
    }

    public void addForest(Forest forest) {
        forests.add(forest);
        for (Position p : forest.getTiles()) {
            Tile t = getTile(p);
            if (t != null) {
                t.setType(TileType.FOREST);
                t.setEntityId(forest.getId());
                t.setEntityName(forest.getName());
            }
        }
    }

    
    public boolean addBridge(Bridge bridge) {
        for (Position p : bridge.getTiles()) {
            Tile t = getTile(p);
            if (t == null || t.getType() != TileType.WATER) {
                return false; 
            }
        }
        bridges.add(bridge);
        for (Position p : bridge.getTiles()) {
            Tile t = getTile(p);
            t.setType(TileType.BRIDGE);
            t.setEntityId(bridge.getId());
            t.setEntityName(bridge.getName());
        }
        return true;
    }
}
