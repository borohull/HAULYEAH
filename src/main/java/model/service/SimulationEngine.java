package model.service;

import model.GameState;
import model.Position;
import model.Route;
import model.Stop;
import model.TrafficLight;
import model.Vehicle;
import model.enums.Direction;
import model.enums.LightState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimulationEngine — advances the game simulation by dt seconds each frame.
 *
 * Movement logic (no BFS):
 *   routePathIndex = the tile the vehicle is currently standing ON.
 *   The vehicle always interpolates from path[routePathIndex]
 *                                     toward path[routePathIndex + 1].
 *   When progress reaches 1.0, routePathIndex advances (wraps around).
 */
public class SimulationEngine {

    private final VehicleService  vehicleService  = new VehicleService();
    private final DeliveryService deliveryService = new DeliveryService();

    // Speed values in VehicleType are 25–60 (game units).
    // Divide by SPEED_SCALE → tiles per second  (40 → 4 t/s).
    private static final double SPEED_SCALE = 40.0;

    /** Per-vehicle progress from current tile toward next tile (0.0 – <1.0). */
    private final Map<String, Double> tileProgress = new HashMap<>();

    // ── Public API ────────────────────────────────────────────────────────────

    public void tick(GameState state, double dt) {
        // Advance all traffic light phase timers
        for (TrafficLight tl : state.getMap().getTrafficLights().values()) {
            tl.tick(dt);
        }

        deliveryService.tickDemand(state.getMap(), dt);

        // Maintenance: drain maintenanceCost per game-minute per vehicle (aggregated, not logged individually)
        double maintenancePerSecond = 1.0 / 60.0;
        for (Vehicle vehicle : state.getMap().getVehicles()) {
            String routeName = (vehicle.getRoute() != null) ? vehicle.getRoute().getName() : "Unassigned";
            String key = vehicle.getType().getCategory() + " (" + vehicle.getType().name().replace("_", " ") + ") | Route: " + routeName;
            state.getPlayer().getLedger().recordMaintenance(key, vehicle.getMaintenanceCost() * maintenancePerSecond * dt);
        }

        for (Vehicle vehicle : state.getMap().getVehicles()) {
            if (vehicle.getRoute() != null
                    && vehicle.getRoute().hasTilePath()
                    && !tileProgress.containsKey(vehicle.getId())) {
                tileProgress.put(vehicle.getId(), 0.0);
            }
            tickVehicle(vehicle, dt, state);
        }
    }

    /** Call this when a vehicle is assigned a new route so it starts cleanly at tile 0. */
    public void resetVehicle(String vehicleId) {
        tileProgress.remove(vehicleId);
    }

    public VehicleService getVehicleService() { return vehicleService; }

    // ── Per-vehicle movement ──────────────────────────────────────────────────

    private void tickVehicle(Vehicle vehicle, double dt, GameState state) {
        Route route = vehicle.getRoute();
        if (route == null || !route.hasTilePath()) return;

        List<Position> path     = route.getTilePath();
        int            pathSize = path.size();
        if (pathSize < 2) return;

        String vid          = vehicle.getId();
        double tilesPerSec  = Math.max(0.5, vehicle.getSpeed() / SPEED_SCALE);
        double storedProgress = tileProgress.getOrDefault(vid, 0.0); // progress before this tick
        double progress       = storedProgress + tilesPerSec * dt;
        final  double STOP_LINE = 0.0; // vehicle waits here (clearly inside previous tile)

        boolean forward = vehicle.isMovingForward();
        int curIdx  = vehicle.getRoutePathIndex();
        int nextIdx = forward ? Math.min(curIdx + 1, pathSize - 1) : Math.max(curIdx - 1, 0);

        // Check traffic light on the NEXT tile before crossing into it.
        // curIdx == nextIdx means we're at an endpoint — nothing to cross.
        if (curIdx != nextIdx) {
            TrafficLight tl = state.getMap().getTrafficLightAt(path.get(nextIdx));
            if (tl != null) {
                Direction approachDir = approachDirection(path.get(curIdx), path.get(nextIdx));
                if (approachDir != null && tl.getStateFor(approachDir) == LightState.RED) {
                    // Only clamp if the vehicle hasn't yet committed to crossing.
                    // storedProgress > STOP_LINE means it moved past the stop line
                    // during a GREEN tick → allow it to complete; don't drag it back.
                    if (storedProgress <= STOP_LINE) {
                        progress = Math.min(progress, STOP_LINE);
                    }
                }
            }
        }

        // Each time progress passes 1.0 the vehicle reaches the next tile
        while (progress >= 1.0) {
            progress -= 1.0;
            vehicle.advanceRoutePathIndex();
            vehicle.setPosition(path.get(vehicle.getRoutePathIndex()));
            // Check for stop arrival
            Stop arrivedStop = DeliveryService.findStopAt(vehicle.getPosition(), state.getMap().getStops());
            if (arrivedStop != null) deliveryService.handleStopArrival(vehicle, arrivedStop, state);
            curIdx  = vehicle.getRoutePathIndex();
            forward = vehicle.isMovingForward();
            nextIdx = forward ? Math.min(curIdx + 1, pathSize - 1) : Math.max(curIdx - 1, 0);
            if (curIdx == nextIdx) break; // at an endpoint, stop advancing
            TrafficLight tl = state.getMap().getTrafficLightAt(path.get(nextIdx));
            if (tl != null) {
                Direction approachDir = approachDirection(path.get(curIdx), path.get(nextIdx));
                if (approachDir != null && tl.getStateFor(approachDir) == LightState.RED) {
                    // After advancing a tile, progress is small (just subtracted 1.0).
                    // Vehicle just arrived — unconditional clamp is safe here.
                    progress = Math.min(progress, STOP_LINE);
                    break;
                }
            }
        }

        tileProgress.put(vid, progress);

        // ── Smooth sub-tile interpolation ─────────────────────────────────────
        curIdx  = vehicle.getRoutePathIndex();
        forward = vehicle.isMovingForward();
        nextIdx = forward ? Math.min(curIdx + 1, pathSize - 1) : Math.max(curIdx - 1, 0);
        Position from = path.get(curIdx);
        Position to   = path.get(nextIdx);

        vehicle.setSmoothPosition(
                lerp(from.getX(), to.getX(), progress),
                lerp(from.getY(), to.getY(), progress)
        );

        // Update travel direction from actual movement vector this tick.
        // When from == to (endpoint), keep the previous direction unchanged.
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        if      (dx > 0) vehicle.setTravelDirection(Direction.EAST);
        else if (dx < 0) vehicle.setTravelDirection(Direction.WEST);
        else if (dy > 0) vehicle.setTravelDirection(Direction.SOUTH);
        else if (dy < 0) vehicle.setTravelDirection(Direction.NORTH);
        // dx==0 && dy==0 → at endpoint, leave previous direction intact
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Direction FROM which a vehicle approaches the junction (from → junction). */
    private static Direction approachDirection(Position from, Position junction) {
        int dx = junction.getX() - from.getX();
        int dy = junction.getY() - from.getY();
        if (dx == 1  && dy == 0) return Direction.WEST;  // vehicle came from west
        if (dx == -1 && dy == 0) return Direction.EAST;  // vehicle came from east
        if (dx == 0  && dy == 1) return Direction.NORTH; // vehicle came from north
        if (dx == 0  && dy == -1) return Direction.SOUTH; // vehicle came from south
        return null;
    }
}
