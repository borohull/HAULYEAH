package controller;

import model.GameState;
import model.Position;
import model.Road;
import model.Route;
import model.Stop;
import model.Tile;
import model.TrafficLight;
import model.Vehicle;
import model.Bridge;
import model.enums.TileType;
import model.enums.TransactionType;
import model.enums.VehicleType;
import model.service.ConstructionService;
import model.service.RoadPathfinder;
import model.service.VehicleService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GameController
 *
 * Handles all in-game user interactions:
 * - Mouse clicks on the map (tile selection, building, demolishing)
 * - Route drawing: player clicks road/stop tiles to define the exact path
 * - Delegating build/remove actions to model services
 */
public class GameController {

    public enum BuildMode {
        SELECT,
        ROAD,
        STOP,
        BRIDGE,
        TRAFFIC_LIGHT,
        DEMOLISH,
        ROUTE_DRAW
    }

    private static final double WOODEN_BRIDGE_COST = 1000.0;
    private static final double STONE_BRIDGE_COST = 2000.0;
    private static final double STEEL_BRIDGE_COST = 3000.0;
    private static final String DEFAULT_BRIDGE_TYPE = "Wooden Bridge";

    private final GameState state;
    private BuildMode buildMode = BuildMode.SELECT;
    private String selectedBridgeType = DEFAULT_BRIDGE_TYPE;

    private final ConstructionService constructionService = new ConstructionService();
    private final VehicleService vehicleService = new VehicleService();
    private int routeCounter = 0;

    // ── Route drawing state ───────────────────────────────────────────────────
    /** Tiles the player has clicked so far while drawing a route. */
    private final List<Position> currentRoutePath = new ArrayList<>();
    /** Stops detected along currentRoutePath. */
    private final List<Stop> currentRouteStops = new ArrayList<>();
    /** Name to give the route when finished. */
    private String pendingRouteName = "";

    private Runnable onStateChanged;
    private SimulationController simController;
    private java.util.function.Consumer<TrafficLight> onTrafficLightSelected;
    private Consumer<String> onBridgeLimitReached;
    private Consumer<String> onInsufficientFunds;
    private Runnable onBankrupt;

    public GameController(GameState state) {
        this.state = state;
    }

    public void setSimulationController(SimulationController sc) {
        this.simController = sc;
    }

    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    public void setOnTrafficLightSelected(java.util.function.Consumer<TrafficLight> callback) {
        this.onTrafficLightSelected = callback;
    }

    public void setOnBridgeLimitReached(Consumer<String> callback) {
        this.onBridgeLimitReached = callback;
    }

    public void setOnInsufficientFunds(Consumer<String> callback) {
        this.onInsufficientFunds = callback;
    }

    public void setOnBankrupt(Runnable callback) {
        this.onBankrupt = callback;
    }

    /**
     * Directly fires the traffic-light-selected callback (used by GameWindow for
     * quick-cycle clicks).
     */
    public void fireTrafficLightSelected(TrafficLight tl) {
        if (onTrafficLightSelected != null)
            onTrafficLightSelected.accept(tl);
    }

    public Vehicle onBuyVehicleDirect(VehicleType type) {
        Position spawnPos = null;

        List<Stop> stops = state.getMap().getStops();
        if (!stops.isEmpty()) {
            spawnPos = stops.get(0).getPosition();
        } else {
            List<Road> roads = state.getMap().getRoads();
            if (!roads.isEmpty()) {
                spawnPos = roads.get(0).getPosition();
            }
        }

        if (spawnPos == null) {
            System.out.println("[GameController] Cannot buy vehicle - no stop or road exists to place it");
            return null;
        }

        Vehicle v = vehicleService.spawnAtPosition(state.getMap(), type, spawnPos);
        if (v != null) {
            if (!state.getPlayer().getLedger().canAfford(type.getPurchasePrice())) {
                notifyInsufficientFunds("Not enough money to buy " + type.name().replace('_', ' ')
                        + ". The purchase will still go through and may bankrupt you.");
            }
            state.getPlayer().getLedger().spend(type.getPurchasePrice(), TransactionType.PURCHASE,
                    "Buy " + type.name());
            handleBankruptcyIfNeeded();
        }
        markUnsaved();
        notifyView();
        return v;
    }

    // ── Main tile-click dispatcher ────────────────────────────────────────────

