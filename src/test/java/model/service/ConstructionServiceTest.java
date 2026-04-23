package model.service;

import model.Game;
import model.Position;
import model.Road;
import model.Bridge;
import model.enums.TileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ConstructionService class.
 * Tests building and removing roads, bridges, and other structures.
 */
@DisplayName("ConstructionService Tests")
class ConstructionServiceTest {

    private ConstructionService service;
    private Game game;

    @BeforeEach
    void setUp() {
        service = new ConstructionService();
        game = new Game(100, 100);
    }

    @Test
    @DisplayName("Should build road on empty tile")
    void testBuildRoadOnEmpty() {
        Position pos = new Position(50, 50);
        boolean result = service.buildRoad(game, pos, Road.RoadType.HORIZONTAL);

        assertTrue(result, "Should successfully build road on empty tile");
        assertEquals(1, game.getRoads().size(), "Road should be added to game");
        assertEquals(TileType.ROAD, game.getTile(pos).getType(), "Tile type should be ROAD");
    }

    @Test
    @DisplayName("Should not build road on water")
    void testBuildRoadOnWater() {
        Position pos = new Position(50, 50);
        game.getTile(pos).setType(TileType.WATER);

        boolean result = service.buildRoad(game, pos, Road.RoadType.HORIZONTAL);
        assertFalse(result, "Should not build road on water tile");
        assertEquals(0, game.getRoads().size(), "Road should not be added");
    }

    @Test
    @DisplayName("Should remove road")
    void testRemoveRoad() {
        Position pos = new Position(50, 50);
        service.buildRoad(game, pos, Road.RoadType.HORIZONTAL);
        assertEquals(1, game.getRoads().size());

        boolean result = service.removeRoad(game, pos);
        assertTrue(result, "Should successfully remove road");
        assertEquals(0, game.getRoads().size(), "Road should be removed from game");
        assertEquals(TileType.EMPTY, game.getTile(pos).getType(), "Tile should become EMPTY");
    }

    @Test
    @DisplayName("Should remove road that doesn't exist")
    void testRemoveNonexistentRoad() {
        Position pos = new Position(50, 50);
        boolean result = service.removeRoad(game, pos);
        assertFalse(result, "Should fail to remove road that doesn't exist");
    }

    @Test
    @DisplayName("Should detect junction (3-way road)")
    void testDetectJunction() {
        // Create a 3-way junction
        Position center = new Position(50, 50);
        Position north = new Position(50, 49);
        Position south = new Position(50, 51);
        Position east = new Position(51, 50);

        service.buildRoad(game, center, Road.RoadType.HORIZONTAL);
        service.buildRoad(game, north, Road.RoadType.HORIZONTAL);
        service.buildRoad(game, south, Road.RoadType.HORIZONTAL);
        service.buildRoad(game, east, Road.RoadType.HORIZONTAL);

        boolean isJunction = service.isJunction(game, center);
        assertTrue(isJunction, "3-way intersection should be detected as junction");
    }

    @Test
    @DisplayName("Should build bridge on water")
    void testBuildBridgeOnWater() {
        Position pos = new Position(50, 50);
        game.getTile(pos).setType(TileType.WATER);

        Bridge bridge = new Bridge("bridge-1", "Test Bridge", 50, 50, 1, Bridge.Orientation.HORIZONTAL);
        boolean result = service.buildBridge(game, bridge);

        assertTrue(result, "Should successfully build bridge on water");
        assertEquals(1, game.getBridges().size(), "Bridge should be added to game");
        assertEquals(TileType.BRIDGE, game.getTile(pos).getType(), "Tile type should be BRIDGE");
    }

    @Test
    @DisplayName("Should not build bridge on non-water tile")
    void testBuildBridgeOnLand() {
        Position pos = new Position(50, 50);
        game.getTile(pos).setType(TileType.EMPTY);  // Not water

        Bridge bridge = new Bridge("bridge-1", "Test Bridge", 50, 50, 1, Bridge.Orientation.HORIZONTAL);
        boolean result = service.buildBridge(game, bridge);

        assertFalse(result, "Should not build bridge on non-water tile");
        assertEquals(0, game.getBridges().size(), "Bridge should not be added");
    }

    @Test
    @DisplayName("Should remove bridge")
    void testRemoveBridge() {
        Position pos = new Position(50, 50);
        game.getTile(pos).setType(TileType.WATER);

        Bridge bridge = new Bridge("bridge-1", "Test Bridge", 50, 50, 1, Bridge.Orientation.HORIZONTAL);
        service.buildBridge(game, bridge);
        assertEquals(1, game.getBridges().size());

        boolean result = service.removeBridge(game, pos);
        assertTrue(result, "Should successfully remove bridge");
        assertEquals(0, game.getBridges().size(), "Bridge should be removed");
        assertEquals(TileType.WATER, game.getTile(pos).getType(), "Tile should revert to water");
    }

    @Test
    @DisplayName("Should detect adjacency to road")
    void testIsAdjacentToRoad() {
        Position roadPos = new Position(50, 50);
        Position emptyPos = new Position(50, 51);

        service.buildRoad(game, roadPos, Road.RoadType.HORIZONTAL);

        boolean adjacent = service.isAdjacentToRoadCityOrFacility(game, emptyPos);
        assertTrue(adjacent, "Empty tile next to road should be adjacent");
    }
}

}
