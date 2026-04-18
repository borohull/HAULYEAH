package model.service;

import model.GameState;
import model.Position;
import model.Route;
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

    private final VehicleService vehicleService = new VehicleService();

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

        for (Vehicle vehicle : state.getMap().getVehicles()) {
            if (vehicle.getRoute() != null
                    && vehicle.getRoute().hasTilePath()
                    && vehicle.getRoutePathIndex() == 0
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

        String vid         = vehicle.getId();
        double tilesPerSec = Math.max(0.5, vehicle.getSpeed() / SPEED_SCALE);
        double progress    = tileProgress.getOrDefault(vid, 0.0) + tilesPerSec * dt;

        int curIdx  = vehicle.getRoutePathIndex();
        int nextIdx = (curIdx + 1) % pathSize;

        // Check traffic light on the NEXT tile before crossing into it.
        // Skip the check when nextIdx wraps to 0 — the approach direction at the seam
        // of a looping route is geometrically undefined and falsely returns RED.
        if (nextIdx != 0) {
            TrafficLight tl = state.getMap().getTrafficLightAt(path.get(nextIdx));
            if (tl != null) {
                Direction approachDir = approachDirection(path.get(curIdx), path.get(nextIdx));
                if (approachDir != null && tl.getStateFor(approachDir) == LightState.RED) {
                    // Clamp but preserve any previously accumulated progress above the old clamp
                    // so on the very next GREEN frame the vehicle always advances past 1.0.
                    progress = Math.min(progress, 0.50);
                }
            }
        }

        // Each time progress passes 1.0 the vehicle reaches the next tile
        while (progress >= 1.0) {
            progress -= 1.0;
            vehicle.advanceRoutePathIndex();
            vehicle.setPosition(path.get(vehicle.getRoutePathIndex()));
            curIdx  = vehicle.getRoutePathIndex();
            nextIdx = (curIdx + 1) % pathSize;
            // Skip wrap-around boundary TL check (same reason as above)
            if (nextIdx == 0) break;
            TrafficLight tl = state.getMap().getTrafficLightAt(path.get(nextIdx));
            if (tl != null) {
                Direction approachDir = approachDirection(path.get(curIdx), path.get(nextIdx));
                if (approachDir != null && tl.getStateFor(approachDir) == LightState.RED) {
                    progress = Math.min(progress, 0.50);
                    break;
                }
            }
        }

        tileProgress.put(vid, progress);

        // ── Smooth sub-tile interpolation ─────────────────────────────────────
        curIdx  = vehicle.getRoutePathIndex();
        nextIdx = (curIdx + 1) % pathSize;
        Position from = path.get(curIdx);
        Position to   = path.get(nextIdx);

        vehicle.setSmoothPosition(
                lerp(from.getX(), to.getX(), progress),
                lerp(from.getY(), to.getY(), progress)
        );
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
