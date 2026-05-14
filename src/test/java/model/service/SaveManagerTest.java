package model.service;

import model.*;
import model.enums.TileType;
import model.enums.VehicleType;
import org.junit.jupiter.api.*;

import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SaveManager — verifies that game state is correctly
 * written to disk and read back without losing data.
 *
 * Each test starts with a clean save file and deletes it afterward
 * so tests don't interfere with each other.
 */
@DisplayName("SaveManager Tests")
class SaveManagerTest {

    private SaveManager saveManager;

    // Helper: builds a minimal but valid GameState we can save and reload
    private GameState buildMinimalState() {
        Game game = new Game(20, 20);
        game.setWorldName("Test World");
        Player player = new Player("TestPlayer", 50000);
        return new GameState(game, player);
    }

    @BeforeEach
    void setUp() {
        saveManager = new SaveManager();
        // Always start each test with no save file
        saveManager.delete(0);
    }

    @AfterEach
    void tearDown() {
        saveManager.delete(0);
    }

    @Test
    @DisplayName("No save file should exist before saving")
    void testNoSaveExistsInitially() {
        assertFalse(saveManager.saveExists(),
                "Save file should not exist before any save is made");
    }

    @Test
    @DisplayName("Save file should exist after saving")
    void testSaveExistsAfterSave() {
        saveManager.save(buildMinimalState(), 0);
        assertTrue(saveManager.saveExists(),
                "Save file should exist after saving");
    }

    @Test
    @DisplayName("Save file should not exist after deleting")
    void testSaveDeletedAfterDelete() {
        saveManager.save(buildMinimalState(), 0);
        saveManager.delete(0);
        assertFalse(saveManager.saveExists(),
                "Save file should be gone after deletion");
    }

    @Test
    @DisplayName("Load should return null when no save file exists")
    void testLoadReturnsNullWhenNoFile() {
        GameState result = saveManager.load(0);
        assertNull(result, "Loading with no save file should return null");
    }

    // World metadata