    public void onTileClicked(Position p) {
        switch (buildMode) {
            case ROAD -> onBuildRoad(p);
            case STOP -> onBuildStop(p);
            case BRIDGE -> onBuildBridge(p, selectedBridgeType);
            case TRAFFIC_LIGHT -> onBuildTrafficLight(p);
            case DEMOLISH -> onDemolish(p);
            case SELECT -> onSelect(p);
            case ROUTE_DRAW -> onRouteDrawClick(p);
            default -> {
            }
        }
    }

    // ── Build mode ────────────────────────────────────────────────────────────

    public void setBuildMode(BuildMode mode) {
        this.buildMode = mode;
        System.out.println("[GameController] buildMode -> " + mode);
        notifyView();
    }

    public BuildMode getBuildMode() {
        return buildMode;
    }

    public void setSelectedBridgeType(String bridgeType) {
        if (bridgeType == null || bridgeType.isBlank()) {
            selectedBridgeType = DEFAULT_BRIDGE_TYPE;
            return;
        }
        selectedBridgeType = bridgeType;
    }

    public String getSelectedBridgeType() {
        return selectedBridgeType;
    }

    // ── Construction ──────────────────────────────────────────────────────────

    private static final double ROAD_COST = 500.0;
    private static final double FOREST_CLEARING_COST = 700.0;

    public void onBuildRoad(Position p) {
        Tile tile = state.getMap().getTile(p);
        if (tile == null)
            return;

        double cost = ROAD_COST;
        String note = "Road";

        if (tile.getType() == TileType.FOREST) {
            cost += FOREST_CLEARING_COST;
            note = "Road + clearing";
        }

        if (constructionService.buildRoad(state.getMap(), p, Road.RoadType.HORIZONTAL)) {
            if (!state.getPlayer().getLedger().canAfford(cost)) {
                notifyInsufficientFunds(
                        "Not enough money to build a road. The build will still go through and may bankrupt you.");
            }
            state.getPlayer().getLedger().spend(cost, TransactionType.BUILD, note);
            handleBankruptcyIfNeeded();
            markUnsaved();
            notifyView();
        }
    }

    public void onBuildStop(Position p) {
        String stopName = "Stop-" + (state.getMap().getStops().size() + 1);
        if (constructionService.buildStop(state.getMap(), p, stopName)) {
            markUnsaved();
            notifyView();
        }
    }

    public void onBuildTrafficLight(Position p) {
        if (constructionService.buildTrafficLight(state.getMap(), p)) {
            markUnsaved();
            notifyView();
        } else {
            System.out.println("[GameController] Traffic light requires a 3- or 4-way junction: " + p);
        }
    }

    public void onBuildBridge(Position p) {
        onBuildBridge(p, selectedBridgeType);
    }

    public void onBuildBridge(Position p, String bridgeType) {
        Tile tile = state.getMap().getTile(p);
        if (tile == null || tile.getType() != TileType.WATER) {
            System.out.println("[GameController] Bridge can only be built on water tiles: " + p);
            return;
        }

        double cost;
        Bridge.BridgeType modelBridgeType;
        switch (bridgeType) {
            case "Wooden Bridge":
                cost = WOODEN_BRIDGE_COST;
                modelBridgeType = Bridge.BridgeType.WOODEN;
                break;
            case "Stone Bridge":
                cost = STONE_BRIDGE_COST;
                modelBridgeType = Bridge.BridgeType.SUSPENSION;
                break;
            case "Steel Bridge":
                cost = STEEL_BRIDGE_COST;
                modelBridgeType = Bridge.BridgeType.STEEL;
                break;
            default:
                System.out.println("[GameController] Unknown bridge type: " + bridgeType);
                return;
        }

        int spanIfBuilt = getConsecutiveBridgeSpan(state.getMap(), p, modelBridgeType);
        if (spanIfBuilt > modelBridgeType.getMaxSpan()) {
            notifyBridgeLimitReached(bridgeType, modelBridgeType.getMaxSpan());
            return;
        }

        int numBridges = state.getMap().getBridges().size();
        Bridge bridge = new Bridge("bridge-" + (numBridges + 1), bridgeType + " " + (numBridges + 1),
                p.getX(), p.getY(), 1, Bridge.Orientation.HORIZONTAL, modelBridgeType);
        if (constructionService.buildBridge(state.getMap(), bridge)) {
            if (!state.getPlayer().getLedger().canAfford(cost)) {
                notifyInsufficientFunds("Not enough money to build a " + bridgeType
                        + ". The build will still go through and may bankrupt you.");
            }
            state.getPlayer().getLedger().spend(cost, TransactionType.BUILD, bridgeType);
            handleBankruptcyIfNeeded();
            markUnsaved();
            notifyView();
        }
    }

