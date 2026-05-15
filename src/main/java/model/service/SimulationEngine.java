package model.service;

import model.*;
import model.enums.Direction;
import model.enums.LightState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimulationEngine — advances the game simulation by dt seconds each frame.
 *
 * Movement logic (no BFS):
 * routePathIndex = the tile the vehicle is currently standing ON.
 * The vehicle always interpolates from path[routePathIndex]
 * toward path[routePathIndex + 1].
 * When progress reaches 1.0, routePathIndex advances (wraps around).
 */
public class SimulationEngine {

    private final VehicleService vehicleService = new VehicleService();
    private final DeliveryService deliveryService = new DeliveryService();
    private static final double FOREST_GROWTH_INTERVAL = 10.0;
    private static final double FOREST_SPREAD_CHANCE = 0.25;
    private double forestGrowthTimer = 0.0;

    // Speed values in VehicleType are 25–60 (game units).
    // Divide by SPEED_SCALE → tiles per second (40 → 4 t/s).
    private static final double SPEED_SCALE = 40.0;

    /** Per-vehicle progress from current tile toward next tile (0.0 – <1.0). */
    private final Map<String, Double> tileProgress = new HashMap<>();

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Advances the simulation by dt seconds.
     * 
     * @param state                  the game state
     * @param dt                     elapsed time in seconds (already scaled by game
     *                               speed multiplier)
     * @param currentSpeedMultiplier the current game speed multiplier (1, 2, 4) for
     *                               bridge limiting
     */
    public void tick(GameState state, double dt, int currentSpeedMultiplier) {
        // Advance all traffic light phase timers
        for (TrafficLight tl : state.getMap().getTrafficLights().values()) {
            tl.tick(dt);
        }

        deliveryService.tickDemand(state.getMap(), state, dt);
        tickForestGrowth(state, dt);

        // Maintenance: drain maintenanceCost per game-minute per vehicle (aggregated,
        // not logged individually)
        double maintenancePerSecond = 1.0 / 60.0;
        for (Vehicle vehicle : state.getMap().getVehicles()) {
            String routeName = (vehicle.getRoute() != null) ? vehicle.getRoute().getName() : "Unassigned";
            String key = vehicle.getType().getCategory() + " (" + vehicle.getType().name().replace("_", " ")
                    + ") | Route: " + routeName;
            state.getPlayer().getLedger().recordMaintenance(key,
                    vehicle.getMaintenanceCost() * maintenancePerSecond * dt);
        }

        for (Vehicle vehicle : state.getMap().getVehicles()) {
            if (vehicle.getRoute() != null
                    && vehicle.getRoute().hasTilePath()
                    && !tileProgress.containsKey(vehicle.getId())) {
                // Restore saved progress, or stagger if another vehicle is already
                // on this route (prevents two vehicles being permanently in sync).
                double savedProgress = vehicle.getRouteProgress();
                if (savedProgress == 0.0) {
                    boolean routeAlreadyRunning = tileProgress.keySet().stream()
                            .anyMatch(id -> state.getMap().getVehicles().stream()
                                    .anyMatch(v -> v.getId().equals(id)
                                            && v.getRoute() != null
                                            && v.getRoute().getId().equals(vehicle.getRoute().getId())));
                    if (routeAlreadyRunning) {
                        int pathSize = vehicle.getRoute().getTilePath().size();
                        savedProgress = (pathSize / 2.0) % pathSize;
                        int staggerIdx = (vehicle.getRoutePathIndex() + pathSize / 2) % pathSize;
                        vehicle.restoreRouteState(staggerIdx, vehicle.isMovingForward());
                    }
                }
                tileProgress.put(vehicle.getId(), savedProgress);
                // Process the starting stop on the first tick so the vehicle loads/unloads
                // immediately rather than driving past it empty.
                Stop startStop = DeliveryService.findStopAt(vehicle.getPosition(), state.getMap().getStops());
                if (startStop != null)
                    deliveryService.handleStopArrival(vehicle, startStop, state);
            }
            tickVehicle(vehicle, dt, state, currentSpeedMultiplier);
        }
    }

    /**
     * Overload for backward compatibility (assumes 1x speed if not specified).
     */
    public void tick(GameState state, double dt) {
        tick(state, dt, 1);
    }

    private void tickForestGrowth(GameState state, double dt) {
        forestGrowthTimer += dt;
        if (forestGrowthTimer < FOREST_GROWTH_INTERVAL)
            return;
        forestGrowthTimer = 0.0;

        Game game = state.getMap();
        List<Position> spreadTargets = new java.util.ArrayList<>();

        for (int y = 0; y < game.getHeight(); y++) {
            for (int x = 0; x < game.getWidth(); x++) {
                Tile tile = game.getTile(x, y);
                if (tile == null || tile.getType() != model.enums.TileType.FOREST)
                    continue;

                if (tile.getTreeCount() <= 0) {
                    tile.setTreeCount(1);
                    game.markStaticTileDirty(new Position(x, y));
                } else if (tile.getTreeCount() < 4) {
                    tile.addTree();
                    game.markStaticTileDirty(new Position(x, y));
                }

                int[][] deltas = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
                for (int[] d : deltas) {
                    int nx = x + d[0];
                    int ny = y + d[1];
                    Tile neighbor = game.getTile(nx, ny);
                    if (neighbor == null)
                        continue;

                    if (neighbor.getType() == model.enums.TileType.EMPTY && Math.random() < FOREST_SPREAD_CHANCE) {
                        spreadTargets.add(new Position(nx, ny));
                    }
                }
            }
        }

        for (Position p : spreadTargets) {
            Tile tile = game.getTile(p);
            if (tile != null && tile.getType() == model.enums.TileType.EMPTY) {
                tile.setType(model.enums.TileType.FOREST);
                tile.setTreeCount(1);
                tile.setEntityId(null);
                tile.setEntityName(null);
                game.markStaticTileDirty(p);
            }
        }
    }

