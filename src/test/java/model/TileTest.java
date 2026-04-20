package model;

import model.enums.TileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Tile class.
 * Tests tile state management and type changes.
 */
@DisplayName("Tile Tests")
class TileTest {

    private Tile tile;

    @BeforeEach
    void setUp() {
        tile = new Tile(0, 0);
    }

    @Test
    @DisplayName("Tile should initialize as EMPTY")
    void testTileInitialization() {
        assertEquals(TileType.EMPTY, tile.getType(), "New tile should be EMPTY");
    }

    @Test
    @DisplayName("Tile type should be changeable")
    void testSetTileType() {
        tile.setType(TileType.ROAD);
        assertEquals(TileType.ROAD, tile.getType(), "Tile type should change to ROAD");
    }

    @Test
    @DisplayName("Tile should support entity ID assignment")
    void testEntityId() {
        tile.setEntityId("road-1");
        assertEquals("road-1", tile.getEntityId(), "Entity ID should be stored");
    }

    @Test
    @DisplayName("Tile should support entity name assignment")
    void testEntityName() {
        tile.setEntityName("Main Street");
        assertEquals("Main Street", tile.getEntityName(), "Entity name should be stored");
    }

    @Test
    @DisplayName("Tile should allow null entity name")
    void testNullEntityName() {
        tile.setEntityName(null);
        assertNull(tile.getEntityName(), "Entity name should be null");
    }

    @Test
    @DisplayName("Multiple tile types should be possible")
    void testMultipleTileTypes() {
        tile.setType(TileType.WATER);
        assertEquals(TileType.WATER, tile.getType());

        tile.setType(TileType.BRIDGE);
        assertEquals(TileType.BRIDGE, tile.getType());

        tile.setType(TileType.STOP);
        assertEquals(TileType.STOP, tile.getType());
    }
}

