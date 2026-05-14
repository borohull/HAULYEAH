package model;

import model.enums.CargoType;
import model.enums.Direction;
import model.enums.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Vehicle class.
 *
 * Covers: construction, route assignment, path index advancement
 * (ping-pong and circular), cargo loading/unloading, and state restoration.
 */
@DisplayName("Vehicle Tests")
class VehicleTest {

    private Vehicle vehicle;
    private static final Position START = new Position(0, 0);

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("v-1", VehicleType.CITY_BUS, START);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a simple linear route: (0,0) → (1,0) → (2,0) → ... → (length-1, 0)
     */
    private Route buildLinearRoute(int length) {
        java.util.List<Position> path = new java.util.ArrayList<>();
        for (int i = 0; i < length; i++) {
            path.add(new Position(i, 0));
        }
        return new Route("r-1", "Test Route", List.of(), path);
    }

    /**
     * Same as buildLinearRoute but marks the route as circular.
     */
    private Route buildCircularRoute(int length) {
        Route route = buildLinearRoute(length);
        route.setCircular(true);
        return route;
    }

    // ── Construction ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Vehicle should store its id, type, and starting position")
    void testConstruction() {
        assertEquals("v-1", vehicle.getId());
        assertEquals(VehicleType.CITY_BUS, vehicle.getType());
        assertEquals(START, vehicle.getPosition());
    }

    @Test
    @DisplayName("New vehicle should be active by default")
    void testNewVehicleIsActive() {
        assertTrue(vehicle.isActive(), "Newly created vehicle should be active");
    }

    @Test
    @DisplayName("New vehicle should not be looping by default")
    void testNewVehicleIsNotLooping() {
        assertFalse(vehicle.isLooping(), "Newly created vehicle should not loop");
    }

    @Test
    @DisplayName("New vehicle should have no route assigned")
    void testNewVehicleHasNoRoute() {
        assertNull(vehicle.getRoute(), "Newly created vehicle should have no route");
    }

    @Test
    @DisplayName("New vehicle should start empty (no cargo)")
    void testNewVehicleIsEmpty() {
        assertFalse(vehicle.isCarrying(), "Newly created vehicle should carry no cargo");
        assertNull(vehicle.getCargoType());
        assertEquals(0, vehicle.getCargoAmount());
    }

    @Test
    @DisplayName("Smooth position should match starting position on creation")
    void testSmoothPositionMatchesStartPosition() {
        assertEquals(START.getX(), vehicle.getSmoothX(), 0.001);
        assertEquals(START.getY(), vehicle.getSmoothY(), 0.001);
    }

    // ── Stats from VehicleType ────────────────────────────────────────────────

    @Test
    @DisplayName("Speed should come from VehicleType")
    void testSpeedFromVehicleType() {
        assertEquals(VehicleType.CITY_BUS.getSpeed(), vehicle.getSpeed());
    }

    @Test
    @DisplayName("Capacity should come from VehicleType")
    void testCapacityFromVehicleType() {
        assertEquals(VehicleType.CITY_BUS.getCapacity(), vehicle.getCapacity());
    }

    @Test
    @DisplayName("Maintenance cost should come from VehicleType")
    void testMaintenanceCostFromVehicleType() {
        assertEquals(VehicleType.CITY_BUS.getMaintenanceCost(), vehicle.getMaintenanceCost());
    }

    // ── assignRoute ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("assignRoute should set the vehicle's route")
    void testAssignRouteSetsRoute() {
        Route route = buildLinearRoute(5);
        vehicle.assignRoute(route);
        assertEquals(route, vehicle.getRoute());
    }

    @Test
    @DisplayName("assignRoute should position the vehicle at the start of the path")
    void testAssignRouteSetsPositionToPathStart() {
        Route route = buildLinearRoute(5);
        vehicle.assignRoute(route);
        assertEquals(new Position(0, 0), vehicle.getPosition());
    }

    @Test
    @DisplayName("assignRoute should reset path index to 0")
    void testAssignRouteResetsPathIndex() {
        Route route = buildLinearRoute(5);
        vehicle.assignRoute(route);
        assertEquals(0, vehicle.getRoutePathIndex());
    }

    @Test
    @DisplayName("assignRoute should set vehicle moving forward")
    void testAssignRouteSetsMovingForward() {
        Route route = buildLinearRoute(5);
        vehicle.assignRoute(route);
        assertTrue(vehicle.isMovingForward());
    }

    @Test
    @DisplayName("assignRoute should reactivate a parked vehicle")
    void testAssignRouteReactivatesParkedVehicle() {
        vehicle.setActive(false);
        Route route = buildLinearRoute(5);
        vehicle.assignRoute(route);
        assertTrue(vehicle.isActive(), "Assigning a route should reactivate a parked vehicle");
    }

