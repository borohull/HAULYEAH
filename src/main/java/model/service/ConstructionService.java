package model.service;

import model.Bridge;
import model.City;
import model.Facility;
import model.Game;
import model.Position;
import model.Road;
import model.Stop;
import model.Tile;
import model.TrafficLight;
import model.enums.Direction;
import model.enums.TileType;

import java.util.ArrayList;
import java.util.List;

/**
 * ConstructionService — handles validation and execution of building operations.
 * Extracts the rules for what can be built where from the Game class.
 */
public class ConstructionService {

    /**
     * Attempts to build a road at the specified position.
     * Roads can only be built on EMPTY or FOREST tiles, not inside cities.
     */
    public boolean buildRoad(Game game, Position p, Road.RoadType type) {
        Tile t = game.getTile(p);
        if (t == null || (t.getType() != TileType.EMPTY && t.getType() != TileType.FOREST)) {
            return false;
        }

        // Cannot build roads inside cities
        for (City city : game.getCities()) {
            if (city.containsPosition(p.getX(), p.getY())) {
                return false;
            }
        }

        // Cannot build roads inside facilities
        for (Facility facility : game.getFacilities()) {
            if (facility.containsPosition(p.getX(), p.getY())) {
                return false;
            }
        }

        // Generate ID (could be passed in, but handled here for simplicity now)
        int numRoads = game.getRoads().size();
        Road road = new Road("road-" + (numRoads + 1), p.getX(), p.getY(), type);

        game.getRoads().add(road);
        t.setType(TileType.ROAD);
        t.setTreeCount(0);

        t.setEntityId(road.getId());
        t.setEntityName(null);
        return true;
    }

    /**
     * Removes a road at the specified position.
     */
    public boolean removeRoad(Game game, Position p) {
        Road found = game.getRoadAt(p);
        if (found == null) return false;
        
        game.getRoads().remove(found);
        Tile t = game.getTile(p);
        t.setType(TileType.EMPTY);
        t.setTreeCount(0);
        t.setEntityId(null);
        t.setEntityName(null);
        return true;
    }

    /**
     * Attempts to build a stop at the specified position.
     * Stops must be on an EMPTY tile adjacent to a ROAD, CITY_ROAD, CITY, or FACILITY.
     */
    public boolean buildStop(Game game, Position p, String stopName) {
        Tile t = game.getTile(p);
        if (t == null || t.getType() != TileType.EMPTY) {
            return false;
        }

        if (!isAdjacentToRoadCityOrFacility(game, p)) {
            return false;
        }

        boolean insideCity = isAdjacentToCityRoad(game, p);

        int numStops = game.getStops().size();
        Stop stop = new Stop("stop-" + (numStops + 1), p.getX(), p.getY(), stopName);
        stop.setInsideCity(insideCity);

        game.getStops().add(stop);
        
        // Use CITY_STOP for stops inside cities, STOP for external ones
        TileType stopType = insideCity ? TileType.CITY_STOP : TileType.STOP;
        t.setType(stopType);
        t.setEntityId(stop.getId());
        t.setEntityName(stop.getName());
        return true;
    }

    /**
     * Removes a stop at the specified position.
     */
    public boolean removeStop(Game game, Position p) {
        Stop found = game.getStopAt(p);
        if (found == null) return false;
        
        game.getStops().remove(found);
        Tile t = game.getTile(p);
        t.setType(TileType.EMPTY);
        t.setEntityId(null);
        t.setEntityName(null);
        return true;
    }

    /**
     * Attempts to build a bridge.
     */
    public boolean buildBridge(Game game, Bridge bridge) {
        if (bridge == null || bridge.getBridgeType() == null) {
            return false;
        }

        if (bridge.getLength() > bridge.getBridgeType().getMaxSpan()) {
            return false;
        }

        for (Position p : bridge.getTiles()) {
            Tile t = game.getTile(p);
            if (t == null || t.getType() != TileType.WATER) {
                return false;
            }
        }
        game.getBridges().add(bridge);
        for (Position p : bridge.getTiles()) {
            Tile t = game.getTile(p);
            t.setType(TileType.BRIDGE);
            t.setEntityId(bridge.getId());
            // Don't set entity name for bridges — they should be displayed like roads without labels
            t.setEntityName(null);
        }
        return true;
    }

    /**
     * Removes a bridge at the specified position.
     */
    public boolean removeBridge(Game game, Position p) {
        Bridge found = null;
        for (Bridge b : game.getBridges()) {
            if (b.containsPosition(p.getX(), p.getY())) {
                found = b;
                break;
            }
        }
        if (found == null) return false;

        game.getBridges().remove(found);
        for (Position pos : found.getTiles()) {
            Tile t = game.getTile(pos);
            t.setType(TileType.WATER);
            t.setEntityId(null);
            t.setEntityName(null);
        }
        return true;
    }

    // --- Traffic Light construction ---

    /**
     * Installs a traffic light at position p.
     * Requires p to be a ROAD tile that is a 3- or 4-way junction.
     */
    public boolean buildTrafficLight(Game game, Position p) {
        if (!isJunction(game, p)) return false;
        if (game.getTrafficLightAt(p) != null) return false; // already has one

        List<Direction> dirs = getActiveDirections(game, p);
        int count = game.getTrafficLights().size();
        TrafficLight tl = new TrafficLight("tl-" + (count + 1), p, dirs);
        game.addTrafficLight(tl);
        return true;
    }

    public boolean removeTrafficLight(Game game, Position p) {
        if (game.getTrafficLightAt(p) == null) return false;
        game.removeTrafficLight(p);
        return true;
    }

    /** Returns true if p is a ROAD tile with 3 or 4 adjacent ROAD neighbors. */
    public boolean isJunction(Game game, Position p) {
        Tile t = game.getTile(p);
        if (t == null || t.getType() != TileType.ROAD) return false;
        return game.getAdjacentRoads(p).size() >= 3;
    }

    /** Returns the Direction list corresponding to which neighbors are roads. */
    private List<Direction> getActiveDirections(Game game, Position p) {
        List<Direction> dirs = new ArrayList<>();
        int x = p.getX(), y = p.getY();
        if (isRoad(game, x,     y - 1)) dirs.add(Direction.NORTH);
        if (isRoad(game, x,     y + 1)) dirs.add(Direction.SOUTH);
        if (isRoad(game, x - 1, y    )) dirs.add(Direction.WEST);
        if (isRoad(game, x + 1, y    )) dirs.add(Direction.EAST);
        return dirs;
    }

    private boolean isRoad(Game game, int x, int y) {
        Tile t = game.getTile(x, y);
        return t != null && t.getType() == TileType.ROAD;
    }

    // --- Validation logic ---

    public boolean isAdjacentToRoadCityOrFacility(Game game, Position p) {
        int[][] deltas = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };
        for (int[] d : deltas) {
            Tile neighbour = game.getTile(p.getX() + d[0], p.getY() + d[1]);
            if (neighbour == null) continue;
            TileType type = neighbour.getType();
            if (type == TileType.ROAD || type == TileType.CITY_ROAD ||
                type == TileType.CITY || type == TileType.FACILITY) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdjacentToCityRoad(Game game, Position p) {
        int[][] deltas = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };
        for (int[] d : deltas) {
            Tile neighbour = game.getTile(p.getX() + d[0], p.getY() + d[1]);
            if (neighbour != null && neighbour.getType() == TileType.CITY_ROAD) {
                return true;
            }
        }
        return false;
    }
}
