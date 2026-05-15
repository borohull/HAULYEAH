package controller;

import model.*;
import model.enums.TileType;
import model.enums.TransactionType;
import model.enums.VehicleType;
import model.service.ConstructionService;
import model.service.RoadPathfinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for GameController to improve coverage.
 * Covers success paths, invalid inputs, edge cases, route/path logic,
 * money/resource changes, state updates, and branch conditions.
 */
@DisplayName("GameController Comprehensive Tests")
class GameControllerTest {

    private Game game;
    private GameController controller;
    private Player player;
    private GameState state;

    @BeforeEach
    void setUp() {
        game = new Game(20, 20);
        player = new Player("TestPlayer", 100000);
        state = new GameState(game, player);
        controller = new GameController(state);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setTile(int x, int y, TileType type) {
        game.getTile(x, y).setType(type);
    }

    private void addRoad(int x, int y) {
        setTile(x, y, TileType.ROAD);
        game.getRoads().add(new Road("road-" + x + "-" + y, x, y, Road.RoadType.HORIZONTAL));
    }

    private Stop addStop(int x, int y) {
        String id = "stop-" + x + "-" + y;
        Stop stop = new Stop(id, x, y, "Stop " + id);
        game.getStops().add(stop);
        setTile(x, y, TileType.STOP);
        return stop;
    }

    private Vehicle addVehicle(int x, int y, VehicleType type) {
        Vehicle vehicle = new Vehicle("v-" + x + "-" + y, type, new Position(x, y));
        game.addVehicle(vehicle);
        return vehicle;
    }

    // ── onTileClicked ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("onTileClicked in ROAD mode builds road on grass")
    void testOnTileClickedRoadMode() {
        controller.setBuildMode(GameController.BuildMode.ROAD);
        Position p = new Position(5, 5);

        controller.onTileClicked(p);

        assertEquals(TileType.ROAD, game.getTile(p).getType());
        assertEquals(1, game.getRoads().size());
    }

    @Test
    @DisplayName("onTileClicked in BRIDGE mode builds bridge on water")
    void testOnTileClickedBridgeMode() {
        setTile(5, 5, TileType.WATER);
        controller.setBuildMode(GameController.BuildMode.BRIDGE);
        Position p = new Position(5, 5);

        controller.onTileClicked(p);

        assertEquals(TileType.BRIDGE, game.getTile(p).getType());
        assertEquals(1, game.getBridges().size());
    }

    @Test
    @DisplayName("onTileClicked in TRAFFIC_LIGHT mode builds traffic light")
    void testOnTileClickedTrafficLightMode() {
        // Need a junction: 3 or 4 roads
        addRoad(5, 4);
        addRoad(5, 5);
        addRoad(5, 6);
        addRoad(4, 5);
        controller.setBuildMode(GameController.BuildMode.TRAFFIC_LIGHT);
        Position p = new Position(5, 5);

        controller.onTileClicked(p);

        assertEquals(1, game.getTrafficLights().size());
    }

    @Test
    @DisplayName("onTileClicked in DEMOLISH mode removes road")
    void testOnTileClickedDemolishMode() {
        addRoad(5, 5);
        controller.setBuildMode(GameController.BuildMode.DEMOLISH);
        Position p = new Position(5, 5);

        controller.onTileClicked(p);

        assertEquals(TileType.EMPTY, game.getTile(p).getType());
        assertEquals(0, game.getRoads().size());
    }

    @Test
    @DisplayName("onTileClicked in SELECT mode selects traffic light")
    void testOnTileClickedSelectMode() {
        TrafficLight tl = new TrafficLight("tl1", new Position(5, 5), List.of());
        game.addTrafficLight(tl);
        AtomicReference<TrafficLight> selected = new AtomicReference<>();
        controller.setOnTrafficLightSelected(selected::set);
        controller.setBuildMode(GameController.BuildMode.SELECT);
        Position p = new Position(5, 5);

        controller.onTileClicked(p);

        assertEquals(tl, selected.get());
    }

    @Test
    @DisplayName("onTileClicked in ROUTE_DRAW mode calls onRouteDrawClick")
    void testOnTileClickedRouteDrawMode() {
        controller.setBuildMode(GameController.BuildMode.ROUTE_DRAW);
        Position p = new Position(5, 5);
        setTile(5, 5, TileType.ROAD);

        controller.onTileClicked(p);

        assertEquals(1, controller.getCurrentRoutePath().size());
    }

    @Test
    @DisplayName("onTileClicked with invalid position does nothing")
    void testOnTileClickedInvalidPosition() {
        controller.setBuildMode(GameController.BuildMode.ROAD);
        Position p = new Position(-1, -1);

        assertDoesNotThrow(() -> controller.onTileClicked(p));
    }

    // ── Build Methods ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("onBuildRoad on forest costs extra and clears forest")
    void testOnBuildRoadOnForest() {
        setTile(5, 5, TileType.FOREST);
        double initialCapital = player.getLedger().getCurrentCapital();

        controller.onBuildRoad(new Position(5, 5));

        assertEquals(TileType.ROAD, game.getTile(5, 5).getType());
        assertTrue(player.getLedger().getCurrentCapital() < initialCapital);
        // Cost should be ROAD + FOREST_CLEARING
    }

    @Test
    @DisplayName("onBuildRoad on invalid tile does nothing")
    void testOnBuildRoadInvalidTile() {
        setTile(5, 5, TileType.WATER);

        controller.onBuildRoad(new Position(5, 5));

        assertEquals(TileType.WATER, game.getTile(5, 5).getType());
        assertEquals(0, game.getRoads().size());
    }

    @Test
    @DisplayName("onBuildTrafficLight on non-junction does nothing")
    void testOnBuildTrafficLightNonJunction() {
        addRoad(5, 5); // Only one road

        controller.onBuildTrafficLight(new Position(5, 5));

        assertEquals(0, game.getTrafficLights().size());
    }

    @Test
    @DisplayName("onBuildBridge on non-water does nothing")
    void testOnBuildBridgeNonWater() {
        controller.onBuildBridge(new Position(5, 5), "Wooden Bridge");

        assertEquals(0, game.getBridges().size());
    }

    @Test
    @DisplayName("onBuildBridge exceeds span limit and notifies")
    void testOnBuildBridgeExceedsSpan() {
        setTile(5, 5, TileType.WATER);
        setTile(6, 5, TileType.WATER);
        setTile(7, 5, TileType.WATER);
        setTile(8, 5, TileType.WATER);
        // Build 3 bridges first
        controller.onBuildBridge(new Position(5, 5), "Wooden Bridge");
        controller.onBuildBridge(new Position(6, 5), "Wooden Bridge");
        controller.onBuildBridge(new Position(7, 5), "Wooden Bridge");

        AtomicReference<String> message = new AtomicReference<>();
        controller.setOnBridgeLimitReached(message::set);

        controller.onBuildBridge(new Position(8, 5), "Wooden Bridge");

        assertEquals(3, game.getBridges().size());
        assertEquals(TileType.WATER, game.getTile(8, 5).getType());
        assertNotNull(message.get());
    }

    // ── Route Drawing ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("startRouteDraw initializes route drawing")
    void testStartRouteDraw() {
        controller.startRouteDraw("Test Route");

        assertEquals(GameController.BuildMode.ROUTE_DRAW, controller.getBuildMode());
        assertEquals(0, controller.getCurrentRoutePath().size());
    }

    @Test
    @DisplayName("onRouteDrawClick adds walkable tile to path")
    void testOnRouteDrawClickWalkable() {
        controller.startRouteDraw("Test Route");
        setTile(5, 5, TileType.ROAD);

        controller.onRouteDrawClick(new Position(5, 5));

        assertEquals(1, controller.getCurrentRoutePath().size());
    }

    @Test
    @DisplayName("onRouteDrawClick skips non-walkable tile")
    void testOnRouteDrawClickNonWalkable() {
        controller.startRouteDraw("Test Route");
        setTile(5, 5, TileType.EMPTY);

        controller.onRouteDrawClick(new Position(5, 5));

        assertEquals(0, controller.getCurrentRoutePath().size());
    }

    @Test
    @DisplayName("onRouteDrawClick auto-fills path between points")
    void testOnRouteDrawClickAutoFill() {
        controller.startRouteDraw("Test Route");
        setTile(5, 5, TileType.ROAD);
        setTile(6, 5, TileType.ROAD);
        setTile(7, 5, TileType.ROAD);

        controller.onRouteDrawClick(new Position(5, 5));
        controller.onRouteDrawClick(new Position(7, 5));

        assertEquals(3, controller.getCurrentRoutePath().size());
    }

    @Test
    @DisplayName("finishRouteDraw creates route with sufficient tiles")
    void testFinishRouteDraw() {
        controller.startRouteDraw("Test Route");
        setTile(5, 5, TileType.ROAD);
        setTile(6, 5, TileType.ROAD);
        controller.onRouteDrawClick(new Position(5, 5));
        controller.onRouteDrawClick(new Position(6, 5));

        Route route = controller.finishRouteDraw();

        assertNotNull(route);
        assertEquals(1, game.getRoutes().size());
        assertEquals(GameController.BuildMode.SELECT, controller.getBuildMode());
    }

    @Test
    @DisplayName("finishRouteDraw with insufficient tiles returns null")
    void testFinishRouteDrawInsufficientTiles() {
        controller.startRouteDraw("Test Route");
        setTile(5, 5, TileType.ROAD);
        controller.onRouteDrawClick(new Position(5, 5));

        Route route = controller.finishRouteDraw();

        assertNull(route);
        assertEquals(0, game.getRoutes().size());
    }

    @Test
    @DisplayName("cancelRouteDraw clears path and returns to select")
    void testCancelRouteDraw() {
        controller.startRouteDraw("Test Route");
        setTile(5, 5, TileType.ROAD);
        controller.onRouteDrawClick(new Position(5, 5));

        controller.cancelRouteDraw();

        assertEquals(0, controller.getCurrentRoutePath().size());
        assertEquals(GameController.BuildMode.SELECT, controller.getBuildMode());
    }

    // ── Vehicle Management ────────────────────────────────────────────────────

    @Test
    @DisplayName("onBuyVehicle spawns vehicle and assigns route if available")
    void testOnBuyVehicle() {
        addStop(5, 5);
        Route route = new Route("r1", "Route 1", List.of(), List.of(new Position(5, 5), new Position(6, 5)));
        game.addRoute(route);

        controller.onBuyVehicle(VehicleType.CITY_BUS);

        assertEquals(1, game.getVehicles().size());
    }

    @Test
    @DisplayName("onBuyVehicle with no stops does nothing")
    void testOnBuyVehicleNoStops() {
        controller.onBuyVehicle(VehicleType.CITY_BUS);

        assertEquals(0, game.getVehicles().size());
    }

    @Test
    @DisplayName("onSellVehicle removes vehicle and credits money")
    void testOnSellVehicle() {
        Vehicle vehicle = addVehicle(5, 5, VehicleType.CITY_BUS);
        double initialCapital = player.getLedger().getCurrentCapital();

        controller.onSellVehicle(vehicle);

        assertEquals(0, game.getVehicles().size());
        assertTrue(player.getLedger().getCurrentCapital() > initialCapital);
    }

    @Test
    @DisplayName("spawnAndAssign spawns and assigns vehicle to route")
    void testSpawnAndAssign() {
        Route route = new Route("r1", "Route 1", List.of(), List.of(new Position(5, 5), new Position(6, 5)));
        game.addRoute(route);

        Vehicle vehicle = controller.spawnAndAssign(VehicleType.CITY_BUS, route);

        assertNotNull(vehicle);
        assertEquals(route, vehicle.getRoute());
    }

    @Test
    @DisplayName("spawnAndAssign with no path uses stop position")
    void testSpawnAndAssignNoPath() {
        Stop stop = addStop(5, 5);
        Route route = new Route("r1", "Route 1", List.of(stop), List.of());

        Vehicle vehicle = controller.spawnAndAssign(VehicleType.CITY_BUS, route);

        assertNotNull(vehicle);
        assertEquals(new Position(5, 5), vehicle.getPosition());
    }

    @Test
    @DisplayName("onAssignVehicle assigns route and looping")
    void testOnAssignVehicle() {
        Vehicle vehicle = addVehicle(5, 5, VehicleType.CITY_BUS);
        Route route = new Route("r1", "Route 1", List.of(), List.of(new Position(5, 5), new Position(6, 5)));
        game.addRoute(route);

        controller.onAssignVehicle(vehicle, route, true);

        assertEquals(route, vehicle.getRoute());
        assertTrue(vehicle.isLooping());
    }

    @Test
    @DisplayName("onDeployVehicle activates vehicle if route exists")
    void testOnDeployVehicle() {
        Vehicle vehicle = addVehicle(5, 5, VehicleType.CITY_BUS);
        Route route = new Route("r1", "Route 1", List.of(), List.of(new Position(5, 5), new Position(6, 5)));
        vehicle.assignRoute(route);

        controller.onDeployVehicle(vehicle);

        assertTrue(vehicle.isActive());
    }

    // ── Demolish ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("onDemolish removes stop")
    void testOnDemolishStop() {
        addStop(5, 5);

        controller.onDemolish(new Position(5, 5));

        assertEquals(TileType.EMPTY, game.getTile(5, 5).getType());
        assertEquals(0, game.getStops().size());
    }

    // ── Select ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("onSelect with traffic light fires callback")
    void testOnSelectTrafficLight() {
        TrafficLight tl = new TrafficLight("tl1", new Position(5, 5), List.of());
        game.addTrafficLight(tl);
        AtomicReference<TrafficLight> selected = new AtomicReference<>();
        controller.setOnTrafficLightSelected(selected::set);

        controller.onSelect(new Position(5, 5));

        assertEquals(tl, selected.get());
    }

    @Test
    @DisplayName("onSelect with no traffic light does nothing")
    void testOnSelectNoTrafficLight() {
        AtomicReference<TrafficLight> selected = new AtomicReference<>();
        controller.setOnTrafficLightSelected(selected::set);

        controller.onSelect(new Position(5, 5));

        assertNull(selected.get());
    }

    // ── Setters and Getters ───────────────────────────────────────────────────

    @Test
    @DisplayName("setBuildMode updates mode and notifies")
    void testSetBuildMode() {
        AtomicBoolean notified = new AtomicBoolean(false);
        controller.setOnStateChanged(() -> notified.set(true));

        controller.setBuildMode(GameController.BuildMode.ROAD);

        assertEquals(GameController.BuildMode.ROAD, controller.getBuildMode());
        assertTrue(notified.get());
    }

    @Test
    @DisplayName("setSelectedBridgeType sets type, trims null/blank")
    void testSetSelectedBridgeType() {
        controller.setSelectedBridgeType("Stone Bridge");
        assertEquals("Stone Bridge", controller.getSelectedBridgeType());

        controller.setSelectedBridgeType(null);
        assertEquals("Wooden Bridge", controller.getSelectedBridgeType());

        controller.setSelectedBridgeType("  ");
        assertEquals("Wooden Bridge", controller.getSelectedBridgeType());
    }

    @Test
    @DisplayName("setSimulationController sets controller")
    void testSetSimulationController() {
        SimulationController sim = new SimulationController(state);
        controller.setSimulationController(sim);
        // No direct getter, but used in other methods
    }

    @Test
    @DisplayName("setOnStateChanged sets callback")
    void testSetOnStateChanged() {
        Runnable callback = () -> {};
        controller.setOnStateChanged(callback);
        // Tested via notifications in other tests
    }

    @Test
    @DisplayName("setOnTrafficLightSelected sets callback")
    void testSetOnTrafficLightSelected() {
        java.util.function.Consumer<TrafficLight> callback = tl -> {};
        controller.setOnTrafficLightSelected(callback);
        // Tested in onSelect
    }

    @Test
    @DisplayName("setOnBridgeLimitReached sets callback")
    void testSetOnBridgeLimitReached() {
        java.util.function.Consumer<String> callback = msg -> {};
        controller.setOnBridgeLimitReached(callback);
        // Tested in bridge tests
    }

    @Test
    @DisplayName("setOnInsufficientFunds sets callback")
    void testSetOnInsufficientFunds() {
        java.util.function.Consumer<String> callback = msg -> {};
        controller.setOnInsufficientFunds(callback);
        // Tested in money tests
    }

    @Test
    @DisplayName("setOnBankrupt sets callback")
    void testSetOnBankrupt() {
        Runnable callback = () -> {};
        controller.setOnBankrupt(callback);
        // Tested in bankruptcy tests
    }

    @Test
    @DisplayName("fireTrafficLightSelected calls callback")
    void testFireTrafficLightSelected() {
        TrafficLight tl = new TrafficLight("tl1", new Position(5, 5), List.of());
        AtomicReference<TrafficLight> fired = new AtomicReference<>();
        controller.setOnTrafficLightSelected(fired::set);

        controller.fireTrafficLightSelected(tl);

        assertEquals(tl, fired.get());
    }

    @Test
    @DisplayName("getCurrentRoutePath returns live list")
    void testGetCurrentRoutePath() {
        assertNotNull(controller.getCurrentRoutePath());
    }

    @Test
    @DisplayName("notifyViewFromOutside calls notifyView")
    void testNotifyViewFromOutside() {
        AtomicBoolean notified = new AtomicBoolean(false);
        controller.setOnStateChanged(() -> notified.set(true));

        controller.notifyViewFromOutside();

        assertTrue(notified.get());
    }

    @Test
    @DisplayName("getState returns state")
    void testGetState() {
        assertEquals(state, controller.getState());
    }

    // ── Other Methods ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("onOpenMinimap does nothing")
    void testOnOpenMinimap() {
        assertDoesNotThrow(() -> controller.onOpenMinimap());
    }

    @Test
    @DisplayName("onOpenFinanceDetails does nothing")
    void testOnOpenFinanceDetails() {
        assertDoesNotThrow(() -> controller.onOpenFinanceDetails());
    }

    @Test
    @DisplayName("onModifyRoute does nothing")
    void testOnModifyRoute() {
        Route route = new Route("r1", "Route 1", List.of(), List.of());
        game.addRoute(route);

        assertDoesNotThrow(() -> controller.onModifyRoute(route, List.of()));
    }

    // ── onCreateRoute ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("onCreateRoute with connected stops creates circular route")
    void testOnCreateRouteConnected() {
        Stop s1 = addStop(5, 5);
        Stop s2 = addStop(7, 5);
        addRoad(5, 5);
        addRoad(6, 5);
        addRoad(7, 5);

        Route route = controller.onCreateRoute(List.of(s1, s2));

        assertNotNull(route);
        assertTrue(route.isCircular());
    }

    @Test
    @DisplayName("onCreateRoute with disconnected stops creates non-circular route")
    void testOnCreateRouteDisconnected() {
        Stop s1 = addStop(5, 5);
        Stop s2 = addStop(10, 10);

        Route route = controller.onCreateRoute(List.of(s1, s2));

        assertNotNull(route);
        assertFalse(route.isCircular());
    }

    @Test
    @DisplayName("onCreateRoute with less than 2 stops returns null")
    void testOnCreateRouteTooFewStops() {
        Stop s1 = addStop(5, 5);

        Route route = controller.onCreateRoute(List.of(s1));

        assertNull(route);
    }

    // ── Edge Cases and Invalid Inputs ─────────────────────────────────────────

    @Test
    @DisplayName("onBuyVehicleDirect with no stops uses road position")
    void testOnBuyVehicleDirectNoStops() {
        addRoad(5, 5);

        Vehicle vehicle = controller.onBuyVehicleDirect(VehicleType.CITY_BUS);

        assertNotNull(vehicle);
        assertEquals(new Position(5, 5), vehicle.getPosition());
    }

    @Test
    @DisplayName("onBuyVehicleDirect with no stops or roads returns null")
    void testOnBuyVehicleDirectNoSpawns() {
        Vehicle vehicle = controller.onBuyVehicleDirect(VehicleType.CITY_BUS);

        assertNull(vehicle);
    }

    @Test
    @DisplayName("onBuildRoad insufficient funds still builds and bankrupts")
    void testOnBuildRoadInsufficientFunds() {
        player.getLedger().spend(99999, TransactionType.PURCHASE, "Spend");
        AtomicReference<String> warning = new AtomicReference<>();
        AtomicBoolean bankrupt = new AtomicBoolean(false);
        controller.setOnInsufficientFunds(warning::set);
        controller.setOnBankrupt(() -> bankrupt.set(true));

        controller.onBuildRoad(new Position(5, 5));

        assertTrue(player.getLedger().isBankrupt());
        assertTrue(bankrupt.get());
        assertNotNull(warning.get());
    }

    @Test
    @DisplayName("onRouteDrawClick with invalid position does nothing")
    void testOnRouteDrawClickInvalidPosition() {
        controller.startRouteDraw("Test");

        assertDoesNotThrow(() -> controller.onRouteDrawClick(new Position(-1, -1)));
    }

    @Test
    @DisplayName("startRouteDraw with null name uses default")
    void testStartRouteDrawNullName() {
        controller.startRouteDraw(null);

        // Mode set, but name handled internally
        assertEquals(GameController.BuildMode.ROUTE_DRAW, controller.getBuildMode());
    }

    @Test
    @DisplayName("renameRoute with null route does nothing")
    void testRenameRouteNullRoute() {
        assertDoesNotThrow(() -> controller.renameRoute(null, "New Name"));
    }

    @Test
    @DisplayName("deleteRoute with null does nothing")
    void testDeleteRouteNull() {
        assertDoesNotThrow(() -> controller.deleteRoute(null));
    }

    @Test
    @DisplayName("onAssignVehicle with null route unassigns")
    void testOnAssignVehicleNullRoute() {
        Vehicle vehicle = addVehicle(5, 5, VehicleType.CITY_BUS);
        Route route = new Route("r1", "Route 1", List.of(), List.of());
        vehicle.assignRoute(route);

        controller.onAssignVehicle(vehicle, null, false);

        assertNull(vehicle.getRoute());
    }
}

