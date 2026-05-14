package controller;

import model.Game;
import model.GameState;
import model.Player;
import model.Position;
import model.Route;
import model.Stop;
import model.Vehicle;
import model.enums.TileType;
import model.enums.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the delete and rename route features added to GameController.
 */
@DisplayName("GameController Route Management Tests")
class GameControllerRouteTest {

    private Game game;
    private GameController controller;

    @BeforeEach
    void setUp() {
        game = new Game(20, 20);
        Player player = new Player("TestPlayer", 100000);
        GameState state = new GameState(game, player);
        controller = new GameController(state);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Route addRoute(String id, String name) {
        List<Position> path = List.of(
                new Position(0, 0),
                new Position(1, 0),
                new Position(2, 0));
        Route route = new Route(id, name, List.of(), path);
        game.addRoute(route);
        return route;
    }

    private Vehicle addVehicleOnRoute(Route route) {
        Vehicle vehicle = new Vehicle("v-" + route.getId(),
                VehicleType.CITY_BUS, new Position(0, 0));
        vehicle.assignRoute(route);
        game.addVehicle(vehicle);
        return vehicle;
    }

    // ── deleteRoute ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteRoute should remove the route from the game")
    void testDeleteRouteRemovesItFromGame() {
        Route route = addRoute("route-1", "Route 1");
        assertEquals(1, game.getRoutes().size());

        controller.deleteRoute(route);

        assertEquals(0, game.getRoutes().size(),
                "Route should be removed from the game after deletion");
    }

    @Test
    @DisplayName("deleteRoute should unassign any vehicle that was using the route")
    void testDeleteRouteUnassignsVehicles() {
        Route route = addRoute("route-1", "Route 1");
        Vehicle vehicle = addVehicleOnRoute(route);

        assertNotNull(vehicle.getRoute(), "Vehicle should have a route before deletion");

        controller.deleteRoute(route);

        assertNull(vehicle.getRoute(),
                "Vehicle route should be null after its route is deleted");
    }

    @Test
    @DisplayName("deleteRoute should park any vehicle that was using the route")
    void testDeleteRouteParkesVehicles() {
        Route route = addRoute("route-1", "Route 1");
        Vehicle vehicle = addVehicleOnRoute(route);

        controller.deleteRoute(route);

        assertFalse(vehicle.isActive(),
                "Vehicle should be parked after its route is deleted");
    }

    @Test
    @DisplayName("deleteRoute should not affect vehicles on a different route")
    void testDeleteRouteDoesNotAffectOtherVehicles() {
        Route route1 = addRoute("route-1", "Route 1");
        Route route2 = addRoute("route-2", "Route 2");
        Vehicle vehicle = addVehicleOnRoute(route2);

        controller.deleteRoute(route1);

        assertNotNull(vehicle.getRoute(),
                "Vehicle on a different route should not be unassigned");
        assertTrue(vehicle.isActive(),
                "Vehicle on a different route should remain active");
    }

    @Test
    @DisplayName("deleteRoute should leave all other routes intact")
    void testDeleteRouteDoesNotRemoveOtherRoutes() {
        Route route1 = addRoute("route-1", "Route 1");
        addRoute("route-2", "Route 2");
        addRoute("route-3", "Route 3");

        controller.deleteRoute(route1);

        assertEquals(2, game.getRoutes().size(),
                "Only the deleted route should be removed");
    }

    @Test
    @DisplayName("deleteRoute with null should do nothing")
    void testDeleteRouteWithNull() {
        addRoute("route-1", "Route 1");

        assertDoesNotThrow(() -> controller.deleteRoute(null),
                "Passing null should not throw an exception");
        assertEquals(1, game.getRoutes().size(),
                "No route should be removed when null is passed");
    }

    @Test
    @DisplayName("Multiple vehicles on the same route should all be unassigned on delete")
    void testDeleteRouteUnassignsMultipleVehicles() {
        Route route = addRoute("route-1", "Route 1");

        Vehicle v1 = new Vehicle("v-1", VehicleType.CITY_BUS, new Position(0, 0));
        Vehicle v2 = new Vehicle("v-2", VehicleType.CITY_BUS, new Position(0, 0));
        v1.assignRoute(route);
        v2.assignRoute(route);
        game.addVehicle(v1);
        game.addVehicle(v2);

        controller.deleteRoute(route);

        assertNull(v1.getRoute(), "First vehicle should be unassigned");
        assertNull(v2.getRoute(), "Second vehicle should be unassigned");
    }

    // ── renameRoute ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("renameRoute should update the route name")
    void testRenameRouteUpdatesName() {
        Route route = addRoute("route-1", "Old Name");

        controller.renameRoute(route, "New Name");

        assertEquals("New Name", route.getName(),
                "Route name should be updated after renaming");
    }

    @Test
    @DisplayName("renameRoute should trim whitespace from the new name")
    void testRenameRouteTrimsWhitespace() {
        Route route = addRoute("route-1", "Old Name");

        controller.renameRoute(route, "  New Name  ");

        assertEquals("New Name", route.getName(),
                "Leading and trailing whitespace should be trimmed");
    }

    @Test
    @DisplayName("renameRoute with blank name should not change the route name")
    void testRenameRouteWithBlankNameDoesNothing() {
        Route route = addRoute("route-1", "Original Name");

        controller.renameRoute(route, "   ");

        assertEquals("Original Name", route.getName(),
                "Blank name should not overwrite the existing route name");
    }

    @Test
    @DisplayName("renameRoute with empty string should not change the route name")
    void testRenameRouteWithEmptyStringDoesNothing() {
        Route route = addRoute("route-1", "Original Name");

        controller.renameRoute(route, "");

        assertEquals("Original Name", route.getName(),
                "Empty string should not overwrite the existing route name");
    }

    @Test
    @DisplayName("renameRoute with null name should not change the route name")
    void testRenameRouteWithNullDoesNothing() {
        Route route = addRoute("route-1", "Original Name");

        controller.renameRoute(route, null);

        assertEquals("Original Name", route.getName(),
                "Null name should not overwrite the existing route name");
    }

    @Test
    @DisplayName("renameRoute with null route should not throw")
    void testRenameRouteWithNullRouteDoesNotThrow() {
        assertDoesNotThrow(() -> controller.renameRoute(null, "New Name"),
                "Passing a null route should not throw an exception");
    }

    @Test
    @DisplayName("Renaming a route should not affect other routes")
    void testRenameRouteDoesNotAffectOtherRoutes() {
        Route route1 = addRoute("route-1", "Route One");
        Route route2 = addRoute("route-2", "Route Two");

        controller.renameRoute(route1, "Renamed Route One");

        assertEquals("Route Two", route2.getName(),
                "Renaming one route should not change another route's name");
    }

    @Test
    @DisplayName("Renaming a route should not unassign its vehicles")
    void testRenameRouteDoesNotUnassignVehicles() {
        Route route = addRoute("route-1", "Route 1");
        Vehicle vehicle = addVehicleOnRoute(route);

        controller.renameRoute(route, "New Name");

        assertNotNull(vehicle.getRoute(),
                "Renaming a route should not unassign its vehicles");
        assertTrue(vehicle.isActive(),
                "Renaming a route should not park its vehicles");
    }
}