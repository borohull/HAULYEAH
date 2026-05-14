package model.service;

import model.Game;
import model.Position;
import model.enums.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RoadPathfinder — verifies that BFS pathfinding correctly
 * finds (or refuses to find) paths across different tile configurations.
 *
 * The setup pattern for every test is:
 * 1. Create a Game grid
 * 2. Set tile types directly to simulate roads/water/etc.
 * 3. Call RoadPathfinder.findPath() and assert the result
 */
@DisplayName("RoadPathfinder Tests")
class RoadPathfinderTest {

        private Game game;

        @BeforeEach
        void setUp() {
                game = new Game(20, 20);
        }

        // Helpers

        private void buildHorizontalRoad(int fromX, int toX, int y) {
                for (int x = fromX; x <= toX; x++) {
                        game.getTile(x, y).setType(TileType.ROAD);
                }
        }

        private void buildVerticalRoad(int x, int fromY, int toY) {
                for (int y = fromY; y <= toY; y++) {
                        game.getTile(x, y).setType(TileType.ROAD);
                }
        }

        // Same-position edge case

        @Test
        @DisplayName("Path from a tile to itself should return a single-element list")
        void testSameStartAndEnd() {
                game.getTile(5, 5).setType(TileType.ROAD);
                Position pos = new Position(5, 5);

                List<Position> path = RoadPathfinder.findPath(game, pos, pos);

                assertNotNull(path, "Path should not be null");
                assertEquals(1, path.size(), "Path to self should contain exactly one tile");
                assertEquals(pos, path.get(0), "That tile should be the position itself");
        }

        // Basic connectivity

        @Test
        @DisplayName("Should find a straight horizontal path")
        void testStraightHorizontalPath() {
                buildHorizontalRoad(3, 7, 5);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(3, 5), new Position(7, 5));

                assertNotNull(path, "Path should be found");
                assertEquals(new Position(3, 5), path.get(0),
                                "Path should start at the start position");
                assertEquals(new Position(7, 5), path.get(path.size() - 1),
                                "Path should end at the end position");
        }

        @Test
        @DisplayName("Should find a straight vertical path")
        void testStraightVerticalPath() {
                buildVerticalRoad(5, 2, 8);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(5, 2), new Position(5, 8));

                assertNotNull(path, "Path should be found");
                assertEquals(new Position(5, 2), path.get(0));
                assertEquals(new Position(5, 8), path.get(path.size() - 1));
        }

        @Test
        @DisplayName("Horizontal path length should match the number of tiles between start and end")
        void testPathLengthIsCorrect() {
                buildHorizontalRoad(0, 4, 0); // 5 tiles: (0,0) to (4,0)

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(4, 0));

                assertNotNull(path);
                assertEquals(5, path.size(),
                                "Path across 5 tiles should have length 5");
        }

        @Test
        @DisplayName("Path should include every tile between start and end")
        void testPathContainsAllIntermediateTiles() {
                buildHorizontalRoad(0, 3, 0); // (0,0) → (1,0) → (2,0) → (3,0)

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(3, 0));

                assertNotNull(path);
                assertTrue(path.contains(new Position(1, 0)),
                                "Path should include intermediate tile (1,0)");
                assertTrue(path.contains(new Position(2, 0)),
                                "Path should include intermediate tile (2,0)");
        }

        // No path exists

        @Test
        @DisplayName("Should return null when start and end are not connected")
        void testDisconnectedRoads() {
                buildHorizontalRoad(0, 2, 5); // (0,5) to (2,5)
                buildHorizontalRoad(4, 6, 5); // (4,5) to (6,5) — gap at (3,5)

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 5), new Position(6, 5));

                assertNull(path, "Should return null when roads are not connected");
        }

        @Test
        @DisplayName("Should return null when start tile is not walkable")
        void testStartTileNotWalkable() {
                game.getTile(5, 5).setType(TileType.ROAD);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(5, 5));

                assertNull(path, "Should return null when start tile is not walkable");
        }

        @Test
        @DisplayName("Should return null when end tile is not walkable")
        void testEndTileNotWalkable() {
                buildHorizontalRoad(0, 4, 0);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(5, 0));

                assertNull(path, "Should return null when end tile is not walkable");
        }

        @Test
        @DisplayName("Should not route through water tiles")
        void testDoesNotRouteThoughWater() {
                buildHorizontalRoad(0, 2, 5);
                game.getTile(3, 5).setType(TileType.WATER);
                buildHorizontalRoad(4, 6, 5);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 5), new Position(6, 5));

                assertNull(path, "Path should not cross water without a bridge");
        }

        @Test
        @DisplayName("Should not route through empty grass tiles")
        void testDoesNotRouteThoughEmptyTiles() {
                game.getTile(0, 0).setType(TileType.ROAD);
                game.getTile(5, 0).setType(TileType.ROAD);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(5, 0));

                assertNull(path, "Path should not cut across empty grass tiles");
        }

        // L-shaped and corner paths

        @Test
        @DisplayName("Should find an L-shaped path via a corner")
        void testLShapedPath() {
                buildHorizontalRoad(0, 5, 5);
                buildVerticalRoad(5, 5, 10);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 5), new Position(5, 10));

                assertNotNull(path, "Should find an L-shaped path");
                assertEquals(new Position(0, 5), path.get(0));
                assertEquals(new Position(5, 10), path.get(path.size() - 1));
        }

        @Test
        @DisplayName("Should find path when multiple routes exist — BFS gives shortest")
        void testFindsShortestWhenMultipleRoutesExist() {
                buildHorizontalRoad(0, 10, 5);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 5), new Position(10, 5));

                assertNotNull(path);
                assertEquals(11, path.size(),
                                "BFS should find the shortest path of 11 tiles");
        }

        // Walkable tile types

        @Test
        @DisplayName("STOP tiles should be walkable and included in paths")
        void testStopTilesAreWalkable() {
                game.getTile(0, 0).setType(TileType.ROAD);
                game.getTile(1, 0).setType(TileType.STOP); // stop in the middle
                game.getTile(2, 0).setType(TileType.ROAD);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(2, 0));

                assertNotNull(path, "Path through a STOP tile should be found");
                assertTrue(path.contains(new Position(1, 0)),
                                "Path should pass through the STOP tile");
        }

        @Test
        @DisplayName("BRIDGE tiles should be walkable and included in paths")
        void testBridgeTilesAreWalkable() {
                game.getTile(0, 0).setType(TileType.ROAD);
                game.getTile(1, 0).setType(TileType.BRIDGE); // bridge over water
                game.getTile(2, 0).setType(TileType.ROAD);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(2, 0));

                assertNotNull(path, "Path through a BRIDGE tile should be found");
                assertTrue(path.contains(new Position(1, 0)),
                                "Path should pass through the BRIDGE tile");
        }

        @Test
        @DisplayName("CITY_ROAD tiles should be walkable")
        void testCityRoadTilesAreWalkable() {
                game.getTile(0, 0).setType(TileType.ROAD);
                game.getTile(1, 0).setType(TileType.CITY_ROAD);
                game.getTile(2, 0).setType(TileType.ROAD);

                List<Position> path = RoadPathfinder.findPath(
                                game, new Position(0, 0), new Position(2, 0));

                assertNotNull(path, "Path through a CITY_ROAD tile should be found");
        }
}