package controller;

import model.Game;
import model.Position;
import model.Road;
import model.Stop;
import model.enums.VehicleType;

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
        ROAD_HORIZONTAL,
        ROAD_VERTICAL,
        STOP,
        BRIDGE,
        DEMOLISH
    }

    private final Game game;
    private BuildMode  buildMode = BuildMode.SELECT;

    // Simple counters — will be replaced by ConstructionService (Issue 6)
    private int roadIdCounter = 0;
    private int stopIdCounter = 0;

    // Registered by the View so the controller can request a redraw
    private Runnable onStateChanged;

    public GameController(Game game) {
        this.game = game;
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
            case ROAD_HORIZONTAL -> onBuildRoad(p, Road.RoadType.HORIZONTAL);
            case ROAD_VERTICAL   -> onBuildRoad(p, Road.RoadType.VERTICAL);
            case STOP            -> onBuildStop(p);
            case DEMOLISH        -> onDemolish(p);
            case SELECT          -> onSelect(p);
            default              -> { /* bridge — handled separately */ }
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

    /** Places a road of the given type at position p. */
    public void onBuildRoad(Position p, Road.RoadType type) {
        Road road = new Road("road-" + (++roadIdCounter), p.getX(), p.getY(), type);
        if (game.addRoad(road)) {
            System.out.println("[GameController] Road placed at " + p);
            notifyView();
        }
    }

    /** Places a stop at position p. */
    public void onBuildStop(Position p) {
        Stop stop = new Stop("stop-" + (++stopIdCounter), p.getX(), p.getY(),
                "Stop " + stopIdCounter);
        if (game.addStop(stop)) {
            System.out.println("[GameController] Stop placed at " + p);
            notifyView();
        }
    }

    /** Removes a road or stop at position p. */
    public void onDemolish(Position p) {
        if (game.removeRoad(p) || game.removeStop(p)) {
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

    public void onCreateRoute(List<Stop> stops) {
        // TODO: delegate to RouteService
        System.out.println("[GameController] onCreateRoute() — not yet implemented");
    }

    public void onModifyRoute(model.Route route, List<Stop> newOrder) {
        // TODO: delegate to RouteService
        System.out.println("[GameController] onModifyRoute() — not yet implemented");
    }

    public void onBuyVehicle(VehicleType type) {
        // TODO: delegate to VehicleService
        System.out.println("[GameController] onBuyVehicle(" + type + ") — not yet implemented");
    }

    public void onAssignVehicle(model.Vehicle vehicle, model.Route route) {
        // TODO: delegate to VehicleService
        System.out.println("[GameController] onAssignVehicle() — not yet implemented");
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

    public Game getGame() {
        return game;
    }
}
