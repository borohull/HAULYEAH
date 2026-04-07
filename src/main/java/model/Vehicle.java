package model;

import model.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a vehicle in the game (bus, truck, train, etc.)
 *
 * Vehicle movement pattern:
 * 1. Start at a stop
 * 2. Travel through ROAD tiles (player-built roads)
 * 3. Enter a CITY via its entrance (CITY_ROAD border tile)
 * 4. Travel through CITY_ROAD tiles (internal city roads)
 * 5. Reach another stop (may be inside or outside city)
 * 6. Repeat for all stops in the route
 *
 * The pathfinding algorithm must handle:
 * - Navigation through ROAD tiles (external)
 * - Detection of city entrance points
 * - Navigation through CITY_ROAD tiles (internal)
 * - Reaching stops at any location
 */
public class Vehicle {

    private final String id;
    private final VehicleType type;
    private Position position;
    private Route route;
    private int currentStopIndex;
    private List<Position> currentPath;  // Current pathfinding waypoints

    public Vehicle(String id, VehicleType type, Position position) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.currentStopIndex = 0;
        this.currentPath = new ArrayList<>();
    }

    public String getId() {return id;}

    public VehicleType getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void assignRoute(Route route) {
        this.route = route;
        this.currentStopIndex = 0;
        this.currentPath.clear();
    }

    public Route getRoute() {
        return route;
    }

    public int getSpeed() {
        return type.getSpeed();
    }

    public int getCapacity() {
        return type.getCapacity();
    }

    public int getMaintenanceCost() {
        return type.getMaintenanceCost();
    }

    /**
     * Set the current path waypoints that the vehicle should follow.
     * This is computed by the pathfinding algorithm and updated as the vehicle moves.
     */
    public void setPath(List<Position> path) {
        this.currentPath = new ArrayList<>(path);
    }

    /**
     * Get the current waypoints the vehicle is following.
     */
    public List<Position> getCurrentPath() {
        return new ArrayList<>(currentPath);
    }

    /**
     * Get the next waypoint the vehicle should move towards.
     */
    public Position getNextWaypoint() {
        if (currentPath.isEmpty()) return null;
        return currentPath.get(0);
    }

    /**
     * Mark that vehicle has reached the next waypoint.
     */
    public void advanceWaypoint() {
        if (!currentPath.isEmpty()) {
            currentPath.remove(0);
        }
    }

    public int getCurrentStopIndex() {
        return currentStopIndex;
    }

    public void advanceToNextStop() {
        if (route != null && currentStopIndex < route.getStops().size() - 1) {
            currentStopIndex++;
        }
    }
}
