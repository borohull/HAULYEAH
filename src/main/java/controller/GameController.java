package controller;

import model.GameState;
import model.Position;
import model.Road;
import model.Route;
import model.Stop;
import model.Vehicle;
import model.enums.VehicleType;
import model.service.ConstructionService;
import model.service.VehicleService;

import java.util.ArrayList;
import java.util.List;

/**
 * GameController
 *
 * Handles all in-game user interactions:
 *   - Mouse clicks on the map (tile selection, building, demolishing)
 *   - Switching build modes via the toolbar
 *   - Delegating build/remove actions to model services (ConstructionService, etc.)
 *
 * After any state change the controller fires an onStateChanged callback
 * so the View knows to redraw without the controller touching JavaFX directly.
 */
public class GameController {

    /**
     * What the player is currently doing on the map.
     * Matches the BuildMode enum in the UML diagram.
     */
    public enum BuildMode {
        SELECT,
        ROAD,
        STOP,
        BRIDGE,
        DEMOLISH
    }

    private final GameState state;
    private BuildMode  buildMode = BuildMode.SELECT;

    private final ConstructionService constructionService = new ConstructionService();
    private       VehicleService      vehicleService      = new VehicleService();
    private int routeCounter = 0;

    private Runnable onStateChanged;

    public GameController(GameState state) {
        this.state = state;
    }


    /**
     * The View registers this callback so the controller can trigger a redraw
     * without importing any JavaFX drawing classes.
     */
    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    // -----------------------------------------------------------------------
    // Main entry point — called by MapPanel's mouse click handler in the View
    // -----------------------------------------------------------------------

    /**
     * Called when the player clicks a tile.
     * Dispatches to the correct action based on the current BuildMode.
     *
     * @param p the grid position that was clicked
     */
    public void onTileClicked(Position p) {
        switch (buildMode) {
            case ROAD     -> onBuildRoad(p);
            case STOP     -> onBuildStop(p);
            case DEMOLISH -> onDemolish(p);
            case SELECT   -> onSelect(p);
            default       -> { /* bridge — handled separately */ }
        }
    }

    // -----------------------------------------------------------------------
    // Build mode
    // -----------------------------------------------------------------------

    /**
     * Switches the active build mode.
     * Called when the player clicks a toolbar button (Build, Remove, Select, etc.).
     */
    public void setBuildMode(BuildMode mode) {
        this.buildMode = mode;
        System.out.println("[GameController] buildMode -> " + mode);
        notifyView();
    }

    public BuildMode getBuildMode() {
        return buildMode;
    }

    // -----------------------------------------------------------------------
    // Construction actions  (will delegate to ConstructionService in Issue 6)
    // -----------------------------------------------------------------------

    /** Places a road at position p. */
    public void onBuildRoad(Position p) {
        if (constructionService.buildRoad(state.getMap(), p, Road.RoadType.HORIZONTAL)) {
            System.out.println("[GameController] Road placed at " + p);
            notifyView();
        }
    }

    /** Places a stop at position p. */
    public void onBuildStop(Position p) {
        // Find a stop counter logic or rely on service to count
        String stopName = "Stop at " + p.getX() + "," + p.getY();
        if (constructionService.buildStop(state.getMap(), p, stopName)) {
            System.out.println("[GameController] Stop placed at " + p);
            notifyView();
        }
    }

    /** Removes a road or stop at position p. */
    public void onDemolish(Position p) {
        if (constructionService.removeRoad(state.getMap(), p) || constructionService.removeStop(state.getMap(), p)) {
            System.out.println("[GameController] Demolished at " + p);
            notifyView();
        }
    }

    /** Selects a tile (e.g. to show info in InfoPanel). */
    public void onSelect(Position p) {
        // TODO: notify InfoPanel with tile data
        System.out.println("[GameController] Selected tile at " + p);
    }

    // -----------------------------------------------------------------------
    // Route / Vehicle stubs  (will be filled in later issues)
    // -----------------------------------------------------------------------

    public Route onCreateRoute(List<Stop> stops) {
        if (stops == null || stops.size() < 2) {
            System.out.println("[GameController] Route needs at least 2 stops");
            return null;
        }
        String id   = "route-" + (++routeCounter);
        String name = "Route " + routeCounter;
        Route route = new Route(id, name, new ArrayList<>(stops));
        state.getMap().addRoute(route);
        System.out.println("[GameController] Created " + name);
        notifyView();
        return route;
    }

    public void onModifyRoute(Route route, List<Stop> newOrder) {
        System.out.println("[GameController] onModifyRoute() — not yet implemented");
    }

    public void onBuyVehicle(VehicleType type) {
        List<Stop> stops = state.getMap().getStops();
        if (stops.isEmpty()) return;
        Vehicle v = vehicleService.spawnVehicle(state.getMap(), type, stops.get(0));
        List<Route> routes = state.getMap().getRoutes();
        if (!routes.isEmpty()) vehicleService.assignRoute(state.getMap(), v, routes.get(0));
        notifyView();
    }

    /** Spawns a vehicle and assigns it to the given route. Used by GaragePanel. */
    public Vehicle spawnAndAssign(VehicleType type, Route route) {
        List<Stop> stops = state.getMap().getStops();
        if (stops.isEmpty()) return null;
        Vehicle v = vehicleService.spawnVehicle(state.getMap(), type, stops.get(0));
        vehicleService.assignRoute(state.getMap(), v, route);
        notifyView();
        return v;
    }

    public void onAssignVehicle(Vehicle vehicle, Route route) {
        vehicleService.assignRoute(state.getMap(), vehicle, route);
        notifyView();
    }

    // -----------------------------------------------------------------------
    // UI panel stubs
    // -----------------------------------------------------------------------

    public void onOpenMinimap() {
        // TODO: open MinimapPanel
        System.out.println("[GameController] onOpenMinimap() — not yet implemented");
    }

    public void onOpenFinanceDetails() {
        // TODO: open FinancePanel
        System.out.println("[GameController] onOpenFinanceDetails() — not yet implemented");
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    private void notifyView() {
        if (onStateChanged != null) {
            onStateChanged.run();
        }
    }

    public GameState getState() {
        return state;
    }
}
