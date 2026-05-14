package model;

import model.enums.TileType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The game world — a fixed-size 2D grid together with all entities that inhabit it.
 *
 * <p>{@code Game} is the spatial root of the simulation. It holds every {@link Tile} in the
 * grid and maintains lists of domain objects: {@link City}, {@link Facility}, {@link WaterBody},
 * {@link Forest}, {@link Bridge}, {@link Road}, {@link Stop}, {@link Vehicle}, {@link Route},
 * and {@link TrafficLight}.
 *
 * <p>Construction and removal operations (add road, build bridge, …) are intentionally
 * extracted into {@link model.service.ConstructionService} to keep this class a pure data
 * container. {@code Game} only exposes the raw collections and lookup helpers.
 */
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
    private final Map<Position, TrafficLight> trafficLights;

    /**
     * Creates an empty game world of the given dimensions. All tiles start as
     * {@link model.enums.TileType#EMPTY}.
     *
     * @param width  number of columns
     * @param height number of rows
     */
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
        this.vehicles      = new ArrayList<>();
        this.routes        = new ArrayList<>();
        this.trafficLights = new HashMap<>();

        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Tile(x, y);
            }
        }
    }

    /**
     * Returns the tile at (x, y), or {@code null} if the coordinates are out of bounds.
     *
     * @param x column index
     * @param y row index
     */
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return grid[x][y];
    }

    /**
     * Returns the tile at the given {@link Position}, or {@code null} if out of bounds.
     *
     * @param p grid position
     */
    public Tile getTile(Position p) {
        return getTile(p.getX(), p.getY());
    }

    /**
     * Returns {@code true} if (x, y) is a valid grid coordinate.
     *
     * @param x column index
     * @param y row index
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    
    public int getWidth()  { return width; }
    public int getHeight() { return height; }
    public String getWorldName() { return worldName; }

    /**
     * Sets the world name shown in the HUD. Trims whitespace; falls back to
     * "Unnamed World" for {@code null} or blank input.
     *
     * @param worldName desired world name
     */
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
    public Map<Position, TrafficLight> getTrafficLights() { return trafficLights; }

    public void addTrafficLight(TrafficLight tl) {
        trafficLights.put(tl.getPosition(), tl);
    }

    public void removeTrafficLight(Position p) {
        trafficLights.remove(p);
    }

    public TrafficLight getTrafficLightAt(Position p) {
        return trafficLights.get(p);
    }

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
                t.setTreeCount(1 + Math.abs((p.getX() * 31 + p.getY() * 17) % 4));
                t.setEntityId(forest.getId());
                t.setEntityName(forest.getName());
            }
        }

    }


    // Construction methods (addRoad, addBridge, addStop, etc.) and validation
    // logic (isAdjacent...) have been moved to model.service.ConstructionService
    // to separate game logic from data representation.

    /**
     * Returns the road at position {@code p}, or {@code null} if none exists there.
     *
     * @param p grid position to query
     */
    public Road getRoadAt(Position p) {
        for (Road r : roads) {
            if (r.getPosition().equals(p)) return r;
        }
        return null;
    }

    /**
     * Returns the stop at position {@code p}, or {@code null} if none exists there.
     *
     * @param p grid position to query
     */
    public Stop getStopAt(Position p) {
        for (Stop s : stops) {
            if (s.getPosition().equals(p)) return s;
        }
        return null;
    }

    /**
     * Returns the bridge whose footprint contains position {@code p}, or {@code null}
     * if no bridge covers that tile.
     *
     * @param p grid position to query
     */
    public Bridge getBridgeAt(Position p) {
        for (Bridge b : bridges) {
            if (b.containsPosition(p.getX(), p.getY())) return b;
        }
        return null;
    }

    /**
     * Returns the positions of all orthogonal neighbours of {@code p} that are
     * {@link model.enums.TileType#ROAD} tiles.
     *
     * @param p centre position
     */
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

    /**
     * Returns a combined list of all map entities that have a visual presence on the map:
     * cities, facilities, water bodies, and bridges. Used for label rendering.
     */
    public List<MapEntity> getAllEntities() {
        List<MapEntity> entities = new ArrayList<>();
        entities.addAll(cities);
        entities.addAll(facilities);
        entities.addAll(waterBodies);
        entities.addAll(bridges);
        return entities;
    }

    private final java.util.List<Position> dirtyStaticTiles = new java.util.ArrayList<>();

    /**
     * Marks a tile as visually changed so the next render pass redraws the static layer
     * (used for forest growth).
     *
     * @param p the tile that changed
     */
    public void markStaticTileDirty(Position p) {
        dirtyStaticTiles.add(p);
    }

    /**
     * Returns and clears the list of tiles that have been marked dirty since the last call.
     * Called once per frame by the rendering layer.
     */
    public java.util.List<Position> consumeDirtyStaticTiles() {
        java.util.List<Position> copy = new java.util.ArrayList<>(dirtyStaticTiles);
        dirtyStaticTiles.clear();
        return copy;
    }

}
