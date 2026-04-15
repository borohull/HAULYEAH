package model;

import model.enums.VehicleType;

public class Vehicle {

    private final String     id;
    private final VehicleType type;
    private Position position;
    private Route    route;

    /**
     * Index into route.getTilePath() — the tile the vehicle is currently
     * travelling TOWARD.  Advances one step at a time and wraps around.
     */
    private int routePathIndex = 0;

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
        this.route          = route;
        this.routePathIndex = 0;
        // Snap to the first tile in the path so the vehicle starts in the right place
        if (route != null && route.hasTilePath()) {
            Position start = route.getTilePath().get(0);
            this.position = start;
            this.smoothX  = start.getX();
            this.smoothY  = start.getY();
        }
    }

    public Route getRoute() { return route; }

    // ── Path index ────────────────────────────────────────────────────────────
    public int getRoutePathIndex() { return routePathIndex; }

    /** Advance to the next tile in the route path, wrapping around at the end. */
    public void advanceRoutePathIndex() {
        if (route != null && route.hasTilePath()) {
            routePathIndex = (routePathIndex + 1) % route.getTilePath().size();
        }
    }
}