    private int getConsecutiveBridgeSpan(model.Game game, Position p, Bridge.BridgeType bridgeType) {
        return 1
                + countMatchingBridgeTiles(game, p, bridgeType, -1)
                + countMatchingBridgeTiles(game, p, bridgeType, 1);
    }

    private int countMatchingBridgeTiles(model.Game game, Position start, Bridge.BridgeType bridgeType, int stepX) {
        int count = 0;
        int x = start.getX() + stepX;
        int y = start.getY();

        while (true) {
            Tile t = game.getTile(x, y);
            if (t == null || t.getType() != TileType.BRIDGE) {
                break;
            }

            Bridge adjacent = game.getBridgeAt(new Position(x, y));
            if (adjacent == null || adjacent.getBridgeType() != bridgeType) {
                break;
            }

            count++;
            x += stepX;
        }

        return count;
    }

    private void notifyBridgeLimitReached(String bridgeType, int maxSpan) {
        String message = bridgeType + " can span at most " + maxSpan + " consecutive bridge tiles.";
        if (onBridgeLimitReached != null) {
            onBridgeLimitReached.accept(message);
        } else {
            System.out.println("[GameController] " + message);
        }
    }

    private void notifyInsufficientFunds(String message) {
        if (onInsufficientFunds != null) {
            onInsufficientFunds.accept(message);
        } else {
            System.out.println("[GameController] " + message);
        }
    }

    private void handleBankruptcyIfNeeded() {
        if (!state.getPlayer().getLedger().isBankrupt()) {
            return;
        }

        if (simController != null) {
            simController.pause();
        }

        if (onBankrupt != null) {
            onBankrupt.run();
        }
    }

    public void onDemolish(Position p) {
        if (constructionService.removeTrafficLight(state.getMap(), p)
                || constructionService.removeRoad(state.getMap(), p)
                || constructionService.removeStop(state.getMap(), p)
                || constructionService.removeBridge(state.getMap(), p)) {
            markUnsaved();
            notifyView();
        }
    }

    public void onSelect(Position p) {
        TrafficLight tl = state.getMap().getTrafficLightAt(p);
        if (tl != null) {
            if (onTrafficLightSelected != null) {
                onTrafficLightSelected.accept(tl);
            }
        }
    }

    // ── Route drawing ─────────────────────────────────────────────────────────

    /**
     * Enters route-draw mode.
     * 
     * @param routeName the name to give the finished route
     */
    public void startRouteDraw(String routeName) {
        currentRoutePath.clear();
        currentRouteStops.clear();
        pendingRouteName = (routeName == null || routeName.isBlank())
                ? "Route " + (routeCounter + 1)
                : routeName;
        setBuildMode(BuildMode.ROUTE_DRAW);
        System.out.println("[GameController] Route draw started: " + pendingRouteName);
    }

    /**
     * Called when the player clicks a tile while in ROUTE_DRAW mode.
     * Only walkable tiles (ROAD, STOP, CITY_ROAD, CITY_STOP, BRIDGE) are accepted.
     */
    public void onRouteDrawClick(Position p) {
        Tile tile = state.getMap().getTile(p.getX(), p.getY());
        if (tile == null)
            return;

        TileType type = tile.getType();
        if (!isWalkable(type)) {
            System.out.println("[GameController] Route draw: tile not walkable " + p);
            return;
        }

        // Auto-fill tiles to ensure continuous one-by-one drawing
        if (!currentRoutePath.isEmpty()) {
            Position lastP = currentRoutePath.get(currentRoutePath.size() - 1);
            if (lastP.equals(p))
                return;

            // Build the straight-line fill (X-first, then Y) and validate every tile
            int dx = Integer.signum(p.getX() - lastP.getX());
            int dy = Integer.signum(p.getY() - lastP.getY());

            List<Position> fillSegment = new ArrayList<>();
            Position curr = lastP;
            boolean allWalkable = true;
            while (!curr.equals(p)) {
                if (curr.getX() != p.getX()) {
                    curr = new Position(curr.getX() + dx, curr.getY());
                } else if (curr.getY() != p.getY()) {
                    curr = new Position(curr.getX(), curr.getY() + dy);
                }
                Tile fillTile = state.getMap().getTile(curr.getX(), curr.getY());
                if (fillTile == null || !isWalkable(fillTile.getType())) {
                    allWalkable = false;
                    System.out.println("[GameController] Route auto-fill blocked: non-walkable tile at " + curr
                            + " — click tiles one by one along the road.");
                    break;
                }
                fillSegment.add(curr);
            }

            if (allWalkable) {
                for (Position fp : fillSegment) {
                    if (!currentRoutePath.contains(fp))
                        currentRoutePath.add(fp);
                }
            }
            // If path is blocked, only the destination tile is skipped — user must trace
            // manually.
        } else {
            currentRoutePath.add(p);
        }

        // Auto-detect stops along the path
        Stop stop = state.getMap().getStopAt(p);
        if (stop != null && !currentRouteStops.contains(stop)) {
            currentRouteStops.add(stop);
        }

        System.out.println("[GameController] Route path: " + currentRoutePath.size() + " tiles");
        notifyView();
    }

