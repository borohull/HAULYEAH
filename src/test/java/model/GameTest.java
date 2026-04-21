package model;

import model.enums.TileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Game class.
 * Tests game state management and tile grid operations.
 */
@DisplayName("Game Tests")
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game(100, 100);
    }

    @Test
    @DisplayName("Game should initialize with correct dimensions")
    void testGameInitialization() {
        assertEquals(100, game.getWidth(), "Game width should be 100");
        assertEquals(100, game.getHeight(), "Game height should be 100");
    }

    @Test
    @DisplayName("Game should have a world name")
    void testWorldName() {
        game.setWorldName("Test World");
        assertEquals("Test World", game.getWorldName());
    }

    @Test
    @DisplayName("Game should handle invalid world names")
    void testInvalidWorldName() {
        game.setWorldName(null);
        assertEquals("Unnamed World", game.getWorldName());

        game.setWorldName("");
        assertEquals("Unnamed World", game.getWorldName());
    }

    @Test
    @DisplayName("getTile should return valid tiles within bounds")
    void testGetTileWithinBounds() {
        Tile tile = game.getTile(0, 0);
        assertNotNull(tile, "Tile at (0,0) should exist");
        assertEquals(0, tile.getPosition().getX());
        assertEquals(0, tile.getPosition().getY());
    }

    @Test
    @DisplayName("getTile should return null outside bounds")
    void testGetTileOutOfBounds() {
        Tile tile = game.getTile(150, 150);
        assertNull(tile, "Tile outside bounds should be null");

        tile = game.getTile(-1, -1);
        assertNull(tile, "Negative coordinates should return null");
    }

    @Test
    @DisplayName("inBounds should correctly validate coordinates")
    void testInBounds() {
        assertTrue(game.inBounds(0, 0), "Origin should be in bounds");
        assertTrue(game.inBounds(99, 99), "Max coordinates should be in bounds");
        assertFalse(game.inBounds(100, 100), "Coordinates at edge should be out of bounds");
        assertFalse(game.inBounds(-1, 0), "Negative X should be out of bounds");
    }

    @Test
    @DisplayName("Game should track roads")
    void testRoadTracking() {
        Road road = new Road("road-1", 10, 20, model.Road.RoadType.HORIZONTAL);
        game.getRoads().add(road);
        assertEquals(1, game.getRoads().size(), "Game should track roads");
    }

    @Test
    @DisplayName("Game should track stops")
    void testStopTracking() {
        Stop stop = new Stop("stop-1", 15, 25, "Bus Stop");
        game.getStops().add(stop);
        assertEquals(1, game.getStops().size(), "Game should track stops");
    }

    @Test
    @DisplayName("Game should track vehicles")
    void testVehicleTracking() {
        assertEquals(0, game.getVehicles().size(), "New game should have no vehicles");
    }

    @Test
    @DisplayName("Game should provide tile via Position object")
    void testGetTileWithPosition() {
        Position pos = new Position(5, 10);
        Tile tile = game.getTile(pos);
        assertNotNull(tile, "Should get tile from Position");
        assertEquals(5, tile.getPosition().getX());
        assertEquals(10, tile.getPosition().getY());
    }
}



