package model.service;

import model.*;
import model.enums.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationEngine — verifies vehicle movement, maintenance costs,
 * traffic light ticking, and stop arrival behaviour.
 *
 * SimulationEngine.tick() is plain Java with no JavaFX dependency,
 * so these tests run safely in headless CI.
 *
 * The setup pattern:
 * 1. Build a Game with road tiles
 * 2. Create a Route with an explicit tile path
 * 3. Create and assign a Vehicle to that route
 * 4. Call engine.tick() with a dt value
 * 5. Assert the vehicle moved / money changed / etc.
 */
@DisplayName("SimulationEngine Tests")
class SimulationEngineTest {

    private SimulationEngine engine;
    private Game game;
    private Player player;
    private GameState state;

    // A large dt value that guarantees the vehicle crosses at least one tile
    // boundary
    private static final double ONE_TILE_DT = 2.0;
    // Speed scale used by SimulationEngine: tilesPerSec = vehicleSpeed / 40.0
    // CITY_BUS speed = 40 → 1 tile/sec, so dt=1.5 → vehicle moves ~1.5 tiles

    @BeforeEach
    void setUp() {
        engine = new SimulationEngine();
        game = new Game(20, 20);
        player = new Player("TestPlayer", 100000);
        state = new GameState(game, player);
    }

    // Helpers

    /**
     * Lays a horizontal strip of ROAD tiles and returns the list of positions,
     * which can be used directly as a route tile path.
     */
    private List<Position> buildRoadStrip(int fromX, int toX, int y) {
        java.util.List<Position> path = new java.util.ArrayList<>();
        for (int x = fromX; x <= toX; x++) {
            game.getTile(x, y).setType(TileType.ROAD);
            path.add(new Position(x, y));
        }
        return path;
    }

    /**
     * Creates a circular route with the given tile path and assigns it to a
     * vehicle.
     */
    private Vehicle buildVehicleOnRoute(VehicleType type, List<Position> tilePath) {
        Route route = new Route("route-1", "Test Route",
                List.of(), tilePath);
        route.setCircular(true);
        game.addRoute(route);

        Vehicle vehicle = new Vehicle("v-1", type,
                tilePath.get(0));
        vehicle.assignRoute(route);
        game.addVehicle(vehicle);
        return vehicle;
    }

    // Vehicle movement

    @Test
    @DisplayName("Vehicle should move forward along its route after a tick")
    void testVehicleMovesForward() {
        List<Position> path = buildRoadStrip(0, 9, 0);
        Vehicle vehicle = buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        Position startPos = vehicle.getPosition();

        // Tick enough time for the vehicle to move at least one tile
        engine.tick(state, ONE_TILE_DT, 1);

        // Smooth position should have advanced from the start
        double startIso = startPos.getX() + startPos.getY(); // simple proxy
        double currentIso = vehicle.getSmoothX() + vehicle.getSmoothY();
        assertTrue(currentIso > startIso,
                "Vehicle smooth position should advance after a tick");
    }

    @Test
    @DisplayName("Inactive vehicle should not move")
    void testInactiveVehicleDoesNotMove() {
        List<Position> path = buildRoadStrip(0, 9, 0);
        Vehicle vehicle = buildVehicleOnRoute(VehicleType.CITY_BUS, path);
        vehicle.setActive(false);

        int indexBefore = vehicle.getRoutePathIndex();
        engine.tick(state, ONE_TILE_DT, 1);

        assertEquals(indexBefore, vehicle.getRoutePathIndex(),
                "Inactive vehicle should not advance its route index");
    }

    @Test
    @DisplayName("Vehicle with no route should not move")
    void testVehicleWithNoRouteDoesNotMove() {
        game.getTile(5, 5).setType(TileType.ROAD);
        Vehicle vehicle = new Vehicle("v-1", VehicleType.CITY_BUS,
                new Position(5, 5));
        game.addVehicle(vehicle);
        // No route assigned

        Position before = vehicle.getPosition();
        engine.tick(state, ONE_TILE_DT, 1);

        assertEquals(before, vehicle.getPosition(),
                "Vehicle without a route should not move");
    }

    @Test
    @DisplayName("Vehicle on a circular route should loop back to start")
    void testVehicleLoopsOnCircularRoute() {
        // Short 2-tile path so we can easily tick past the end
        List<Position> path = buildRoadStrip(0, 1, 0);
        Vehicle vehicle = buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        // Tick many times — the vehicle should keep moving without going inactive
        for (int i = 0; i < 20; i++) {
            engine.tick(state, ONE_TILE_DT, 1);
        }

        assertTrue(vehicle.isActive(),
                "Vehicle on a circular route should remain active indefinitely");
    }