    /**
     * Finishes route drawing, creates the Route, and returns to SELECT mode.
     * 
     * @return the created Route, or null if path is too short
     */
    public Route finishRouteDraw() {
        if (currentRoutePath.size() < 2) {
            System.out.println("[GameController] Route needs at least 2 tiles");
            cancelRouteDraw();
            return null;
        }

        String id = "route-" + (++routeCounter);
        String name = pendingRouteName.isBlank() ? "Route " + routeCounter : pendingRouteName;

        Route route = new Route(id, name,
                new ArrayList<>(currentRouteStops),
                new ArrayList<>(currentRoutePath));
        state.getMap().addRoute(route);

        System.out.println("[GameController] Route '" + name + "' created with "
                + currentRoutePath.size() + " tiles, "
                + currentRouteStops.size() + " stops");

        currentRoutePath.clear();
        currentRouteStops.clear();
        setBuildMode(BuildMode.SELECT);
        markUnsaved();
        return route;
    }

    /** Cancels route drawing without saving. */
    public void cancelRouteDraw() {
        currentRoutePath.clear();
        currentRouteStops.clear();
        setBuildMode(BuildMode.SELECT);
        notifyView();
    }

    // Add both methods to GameController.java
    // Place them in the "Route drawing" section, after finishRouteDraw()

    /**
     * Deletes a route from the world.
     *
     * Before removing the route, every vehicle currently assigned to it
     * is unassigned and parked so they don't keep trying to follow a
     * route that no longer exists.
     *
     * @param route the route to delete
     */
    public void deleteRoute(Route route) {
        if (route == null)
            return;

        // Unassign every vehicle that was following this route
        for (model.Vehicle vehicle : state.getMap().getVehicles()) {
            if (vehicle.getRoute() != null
                    && vehicle.getRoute().getId().equals(route.getId())) {
                vehicle.assignRoute(null);
                vehicle.setActive(false);
            }
        }

        state.getMap().removeRoute(route);
        markUnsaved();
        notifyView();
    }

    /**
     * Renames an existing route.
     * Blank or null names are ignored — the route keeps its current name.
     *
     * @param route   the route to rename
     * @param newName the desired new name
     */
    public void renameRoute(Route route, String newName) {
        if (route == null || newName == null || newName.isBlank())
            return;
        route.setName(newName.trim());
        markUnsaved();
        notifyView();
    }

    /**
     * Returns a live (uncopied) view of the path being drawn — for overlay
     * rendering.
     */
    public List<Position> getCurrentRoutePath() {
        return currentRoutePath;
    }

    // ── Backward-compat route creation (used by GaragePanel auto-route) ───────

    public Route onCreateRoute(List<Stop> stops) {
        return onCreateRoute(stops, null);
    }

