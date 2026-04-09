package model;

import model.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {

    private final String id;
    private final VehicleType type;
    private Position position;
    private Route route;
    private int currentStopIndex;
    private List<Position> currentPath;

    public Vehicle(String id, VehicleType type, Position position) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.currentStopIndex = 0;
        this.currentPath = new ArrayList<>();
    }

    public String      getId()              { return id; }
    public VehicleType getType()            { return type; }
    public int         getSpeed()           { return type.getSpeed(); }
    public int         getCapacity()        { return type.getCapacity(); }
    public int         getMaintenanceCost() { return type.getMaintenanceCost(); }

    public Position getPosition()                  { return position; }
    public void     setPosition(Position position) { this.position = position; }

    public void assignRoute(Route route) {
        this.route = route;
        this.currentStopIndex = 0;
        this.currentPath.clear();
    }

    public Route getRoute()            { return route; }
    public int   getCurrentStopIndex() { return currentStopIndex; }

    public void advanceToNextStop() {
        if (route != null && !route.getStops().isEmpty()) {
            currentStopIndex = (currentStopIndex + 1) % route.getStops().size();
        }
    }

    public void setPath(List<Position> path) {
        this.currentPath = new ArrayList<>(path);
    }

    public List<Position> getCurrentPath() { return new ArrayList<>(currentPath); }

    public Position getNextWaypoint() {
        return currentPath.isEmpty() ? null : currentPath.get(0);
    }

    public void advanceWaypoint() {
        if (!currentPath.isEmpty()) currentPath.remove(0);
    }
}