    @Test
    @DisplayName("assignRoute with a route that has a stop should position vehicle at that stop")
    void testAssignRoutePositionsAtFirstStop() {
        Stop stop = new Stop("s-1", 2, 0, "Stop A");
        List<Position> path = List.of(
                new Position(0, 0),
                new Position(1, 0),
                new Position(2, 0),
                new Position(3, 0));
        Route route = new Route("r-1", "Route", List.of(stop), path);

        vehicle.assignRoute(route);

        assertEquals(new Position(2, 0), vehicle.getPosition(),
                "Vehicle should start at the first stop's position");
        assertEquals(2, vehicle.getRoutePathIndex(),
                "Route path index should point to the first stop");
    }

    @Test
    @DisplayName("assignRoute(null) should clear the route")
    void testAssignNullClearsRoute() {
        vehicle.assignRoute(buildLinearRoute(5));
        vehicle.assignRoute(null);
        assertNull(vehicle.getRoute(), "Assigning null should clear the route");
    }

    // ── advanceRoutePathIndex — ping-pong (non-circular) ──────────────────────

    @Test
    @DisplayName("advanceRoutePathIndex should move forward along the path")
    void testAdvanceMovesForward() {
        vehicle.assignRoute(buildLinearRoute(5));
        assertEquals(0, vehicle.getRoutePathIndex());

        vehicle.advanceRoutePathIndex();

        assertEquals(1, vehicle.getRoutePathIndex(),
                "Index should advance by one when moving forward");
    }

    @Test
    @DisplayName("Vehicle should reverse direction when it reaches the end of a non-circular route")
    void testReverseAtEndOfPath() {
        Route route = buildLinearRoute(3); // tiles: 0, 1, 2
        vehicle.assignRoute(route);

        // Advance to the last tile (index 2)
        vehicle.advanceRoutePathIndex(); // → 1
        vehicle.advanceRoutePathIndex(); // → 2

        assertTrue(vehicle.isMovingForward(), "Should still be forward before the end");

        vehicle.advanceRoutePathIndex(); // hits the end → reverses

        assertFalse(vehicle.isMovingForward(),
                "Vehicle should be moving backward after reaching the end");
    }

    @Test
    @DisplayName("Vehicle should reverse direction again when it reaches the start going backward")
    void testReverseAtStartOfPath() {
        Route route = buildLinearRoute(3);
        vehicle.assignRoute(route);

        // Go to end then come back to start
        vehicle.advanceRoutePathIndex(); // 0→1
        vehicle.advanceRoutePathIndex(); // 1→2
        vehicle.advanceRoutePathIndex(); // reverses at end
        vehicle.advanceRoutePathIndex(); // 2→1
        vehicle.advanceRoutePathIndex(); // 1→0
        vehicle.advanceRoutePathIndex(); // reverses at start

        assertTrue(vehicle.isMovingForward(),
                "Vehicle should be moving forward again after bouncing off the start");
    }

    @Test
    @DisplayName("Index should not go below 0 on a non-circular route")
    void testIndexDoesNotGoBelowZero() {
        Route route = buildLinearRoute(3);
        vehicle.assignRoute(route);

        // Force to start moving backward
        vehicle.advanceRoutePathIndex(); // 0→1
        vehicle.advanceRoutePathIndex(); // 1→2
        vehicle.advanceRoutePathIndex(); // reverses
        vehicle.advanceRoutePathIndex(); // 2→1
        vehicle.advanceRoutePathIndex(); // 1→0
        vehicle.advanceRoutePathIndex(); // reverses again
        vehicle.advanceRoutePathIndex(); // should move forward again, not go to -1

        assertTrue(vehicle.getRoutePathIndex() >= 0,
                "Route path index should never go below 0");
    }

    @Test
    @DisplayName("Index should not exceed path length - 1 on a non-circular route")
    void testIndexDoesNotExceedPathEnd() {
        Route route = buildLinearRoute(4);
        vehicle.assignRoute(route);

        // Advance well past the end
        for (int i = 0; i < 10; i++) {
            vehicle.advanceRoutePathIndex();
        }

        assertTrue(vehicle.getRoutePathIndex() <= 3,
                "Route path index should never exceed the last valid index");
    }

    // ── advanceRoutePathIndex — circular ──────────────────────────────────────

    @Test
    @DisplayName("Circular route should wrap from last tile back to index 0")
    void testCircularRouteWrapsAround() {
        Route route = buildCircularRoute(3); // tiles: 0, 1, 2
        vehicle.assignRoute(route);

        vehicle.advanceRoutePathIndex(); // 0→1
        vehicle.advanceRoutePathIndex(); // 1→2
        vehicle.advanceRoutePathIndex(); // 2→0 (wrap)

        assertEquals(0, vehicle.getRoutePathIndex(),
                "Circular route should wrap back to index 0 after the last tile");
    }

    @Test
    @DisplayName("Circular route should never reverse direction")
    void testCircularRouteNeverReversesDirection() {
        Route route = buildCircularRoute(4);
        vehicle.assignRoute(route);

        for (int i = 0; i < 20; i++) {
            vehicle.advanceRoutePathIndex();
            assertTrue(vehicle.isMovingForward(),
                    "Circular route vehicle should always be moving forward");
        }
    }

