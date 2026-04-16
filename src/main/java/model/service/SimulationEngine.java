package model.service;

import model.GameState;
import model.Position;
import model.Route;
import model.Vehicle;

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
        for (Vehicle vehicle : state.getMap().getVehicles()) {
            // Reset progress when vehicle is freshly assigned to a route
            // (routePathIndex == 0 and no progress recorded yet = fresh assignment)
            if (vehicle.getRoute() != null
                    && vehicle.getRoute().hasTilePath()
                    && vehicle.getRoutePathIndex() == 0
                    && !tileProgress.containsKey(vehicle.getId())) {
                tileProgress.put(vehicle.getId(), 0.0);
            }
            tickVehicle(vehicle, dt);
        }
    }

    /** Call this when a vehicle is assigned a new route so it starts cleanly at tile 0. */
    public void resetVehicle(String vehicleId) {
        tileProgress.remove(vehicleId);
    }

    public VehicleService getVehicleService() { return vehicleService; }

    // ── Per-vehicle movement ──────────────────────────────────────────────────

    private void tickVehicle(Vehicle vehicle, double dt) {
        Route route = vehicle.getRoute();
        if (route == null || !route.hasTilePath()) return;

        List<Position> path     = route.getTilePath();
        int            pathSize = path.size();
        if (pathSize < 2) return;

        String vid         = vehicle.getId();
        double tilesPerSec = Math.max(0.5, vehicle.getSpeed() / SPEED_SCALE);
        double progress    = tileProgress.getOrDefault(vid, 0.0) + tilesPerSec * dt;

        // Each time progress passes 1.0 the vehicle reaches the next tile
        while (progress >= 1.0) {
            progress -= 1.0;
            vehicle.advanceRoutePathIndex();                          // move to next tile
            vehicle.setPosition(path.get(vehicle.getRoutePathIndex()));
        }

        tileProgress.put(vid, progress);

        // ── Smooth sub-tile interpolation ─────────────────────────────────────
        // from = tile the vehicle is currently ON  (path[curIdx])
        // to   = the NEXT tile it is heading toward (path[nextIdx])
        int      curIdx  = vehicle.getRoutePathIndex();
        int      nextIdx = (curIdx + 1) % pathSize;
        Position from    = path.get(curIdx);
        Position to      = path.get(nextIdx);

        vehicle.setSmoothPosition(
                lerp(from.getX(), to.getX(), progress),
                lerp(from.getY(), to.getY(), progress)
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