    @Test
    @DisplayName("Vehicle should update its travel direction as it moves east")
    void testVehicleUpdatesDirectionMovingEast() {
        List<Position> path = buildRoadStrip(0, 9, 0);
        Vehicle vehicle = buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        engine.tick(state, ONE_TILE_DT, 1);

        assertEquals(Direction.EAST, vehicle.getTravelDirection(),
                "Vehicle moving along a horizontal path should face EAST");
    }

    // Maintenance costs

    @Test
    @DisplayName("Maintenance should be deducted from player capital each tick")
    void testMaintenanceDeductedEachTick() {
        List<Position> path = buildRoadStrip(0, 9, 0);
        buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        double capitalBefore = player.getLedger().getCurrentCapital();

        engine.tick(state, 60.0, 1); // tick for 60 game-seconds (1 game-minute)

        double capitalAfter = player.getLedger().getCurrentCapital();
        assertTrue(capitalAfter < capitalBefore,
                "Capital should decrease due to maintenance costs");
    }

    @Test
    @DisplayName("Maintenance deduction should scale with time elapsed")
    void testMaintenanceScalesWithTime() {
        List<Position> path = buildRoadStrip(0, 9, 0);
        buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        double capitalBefore = player.getLedger().getCurrentCapital();
        engine.tick(state, 60.0, 1);
        double capitalAfterOneMinute = player.getLedger().getCurrentCapital();
        double deductedOneMinute = capitalBefore - capitalAfterOneMinute;

        // Reset with a fresh engine and fresh state for 2-minute tick
        engine = new SimulationEngine();
        game = new Game(20, 20);
        player = new Player("TestPlayer", 100000);
        state = new GameState(game, player);
        path = buildRoadStrip(0, 9, 0);
        buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        engine.tick(state, 120.0, 1); // 2 game-minutes
        double deductedTwoMinutes = 100000 - player.getLedger().getCurrentCapital();

        assertEquals(deductedOneMinute * 2, deductedTwoMinutes, 1.0,
                "Maintenance for 2 minutes should be double that of 1 minute");
    }

    @Test
    @DisplayName("Two vehicles should cost more to maintain than one")
    void testTwoVehiclesCostMoreThanOne() {
        List<Position> path1 = buildRoadStrip(0, 9, 0);
        buildVehicleOnRoute(VehicleType.CITY_BUS, path1);

        double capitalBefore = player.getLedger().getCurrentCapital();
        engine.tick(state, 60.0, 1);
        double costOneVehicle = capitalBefore - player.getLedger().getCurrentCapital();

        // Add a second vehicle to the same route
        Route route = game.getRoutes().get(0);
        Vehicle v2 = new Vehicle("v-2", VehicleType.CITY_BUS, path1.get(0));
        v2.assignRoute(route);
        game.addVehicle(v2);

        capitalBefore = player.getLedger().getCurrentCapital();
        engine.tick(state, 60.0, 1);
        double costTwoVehicles = capitalBefore - player.getLedger().getCurrentCapital();

        assertTrue(costTwoVehicles > costOneVehicle,
                "Two vehicles should cost more to maintain than one");
    }

    // Traffic lights

    @Test
    @DisplayName("Traffic lights should tick down their phase timer each tick")
    void testTrafficLightTimerDecreases() {
        // Place a traffic light on a road tile
        game.getTile(5, 5).setType(TileType.ROAD);
        Position tlPos = new Position(5, 5);
        TrafficLight tl = new TrafficLight("tl-1", tlPos,
                List.of(Direction.NORTH, Direction.SOUTH));
        game.addTrafficLight(tl);

        double timerBefore = tl.getPhaseTimer();

        // Build a minimal route so tick() doesn't skip due to no vehicles
        List<Position> path = buildRoadStrip(0, 2, 0);
        buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        engine.tick(state, 1.0, 1);

        assertTrue(tl.getPhaseTimer() < timerBefore,
                "Traffic light phase timer should decrease after a tick");
    }

    @Test
    @DisplayName("Traffic light should cycle to next phase when timer expires")
    void testTrafficLightCyclesToNextPhase() {
        game.getTile(5, 5).setType(TileType.ROAD);
        TrafficLight tl = new TrafficLight("tl-1", new Position(5, 5),
                List.of(Direction.NORTH, Direction.SOUTH));
        game.addTrafficLight(tl);

        // Force the timer almost to zero
        tl.restoreState(0, 0.1);
        int phaseBefore = tl.getCurrentPhaseIndex();

        List<Position> path = buildRoadStrip(0, 2, 0);
        buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        engine.tick(state, 1.0, 1); // dt > remaining timer → phase should cycle

        assertNotEquals(phaseBefore, tl.getCurrentPhaseIndex(),
                "Traffic light should have advanced to the next phase");
    }

