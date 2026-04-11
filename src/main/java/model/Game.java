package model;

import model.enums.TileType;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final int width;
    private final int height;
    private final Tile[][] grid;
    private String worldName;

    private final List<City>       cities;
    private final List<Facility>   facilities;
    private final List<WaterBody>  waterBodies;
    private final List<Forest>     forests;
    private final List<Bridge>     bridges;
    private final List<Road>       roads;
    private final List<Stop>       stops;
    private final List<Vehicle>    vehicles;
    private final List<Route>      routes;

    public Game(int width, int height) {
        this.width  = width;
        this.height = height;
        this.grid   = new Tile[width][height];
        this.worldName = "Unnamed World";
        this.cities      = new ArrayList<>();
        this.facilities  = new ArrayList<>();
        this.waterBodies = new ArrayList<>();
        this.forests     = new ArrayList<>();
        this.bridges     = new ArrayList<>();
        this.roads       = new ArrayList<>();
        this.stops       = new ArrayList<>();
        this.vehicles    = new ArrayList<>();
        this.routes      = new ArrayList<>();

        
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
    public String getWorldName() { return worldName; }

    public void setWorldName(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            this.worldName = "Unnamed World";
            return;
        }
        this.worldName = worldName.trim();
    }

    
    public List<City>      getCities()      { return cities; }
    public List<Facility>  getFacilities()  { return facilities; }
    public List<WaterBody> getWaterBodies() { return waterBodies; }
    public List<Forest>    getForests()     { return forests; }
    public List<Bridge>    getBridges()     { return bridges; }
    public List<Road>      getRoads()       { return roads; }
    public List<Stop>      getStops()       { return stops; }
    public List<Vehicle>   getVehicles()    { return vehicles; }
    public List<Route>     getRoutes()      { return routes; }

    public void addVehicle(Vehicle v) { vehicles.add(v); }
    public void addRoute(Route r)     { routes.add(r); }

    public void addCity(City city) {
        cities.add(city);

        for (Position p : city.getBuildingTiles()) {
            Tile t = getTile(p);
            if (t != null) {
                t.setType(TileType.CITY);
                t.setEntityId(city.getId());
                t.setEntityName(city.getName());
            }
        }

        for (Position p : city.getEmptyTiles()) {
            Tile t = getTile(p);
            if (t != null) {
                t.setType(TileType.CITY_EMPTY);
                t.setEntityId(city.getId());
                t.setEntityName(city.getName());
            }
        }

        // City roads are removed - no internal roads
        for (Position p : city.getRoadTiles()) {
            Tile t = getTile(p);
            if (t != null) {
                t.setType(TileType.CITY_ROAD);
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


    // Construction methods (addRoad, addBridge, addStop, etc.) and validation
    // logic (isAdjacent...) have been moved to model.service.ConstructionService
    // to separate game logic from data representation.

    public Road getRoadAt(Position p) {
        for (Road r : roads) {
            if (r.getPosition().equals(p)) return r;
        }
        return null;
    }

    public Stop getStopAt(Position p) {
        for (Stop s : stops) {
            if (s.getPosition().equals(p)) return s;
        }
        return null;
    }

    // Returns the N/S/E/W neighbours of p that are also ROAD tiles.
    public List<Position> getAdjacentRoads(Position p) {
        int[][] deltas = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };
        List<Position> result = new ArrayList<>();
        for (int[] d : deltas) {
            Position neighbour = new Position(p.getX() + d[0], p.getY() + d[1]);
            Tile t = getTile(neighbour);
            if (t != null && t.getType() == TileType.ROAD) {
                result.add(neighbour);
            }
        }
        return result;
    }

    public List<MapEntity> getAllEntities() {
        List<MapEntity> entities = new ArrayList<>();
        entities.addAll(cities);
        entities.addAll(facilities);
        entities.addAll(forests);
        entities.addAll(waterBodies);
        entities.addAll(bridges);
        return entities;
    }
}