    /**
     * Call this when a vehicle is assigned a new route so it starts cleanly at tile
     * 0.
     */
    public void resetVehicle(String vehicleId) {
        tileProgress.remove(vehicleId);
    }

    public VehicleService getVehicleService() {
        return vehicleService;
    }

    // ── Per-vehicle movement ──────────────────────────────────────────────────

    private void tickVehicle(Vehicle vehicle, double dt, GameState state, int currentSpeedMultiplier) {
        if (!vehicle.isActive())
            return;
        Route route = vehicle.getRoute();
        if (route == null || !route.hasTilePath())
            return;

        List<Position> path = route.getTilePath();
        int pathSize = path.size();
        if (pathSize < 2)
            return;

        String vid = vehicle.getId();
        double tilesPerSec = Math.max(0.5, vehicle.getSpeed() / SPEED_SCALE);

        // ── Apply bridge speed limit ───────────────────────────────────────────
        // If the vehicle is on a bridge, cap its speed to the bridge's max speed
        // multiplier
        Position currentPos = vehicle.getPosition();
        Bridge bridgeAtPos = state.getMap().getBridgeAt(currentPos);
        if (bridgeAtPos != null) {
            int bridgeMaxSpeedMultiplier = bridgeAtPos.getBridgeType().getMaxSpeedMultiplier();
            // Cap dt to effectively limit to the bridge's max speed multiplier
            // If bridge max is 1x and game speed is 4x, we use dt / 4
            dt = dt * bridgeMaxSpeedMultiplier / currentSpeedMultiplier;
        }

        double storedProgress = tileProgress.getOrDefault(vid, 0.0); // progress before this tick
        double progress = storedProgress + tilesPerSec * dt;
        final double STOP_LINE = 0.0; // vehicle waits here (clearly inside previous tile)

        boolean forward = vehicle.isMovingForward();
        int curIdx = vehicle.getRoutePathIndex();
        int nextIdx = route.isCircular()
                ? (curIdx + 1) % pathSize
                : (forward ? Math.min(curIdx + 1, pathSize - 1) : Math.max(curIdx - 1, 0));

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
            if (arrivedStop != null)
                deliveryService.handleStopArrival(vehicle, arrivedStop, state);
            curIdx = vehicle.getRoutePathIndex();
            forward = vehicle.isMovingForward();
            nextIdx = route.isCircular()
                    ? (curIdx + 1) % pathSize
                    : (forward ? Math.min(curIdx + 1, pathSize - 1) : Math.max(curIdx - 1, 0));
            if (!route.isCircular() && curIdx == nextIdx) {
                // Arrived at endpoint — park non-looping vehicles; flip looping ones.
                if (!vehicle.isLooping()) {
                    vehicle.setActive(false);
                    break;
                }
                vehicle.advanceRoutePathIndex();
                curIdx = vehicle.getRoutePathIndex();
                forward = vehicle.isMovingForward();
                nextIdx = forward ? Math.min(curIdx + 1, pathSize - 1)
                        : Math.max(curIdx - 1, 0);
                if (curIdx == nextIdx)
                    break; // safety: path too short to move
                continue;
            }
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
        vehicle.setRouteProgress(progress); // keep Vehicle in sync for save/load

        // ── Smooth sub-tile interpolation ─────────────────────────────────────
        curIdx = vehicle.getRoutePathIndex();
        forward = vehicle.isMovingForward();
        nextIdx = route.isCircular()
                ? (curIdx + 1) % pathSize
                : (forward ? Math.min(curIdx + 1, pathSize - 1) : Math.max(curIdx - 1, 0));
        Position from = path.get(curIdx);
        Position to = path.get(nextIdx);

        vehicle.setSmoothPosition(
                lerp(from.getX(), to.getX(), progress),
                lerp(from.getY(), to.getY(), progress));

        // Update travel direction from actual movement vector this tick.
        // When from == to (endpoint), keep the previous direction unchanged.
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        if (dx > 0)
            vehicle.setTravelDirection(Direction.EAST);
        else if (dx < 0)
            vehicle.setTravelDirection(Direction.WEST);
        else if (dy > 0)
            vehicle.setTravelDirection(Direction.SOUTH);
        else if (dy < 0)
            vehicle.setTravelDirection(Direction.NORTH);
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
        if (dx == 1 && dy == 0)
            return Direction.WEST; // vehicle came from west
        if (dx == -1 && dy == 0)
            return Direction.EAST; // vehicle came from east
        if (dx == 0 && dy == 1)
            return Direction.NORTH; // vehicle came from north
        if (dx == 0 && dy == -1)
            return Direction.SOUTH; // vehicle came from south
        return null;
    }
}