    // Stop arrival / delivery integration

    @Test
    @DisplayName("Vehicle should load cargo when it reaches a stop adjacent to a facility")
    void testVehicleLoadsCargAtStop() {
        // Road: (0,0) → (1,0) → (2,0)
        // Stop at (1,0), facility at (1,1)-(2,2)
        List<Position> path = buildRoadStrip(0, 2, 0);

        game.getTile(1, 0).setType(TileType.STOP);
        Stop stop = new Stop("stop-1", 1, 0, "Pickup");
        game.getStops().add(stop);

        Facility fac = new Facility("fac-1", "Wood Factory",
                1, 1, 2, 2,
                List.of(CargoType.WOOD), List.of());
        game.addFacility(fac);

        Route route = new Route("route-1", "Test Route", List.of(stop), path);
        route.setCircular(true);
        game.addRoute(route);

        Vehicle vehicle = new Vehicle("v-1", VehicleType.LOG_TRUCK, path.get(0));
        vehicle.assignRoute(route);
        game.addVehicle(vehicle);

        // Tick until vehicle reaches the stop at index 1
        for (int i = 0; i < 10; i++) {
            engine.tick(state, ONE_TILE_DT, 1);
            if (vehicle.isCarrying())
                break;
        }

        assertTrue(vehicle.isCarrying(),
                "Vehicle should have picked up cargo after passing the stop");
        assertEquals(CargoType.WOOD, vehicle.getCargoType(),
                "Vehicle should be carrying WOOD");
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("resetVehicle should allow the vehicle to restart from zero progress")
    void testResetVehicleClearsProgress() {
        List<Position> path = buildRoadStrip(0, 9, 0);
        Vehicle vehicle = buildVehicleOnRoute(VehicleType.CITY_BUS, path);

        // Tick to build up some internal progress
        engine.tick(state, ONE_TILE_DT, 1);

        // Reset — progress map entry should be removed
        engine.resetVehicle(vehicle.getId());

        // After reset the vehicle should still be active and able to move
        assertDoesNotThrow(() -> engine.tick(state, ONE_TILE_DT, 1),
                "Tick after reset should not throw");
        assertTrue(vehicle.isActive(),
                "Vehicle should still be active after reset");
    }

    // Bridge speed cap

    @Test
    @DisplayName("Vehicle on a wooden bridge at 4x speed should move slower than on road")
    void testBridgeSpeedCapSlowsVehicle() {
        // Route: road → bridge → road
        game.getTile(0, 0).setType(TileType.ROAD);
        game.getTile(1, 0).setType(TileType.BRIDGE);
        game.getTile(2, 0).setType(TileType.ROAD);
        // Register a wooden bridge so getBridgeAt() returns the cap
        Bridge bridge = new Bridge("b-1", "Wooden Bridge",
                1, 0, 1,
                Bridge.Orientation.HORIZONTAL,
                Bridge.BridgeType.WOODEN); // maxSpeedMultiplier = 1
        game.getBridges().add(bridge);

        List<Position> path = List.of(
                new Position(0, 0),
                new Position(1, 0),
                new Position(2, 0));

        Route route = new Route("route-1", "Bridge Route", List.of(), path);
        route.setCircular(true);
        game.addRoute(route);
        Vehicle vehicle = new Vehicle("v-1", VehicleType.CITY_BUS, path.get(0));
        vehicle.assignRoute(route);
        game.addVehicle(vehicle);

        // Manually position vehicle on the bridge tile
        vehicle.restoreRouteState(1, true);
        vehicle.setPosition(new Position(1, 0));

        double smoothXBefore = vehicle.getSmoothX();

        // Tick at 4x speed — wooden bridge caps at 1x, so effective dt = dt * 1/4
        engine.tick(state, ONE_TILE_DT, 4);

        double progressAtFourX = vehicle.getSmoothX() - smoothXBefore;

        // Now try the same on a plain road (no bridge cap)
        engine = new SimulationEngine();
        game = new Game(20, 20);
        player = new Player("TestPlayer", 100000);
        state = new GameState(game, player);
        List<Position> roadPath = buildRoadStrip(0, 4, 0);
        Vehicle roadVehicle = buildVehicleOnRoute(VehicleType.CITY_BUS, roadPath);
        double roadSmoothBefore = roadVehicle.getSmoothX();
        engine.tick(state, ONE_TILE_DT, 4); // same dt, same game speed, but no bridge cap
        double progressOnRoad = roadVehicle.getSmoothX() - roadSmoothBefore;

        assertTrue(progressAtFourX < progressOnRoad,
                "Vehicle on a wooden bridge at 4x should travel less distance than on open road");
    }
}