    @Test
    @DisplayName("World name should survive a save/load cycle")
    void testWorldNamePersistedCorrectly() {
        GameState original = buildMinimalState();
        original.getMap().setWorldName("My Cool World");

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded, "Loaded state should not be null");
        assertEquals("My Cool World", loaded.getMap().getWorldName(),
                "World name should be the same after loading");
    }

    @Test
    @DisplayName("Player name should survive a save/load cycle")
    void testPlayerNamePersistedCorrectly() {
        GameState original = buildMinimalState();

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals("TestPlayer", loaded.getPlayer().getName(),
                "Player name should be the same after loading");
    }

    @Test
    @DisplayName("Player capital should survive a save/load cycle")
    void testPlayerCapitalPersistedCorrectly() {
        GameState original = buildMinimalState();
        // Spend some money so it's not the default value
        original.getPlayer().getLedger().spend(10000,
                model.enums.TransactionType.BUILD, "test");

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(40000, loaded.getPlayer().getLedger().getCurrentCapital(), 0.01,
                "Capital should reflect the spend after loading");
    }

    // Roads

    @Test
    @DisplayName("Roads should survive a save/load cycle")
    void testRoadsPersistedCorrectly() {
        GameState original = buildMinimalState();
        Game game = original.getMap();

        // Build a road manually (bypass ConstructionService cost check)
        Road road = new Road("road-1", 5, 5, Road.RoadType.HORIZONTAL);
        game.getRoads().add(road);
        game.getTile(5, 5).setType(TileType.ROAD);

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(1, loaded.getMap().getRoads().size(),
                "One road should be present after loading");
        assertEquals(TileType.ROAD, loaded.getMap().getTile(5, 5).getType(),
                "Tile at (5,5) should still be a road after loading");
    }

    @Test
    @DisplayName("Multiple roads should all survive a save/load cycle")
    void testMultipleRoadsPersistedCorrectly() {
        GameState original = buildMinimalState();
        Game game = original.getMap();

        for (int x = 3; x <= 7; x++) {
            game.getRoads().add(new Road("road-" + x, x, 10, Road.RoadType.HORIZONTAL));
            game.getTile(x, 10).setType(TileType.ROAD);
        }

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(5, loaded.getMap().getRoads().size(),
                "All 5 roads should be present after loading");
    }

    // Stops

    @Test
    @DisplayName("Stops should survive a save/load cycle")
    void testStopsPersistedCorrectly() {
        GameState original = buildMinimalState();
        Game game = original.getMap();

        Stop stop = new Stop("stop-1", 6, 6, "Bus Stop A");
        game.getStops().add(stop);
        game.getTile(6, 6).setType(TileType.STOP);

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(1, loaded.getMap().getStops().size(),
                "One stop should be present after loading");
        assertEquals("Bus Stop A", loaded.getMap().getStops().get(0).getName(),
                "Stop name should be preserved");
        assertEquals(TileType.STOP, loaded.getMap().getTile(6, 6).getType(),
                "Tile at (6,6) should still be a stop after loading");
    }

    // Vehicles

    @Test
    @DisplayName("Vehicles should survive a save/load cycle")
    void testVehiclesPersistedCorrectly() {
        GameState original = buildMinimalState();
        Game game = original.getMap();

        // Put a road tile down so the vehicle has somewhere to stand
        game.getTile(8, 8).setType(TileType.ROAD);
        Vehicle vehicle = new Vehicle("vehicle-1", VehicleType.CITY_BUS, new Position(8, 8));
        game.addVehicle(vehicle);

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(1, loaded.getMap().getVehicles().size(),
                "One vehicle should be present after loading");
        assertEquals(VehicleType.CITY_BUS,
                loaded.getMap().getVehicles().get(0).getType(),
                "Vehicle type should be preserved");
    }

    // Tile types

    @Test
    @DisplayName("Water tiles should survive a save/load cycle")
    void testWaterTilePersistedCorrectly() {
        GameState original = buildMinimalState();
        original.getMap().getTile(10, 10).setType(TileType.WATER);

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(TileType.WATER, loaded.getMap().getTile(10, 10).getType(),
                "Water tile should still be water after loading");
    }

    @Test
    @DisplayName("Empty tiles should remain empty after load")
    void testEmptyTileRemainsEmpty() {
        GameState original = buildMinimalState();

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(TileType.EMPTY, loaded.getMap().getTile(0, 0).getType(),
                "Untouched tile should remain EMPTY after loading");
    }

    // Routes

    @Test
    @DisplayName("Routes should survive a save/load cycle")
    void testRoutesPersistedCorrectly() {
        GameState original = buildMinimalState();
        Game game = original.getMap();

        Stop stopA = new Stop("stop-1", 3, 3, "Stop A");
        Stop stopB = new Stop("stop-2", 5, 3, "Stop B");
        game.getStops().add(stopA);
        game.getStops().add(stopB);

        java.util.List<Position> tilePath = java.util.List.of(
                new Position(3, 3),
                new Position(4, 3),
                new Position(5, 3));
        Route route = new Route("route-1", "Route 1",
                java.util.List.of(stopA, stopB), tilePath);
        game.addRoute(route);

        saveManager.save(original, 0);
        GameState loaded = saveManager.load(0);

        assertNotNull(loaded);
        assertEquals(1, loaded.getMap().getRoutes().size(),
                "One route should be present after loading");
        assertEquals("Route 1", loaded.getMap().getRoutes().get(0).getName(),
                "Route name should be preserved");
        assertEquals(3, loaded.getMap().getRoutes().get(0).getTilePath().size(),
                "Route tile path length should be preserved");
    }

    @Test
    @DisplayName("Saving twice should overwrite, not duplicate")
    void testSavingTwiceOverwrites() {
        GameState state = buildMinimalState();
        state.getMap().setWorldName("First Save");

        saveManager.save(state, 0);

        state.getMap().setWorldName("Second Save");
        saveManager.save(state, 0);

        GameState loaded = saveManager.load(0);
        assertNotNull(loaded);
        assertEquals("Second Save", loaded.getMap().getWorldName(),
                "Second save should overwrite the first");
    }
}