    public Route onCreateRoute(List<Stop> stops, String customName) {
        if (stops == null || stops.size() < 2)
            return null;
        String id = "route-" + (++routeCounter);
        String name = (customName != null && !customName.isBlank())
                ? customName
                : "Route " + routeCounter;

        // Build circular tile path: stop[0]→stop[1]→...→stop[n-1]→back to stop[0]
        List<Position> tilePath = new ArrayList<>();
        boolean allConnected = true;

        // Forward segments between consecutive stops
        for (int i = 0; i < stops.size() - 1; i++) {
            List<Position> seg = RoadPathfinder.findPath(
                    state.getMap(), stops.get(i).getPosition(), stops.get(i + 1).getPosition());
            if (seg == null) {
                allConnected = false;
                break;
            }
            if (tilePath.isEmpty())
                tilePath.addAll(seg);
            else
                tilePath.addAll(seg.subList(1, seg.size()));
        }

        // Closing segment: last stop back to first stop (exclude both endpoints — first
        // is already
        // at index 0 of tilePath, last is already at the end)
        if (allConnected) {
            List<Position> closing = RoadPathfinder.findPath(
                    state.getMap(),
                    stops.get(stops.size() - 1).getPosition(),
                    stops.get(0).getPosition());
            if (closing == null || closing.size() < 2) {
                allConnected = false;
            } else {
                // closing[0] == last stop (already in path), closing[last] == first stop
                // (already at index 0)
                tilePath.addAll(closing.subList(1, closing.size() - 1));
            }
        }

        Route route = new Route(id, name, new ArrayList<>(stops), allConnected ? tilePath : new ArrayList<>());
        route.setCircular(allConnected);
        if (!allConnected)
            System.out.println("[GameController] Route '" + name + "': stops not fully connected by roads");
        state.getMap().addRoute(route);
        notifyView();
        return route;
    }

    // ── Vehicle management ────────────────────────────────────────────────────

    public void onBuyVehicle(VehicleType type) {
        List<Stop> stops = state.getMap().getStops();
        if (stops.isEmpty())
            return;
        Vehicle v = vehicleService.spawnVehicle(state.getMap(), type, stops.get(0));
        List<Route> routes = state.getMap().getRoutes();
        if (!routes.isEmpty()) {
            v.setLooping(false);
            vehicleService.assignRoute(state.getMap(), v, routes.get(0));
        }
        notifyView();
    }

    public void onSellVehicle(Vehicle vehicle) {
        int sellPrice = vehicle.getType().getSellPrice();
        state.getMap().getVehicles().remove(vehicle);
        state.getPlayer().getLedger().earn(
                sellPrice,
                TransactionType.INCOME,
                "Sold " + vehicle.getType().name().replace("_", " "));
        markUnsaved();
        notifyView();
    }

    public Vehicle spawnAndAssign(VehicleType type, Route route) {
        Position spawnPos = null;

        // Prefer the first stop on the route so vehicle starts at a stop facing the
        // right direction
        if (route != null && route.hasTilePath()) {
            if (route.hasStops()) {
                spawnPos = route.getStops().get(0).getPosition();
            } else {
                spawnPos = route.getTilePath().get(0);
            }
        } else {
            // Fallback: first stop on the map
            List<Stop> stops = state.getMap().getStops();
            if (!stops.isEmpty())
                spawnPos = stops.get(0).getPosition();
        }

        if (spawnPos == null) {
            System.out.println("[GameController] Cannot spawn vehicle — route has no path and no stops exist");
            return null;
        }

        Vehicle v = vehicleService.spawnAtPosition(state.getMap(), type, spawnPos);
        vehicleService.assignRoute(state.getMap(), v, route);
        markUnsaved();
        notifyView();
        return v;
    }

    public void onAssignVehicle(Vehicle vehicle, Route route, boolean looping) {
        vehicle.setLooping(looping);
        vehicleService.assignRoute(state.getMap(), vehicle, route);
        if (simController != null)
            simController.resetVehicle(vehicle.getId());
        notifyView();
    }

    public void onDeployVehicle(Vehicle vehicle) {
        if (vehicle.getRoute() == null)
            return;
        vehicle.assignRoute(vehicle.getRoute());
        if (simController != null)
            simController.resetVehicle(vehicle.getId());
        notifyView();
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    public void onOpenMinimap() {
    }

    public void onOpenFinanceDetails() {
    }

    public void onModifyRoute(Route route, List<Stop> newOrder) {
        System.out.println("[GameController] onModifyRoute() — not yet implemented");
    }

    private void markUnsaved() {
        if (simController != null)
            simController.markUnsavedChanges();
    }

    private static boolean isWalkable(TileType t) {
        return t == TileType.ROAD || t == TileType.STOP
                || t == TileType.CITY_ROAD || t == TileType.CITY_STOP
                || t == TileType.BRIDGE;
    }

    private void notifyView() {
        if (onStateChanged != null)
            onStateChanged.run();
    }

    public void notifyViewFromOutside() {
        notifyView();
    }

    public GameState getState() {
        return state;
    }
}