    // ── Cargo ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("loadCargo should set cargo type and amount")
    void testLoadCargo() {
        vehicle.loadCargo(CargoType.PASSENGERS, 10);

        assertTrue(vehicle.isCarrying());
        assertEquals(CargoType.PASSENGERS, vehicle.getCargoType());
        assertEquals(10, vehicle.getCargoAmount());
    }

    @Test
    @DisplayName("loadCargo should cap amount at the vehicle's capacity")
    void testLoadCargoCapedAtCapacity() {
        int overCapacity = vehicle.getCapacity() + 999;
        vehicle.loadCargo(CargoType.PASSENGERS, overCapacity);

        assertEquals(vehicle.getCapacity(), vehicle.getCargoAmount(),
                "Cargo amount should not exceed vehicle capacity");
    }

    @Test
    @DisplayName("unloadCargo should return the amount that was carried")
    void testUnloadCargoReturnsAmount() {
        vehicle.loadCargo(CargoType.PASSENGERS, 15);
        int delivered = vehicle.unloadCargo();

        assertEquals(15, delivered,
                "unloadCargo should return the exact amount that was loaded");
    }

    @Test
    @DisplayName("unloadCargo should leave the vehicle empty")
    void testUnloadCargoLeavesVehicleEmpty() {
        vehicle.loadCargo(CargoType.PASSENGERS, 15);
        vehicle.unloadCargo();

        assertFalse(vehicle.isCarrying(), "Vehicle should be empty after unloading");
        assertNull(vehicle.getCargoType(), "Cargo type should be null after unloading");
        assertEquals(0, vehicle.getCargoAmount(), "Cargo amount should be 0 after unloading");
    }

    @Test
    @DisplayName("isCarrying should return false when cargo amount is 0")
    void testIsCarryingFalseWhenAmountIsZero() {
        vehicle.loadCargo(CargoType.PASSENGERS, 0);
        assertFalse(vehicle.isCarrying(),
                "Vehicle with 0 cargo should not be considered carrying");
    }

    @Test
    @DisplayName("Different vehicle types should carry different cargo")
    void testDifferentVehicleTypesCarryDifferentCargo() {
        Vehicle logTruck = new Vehicle("v-2", VehicleType.LOG_TRUCK, START);
        Vehicle oreTruck = new Vehicle("v-3", VehicleType.ORE_TRUCK, START);

        assertEquals(CargoType.WOOD, logTruck.getType().getAllowedCargo());
        assertEquals(CargoType.IRON, oreTruck.getType().getAllowedCargo());
    }

    // ── State setters ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("setActive should change the active state")
    void testSetActive() {
        vehicle.setActive(false);
        assertFalse(vehicle.isActive());

        vehicle.setActive(true);
        assertTrue(vehicle.isActive());
    }

    @Test
    @DisplayName("setLooping should change the looping state")
    void testSetLooping() {
        vehicle.setLooping(true);
        assertTrue(vehicle.isLooping());

        vehicle.setLooping(false);
        assertFalse(vehicle.isLooping());
    }

    @Test
    @DisplayName("setPosition should update the vehicle's grid position")
    void testSetPosition() {
        Position newPos = new Position(5, 10);
        vehicle.setPosition(newPos);
        assertEquals(newPos, vehicle.getPosition());
    }

    @Test
    @DisplayName("setSmoothPosition should update both smooth coordinates")
    void testSetSmoothPosition() {
        vehicle.setSmoothPosition(3.5, 7.2);
        assertEquals(3.5, vehicle.getSmoothX(), 0.001);
        assertEquals(7.2, vehicle.getSmoothY(), 0.001);
    }

    @Test
    @DisplayName("setTravelDirection should update the direction")
    void testSetTravelDirection() {
        vehicle.setTravelDirection(Direction.NORTH);
        assertEquals(Direction.NORTH, vehicle.getTravelDirection());

        vehicle.setTravelDirection(Direction.WEST);
        assertEquals(Direction.WEST, vehicle.getTravelDirection());
    }

    @Test
    @DisplayName("setRouteProgress should update the stored progress value")
    void testSetRouteProgress() {
        vehicle.setRouteProgress(0.75);
        assertEquals(0.75, vehicle.getRouteProgress(), 0.001);
    }

    // ── restoreRouteState ─────────────────────────────────────────────────────

    @Test
    @DisplayName("restoreRouteState should set the path index and direction")
    void testRestoreRouteState() {
        vehicle.assignRoute(buildLinearRoute(10));
        vehicle.restoreRouteState(5, false);

        assertEquals(5, vehicle.getRoutePathIndex(),
                "Path index should be restored");
        assertFalse(vehicle.isMovingForward(),
                "Moving direction should be restored");
    }

    @Test
    @DisplayName("restoreRouteState with negative index should clamp to 0")
    void testRestoreRouteStateNegativeIndexClamped() {
        vehicle.assignRoute(buildLinearRoute(5));
        vehicle.restoreRouteState(-3, true);

        assertEquals(0, vehicle.getRoutePathIndex(),
                "Negative index should be clamped to 0");
    }
}
