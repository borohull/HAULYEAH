package model;

public class Vehicle {

    private final String id;
    private final VehicleType type;
    private Position position;
    private Route route;
    private int currentStopIndex;

    public Vehicle(String id, VehicleType type, Position position) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.currentStopIndex = 0;
    }

    public String getId() {return id;}

    public VehicleType getType() {
        return type;
    }

    public Position getPosition() { return position;}

    public void setPosition(Position position) {
        this.position = position;
    }

    public void assignRoute(Route route) {
        this.route = route;
        this.currentStopIndex = 0;
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
}
