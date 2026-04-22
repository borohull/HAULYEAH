package model;

import model.enums.CargoType;
import model.enums.Direction;
import model.enums.VehicleType;
import java.util.List;

public class Vehicle {

    private final String     id;
    private final VehicleType type;
    private Position position;
    private Route    route;

    /**
     * Index into route.getTilePath() — the tile the vehicle is currently ON.
     * Ping-pongs between 0 and size-1.
     */
    private int     routePathIndex = 0;
    /** True = travelling toward higher indices; false = travelling backward. */
    private boolean movingForward  = true;

    /** Direction the vehicle is currently traveling (set by SimulationEngine each tick). */
    private Direction travelDirection = Direction.EAST;

    /** Cargo currently carried by this vehicle. */
    private CargoType cargoType   = null;
    private int       cargoAmount = 0;

    /** Continuous tile-coordinate position for smooth rendering (set by SimulationEngine). */
    private double smoothX;
    private double smoothY;

    public Vehicle(String id, VehicleType type, Position position) {
        this.id       = id;
        this.type     = type;
        this.position = position;
        this.smoothX  = position.getX();
        this.smoothY  = position.getY();
    }


    public void restoreRouteState(int routePathIndex, boolean movingForward) {
        this.routePathIndex = Math.max(0, routePathIndex);
        this.movingForward = movingForward;
    }

    // ── Identity / stats ──────────────────────────────────────────────────────
    public String      getId()              { return id; }
    public VehicleType getType()            { return type; }
    public int         getSpeed()           { return type.getSpeed(); }
    public int         getCapacity()        { return type.getCapacity(); }
    public int         getMaintenanceCost() { return type.getMaintenanceCost(); }

    // ── Position ──────────────────────────────────────────────────────────────
    public Position getPosition()                  { return position; }
    public void     setPosition(Position position) { this.position = position; }

    // ── Smooth rendering position ─────────────────────────────────────────────
    public double getSmoothX() { return smoothX; }
    public double getSmoothY() { return smoothY; }
    public void   setSmoothPosition(double x, double y) {
        this.smoothX = x;
        this.smoothY = y;
    }

    // ── Route assignment ──────────────────────────────────────────────────────
    public void assignRoute(Route route) {
        this.route         = route;
        this.movingForward = true;
        if (route != null && route.hasTilePath()) {
            List<Position> path = route.getTilePath();
            // Start at the first stop in the path so the vehicle moves toward the second stop.
            // Falls back to path[0] if the route has no stops.
            int startIdx = 0;
            if (route.hasStops()) {
                Position firstStopPos = route.getStops().get(0).getPosition();
                for (int i = 0; i < path.size(); i++) {
                    if (path.get(i).equals(firstStopPos)) {
                        startIdx = i;
                        break;
                    }
                }
            }
            this.routePathIndex = startIdx;
            Position start = path.get(startIdx);
            this.position = start;
            this.smoothX  = start.getX();
            this.smoothY  = start.getY();
        } else {
            this.routePathIndex = 0;
        }
    }

    public Route      getRoute()              { return route; }
    public boolean    isMovingForward()       { return movingForward; }
    public Direction  getTravelDirection()    { return travelDirection; }
    public void       setTravelDirection(Direction d) { this.travelDirection = d; }

    // ── Path index ────────────────────────────────────────────────────────────
    public int getRoutePathIndex() { return routePathIndex; }

    /** Advance one step, flipping direction at each endpoint (ping-pong). */
    public void advanceRoutePathIndex() {
        if (route == null || !route.hasTilePath()) return;
        int size = route.getTilePath().size();
        if (movingForward) {
            if (routePathIndex >= size - 1) {
                movingForward = false; // flip only — stay at endpoint this call
            } else {
                routePathIndex++;
            }
        } else {
            if (routePathIndex <= 0) {
                movingForward = true; // flip only — stay at endpoint this call
            } else {
                routePathIndex--;
            }
        }
    }

    // ── Cargo ─────────────────────────────────────────────────────────────────
    public CargoType getCargoType()   { return cargoType; }
    public int       getCargoAmount() { return cargoAmount; }
    public boolean   isCarrying()     { return cargoType != null && cargoAmount > 0; }

    public void loadCargo(CargoType type, int amount) {
        this.cargoType   = type;
        this.cargoAmount = Math.min(amount, getCapacity());
    }

    /** Unloads all cargo and returns the amount that was carried. */
    public int unloadCargo() {
        int delivered = cargoAmount;
        cargoType   = null;
        cargoAmount = 0;
        return delivered;
    }
}
