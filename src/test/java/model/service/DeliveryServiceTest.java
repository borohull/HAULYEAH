package model.service;

import model.*;
import model.enums.CargoType;
import model.enums.TransactionType;
import model.enums.VehicleType;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DeliveryService — verifies that vehicles correctly
 * load cargo from facilities, deliver to cities, earn money,
 * and that city demand advances properly.
 *
 * Each test builds only what it needs: a Game, a Player, stops,
 * cities/facilities placed adjacent to those stops, and a vehicle.
 */
@DisplayName("DeliveryService Tests")
class DeliveryServiceTest {

    private DeliveryService deliveryService;
    private Game game;
    private Player player;
    private GameState state;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService();
        game = new Game(20, 20);
        player = new Player("TestPlayer", 100000);
        state = new GameState(game, player);
    }

    // Helpers

    /**
     * Places a city whose top-left corner is at (originX, originY).
     * Also sets all of its tiles to CITY in the game grid.
     */
    private City placeCity(String id, String name, int originX, int originY) {
        City city = new City(id, name, originX, originY, 3, 3);
        game.addCity(city);
        // Set demand so the city actually wants something
        city.setDemandSequence(List.of(CargoType.WOOD, CargoType.IRON));
        return city;
    }

    /**
     * Places a facility that produces the given cargo at (originX, originY).
     */
    private Facility placeFacility(String id, String name, int originX, int originY,
            CargoType produces) {
        Facility fac = new Facility(id, name, originX, originY, 2, 2,
                List.of(produces), List.of());
        game.addFacility(fac);
        return fac;
    }

    /**
     * Creates a stop at (x, y) and registers it with the game.
     */
    private Stop makeStop(int x, int y, String name) {
        Stop stop = new Stop("stop-" + name, x, y, name);
        game.getStops().add(stop);
        return stop;
    }

    /**
     * Creates a vehicle of the given type at (x, y) and registers it.
     */
    private Vehicle makeVehicle(VehicleType type, int x, int y) {
        Vehicle v = new Vehicle("v-1", type, new Position(x, y));
        game.addVehicle(v);
        return v;
    }

    // Loading cargo from a facility

    @Test
    @DisplayName("Empty vehicle should load cargo from an adjacent facility")
    void testVehicleLoadsFromFacility() {
        // Facility at (5,5)-(6,6), stop at (5,7) — directly south of facility
        placeFacility("fac-1", "Wood Factory", 5, 5, CargoType.WOOD);
        Stop stop = makeStop(5, 7, "Pickup Stop");
        Vehicle vehicle = makeVehicle(VehicleType.LOG_TRUCK, 5, 7);

        assertFalse(vehicle.isCarrying(), "Vehicle should start empty");

        deliveryService.handleStopArrival(vehicle, stop, state);

        assertTrue(vehicle.isCarrying(), "Vehicle should have loaded cargo");
        assertEquals(CargoType.WOOD, vehicle.getCargoType(),
                "Vehicle should be carrying WOOD");
        assertEquals(vehicle.getCapacity(), vehicle.getCargoAmount(),
                "Vehicle should be fully loaded");
    }

    @Test
    @DisplayName("Vehicle should not load cargo it cannot carry")
    void testVehicleDoesNotLoadWrongCargo() {
        // Facility produces IRON, but vehicle only carries WOOD
        placeFacility("fac-1", "Iron Mine", 5, 5, CargoType.IRON);
        Stop stop = makeStop(5, 7, "Pickup Stop");
        Vehicle vehicle = makeVehicle(VehicleType.LOG_TRUCK, 5, 7); // carries WOOD only

        deliveryService.handleStopArrival(vehicle, stop, state);

        assertFalse(vehicle.isCarrying(),
                "Vehicle should NOT load cargo it cannot carry");
    }

    @Test
    @DisplayName("Vehicle already carrying cargo should not load more")
    void testFullVehicleDoesNotLoadMore() {
        placeFacility("fac-1", "Wood Factory", 5, 5, CargoType.WOOD);
        Stop stop = makeStop(5, 7, "Pickup Stop");
        Vehicle vehicle = makeVehicle(VehicleType.LOG_TRUCK, 5, 7);

        // Pre-load the vehicle
        vehicle.loadCargo(CargoType.WOOD, 10);
        int originalAmount = vehicle.getCargoAmount();

        deliveryService.handleStopArrival(vehicle, stop, state);

        assertEquals(originalAmount, vehicle.getCargoAmount(),
                "Already-loaded vehicle should not load additional cargo");
    }

    // Delivering cargo to a city

    @Test
    @DisplayName("Vehicle should deliver cargo to a city that demands it")
    void testVehicleDeliversToCity() {
        // City at (5,5)-(7,7), stop at (5,8) — directly south of city
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.WOOD));
        Stop stop = makeStop(5, 8, "City Stop");
        Vehicle vehicle = makeVehicle(VehicleType.LOG_TRUCK, 5, 8);
        vehicle.loadCargo(CargoType.WOOD, 50);

        double capitalBefore = player.getLedger().getCurrentCapital();

        deliveryService.handleStopArrival(vehicle, stop, state);

        assertFalse(vehicle.isCarrying(), "Vehicle should be empty after delivery");
        assertTrue(player.getLedger().getCurrentCapital() > capitalBefore,
                "Player should have earned money from the delivery");
    }

    @Test
    @DisplayName("Vehicle should NOT deliver cargo a city does not currently demand")
    void testVehicleDoesNotDeliverWrongCargo() {
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.IRON)); // wants IRON, not WOOD
        Stop stop = makeStop(5, 8, "City Stop");
        Vehicle vehicle = makeVehicle(VehicleType.LOG_TRUCK, 5, 8);
        vehicle.loadCargo(CargoType.WOOD, 50);

        double capitalBefore = player.getLedger().getCurrentCapital();

        deliveryService.handleStopArrival(vehicle, stop, state);

        assertTrue(vehicle.isCarrying(),
                "Vehicle should still be carrying cargo after failed delivery attempt");
        assertEquals(capitalBefore, player.getLedger().getCurrentCapital(), 0.01,
                "Player should NOT earn money for delivering wrong cargo");
    }

    @Test
    @DisplayName("Delivery should earn money proportional to amount delivered")
    void testDeliveryEarnsCorrectAmount() {
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.WOOD));
        Stop stop = makeStop(5, 8, "City Stop");
        Vehicle vehicle = makeVehicle(VehicleType.LOG_TRUCK, 5, 8);

        int amount = 10;
        vehicle.loadCargo(CargoType.WOOD, amount);
        double capitalBefore = player.getLedger().getCurrentCapital();

        deliveryService.handleStopArrival(vehicle, stop, state);

        double earned = player.getLedger().getCurrentCapital() - capitalBefore;
        // DeliveryService pays 150 per unit
        assertEquals(amount * 150.0, earned, 0.01,
                "Should earn 150 per unit delivered");
    }

    // City demand progression

    @Test
    @DisplayName("City demand should advance after a successful delivery")
    void testDemandAdvancesAfterDelivery() {
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.WOOD, CargoType.IRON));
        assertEquals(CargoType.WOOD, city.getCurrentDemand(),
                "City should start demanding WOOD");

        Stop stop = makeStop(5, 8, "City Stop");
        Vehicle vehicle = makeVehicle(VehicleType.LOG_TRUCK, 5, 8);
        vehicle.loadCargo(CargoType.WOOD, 10);

        deliveryService.handleStopArrival(vehicle, stop, state);

        assertEquals(CargoType.IRON, city.getCurrentDemand(),
                "City demand should advance to IRON after WOOD is delivered");
    }

    @Test
    @DisplayName("City demand should wrap around after the last item in the sequence")
    void testDemandWrapsAround() {
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.WOOD)); // only one item
        Stop stop = makeStop(5, 8, "City Stop");

        // Deliver once
        Vehicle v1 = makeVehicle(VehicleType.LOG_TRUCK, 5, 8);
        v1.loadCargo(CargoType.WOOD, 10);
        deliveryService.handleStopArrival(v1, stop, state);

        // Demand should wrap back to WOOD
        assertEquals(CargoType.WOOD, city.getCurrentDemand(),
                "Demand should wrap back to the start of the sequence");
    }

    // Passenger buses

    @Test
    @DisplayName("Bus should load passengers from a city")
    void testBusLoadsPassengers() {
        placeCity("city-1", "Testville", 5, 5);
        Stop stop = makeStop(5, 8, "City Stop");
        Vehicle bus = makeVehicle(VehicleType.CITY_BUS, 5, 8);

        assertFalse(bus.isCarrying(), "Bus should start empty");

        deliveryService.handleStopArrival(bus, stop, state);

        assertTrue(bus.isCarrying(), "Bus should have loaded passengers");
        assertEquals(CargoType.PASSENGERS, bus.getCargoType(),
                "Bus should be carrying PASSENGERS");
    }

    @Test
    @DisplayName("Bus should unload passengers at any city and earn money")
    void testBusUnloadsPassengersAtCity() {
        placeCity("city-1", "Testville", 5, 5);
        Stop stop = makeStop(5, 8, "City Stop");
        Vehicle bus = makeVehicle(VehicleType.CITY_BUS, 5, 8);
        bus.loadCargo(CargoType.PASSENGERS, 20);

        double capitalBefore = player.getLedger().getCurrentCapital();

        deliveryService.handleStopArrival(bus, stop, state);

        assertFalse(bus.isCarrying(), "Bus should be empty after unloading");
        assertTrue(player.getLedger().getCurrentCapital() > capitalBefore,
                "Player should earn money for transporting passengers");
    }

    // Demand timer

    @Test
    @DisplayName("tickDemand should NOT advance demand before the interval elapses")
    void testTickDemandDoesNotAdvanceEarly() {
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.WOOD, CargoType.IRON));
        int indexBefore = city.getDemandIndex();

        // Tick for only 10 seconds — interval is 120 seconds
        deliveryService.tickDemand(game, 10.0);

        assertEquals(indexBefore, city.getDemandIndex(),
                "Demand should not advance after only 10 seconds");
    }

    @Test
    @DisplayName("tickDemand should advance demand after the full interval elapses")
    void testTickDemandAdvancesAfterInterval() {
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.WOOD, CargoType.IRON));
        assertEquals(CargoType.WOOD, city.getCurrentDemand());

        // Tick past the 120-second interval
        deliveryService.tickDemand(game, 121.0);

        assertEquals(CargoType.IRON, city.getCurrentDemand(),
                "Demand should advance after 120 seconds");
    }

    @Test
    @DisplayName("tickDemand should accumulate time across multiple calls")
    void testTickDemandAccumulatesTime() {
        City city = placeCity("city-1", "Testville", 5, 5);
        city.setDemandSequence(List.of(CargoType.WOOD, CargoType.IRON));

        // Three ticks of 50 seconds each = 150 total, which exceeds 120
        deliveryService.tickDemand(game, 50.0);
        deliveryService.tickDemand(game, 50.0);
        deliveryService.tickDemand(game, 50.0);

        assertEquals(CargoType.IRON, city.getCurrentDemand(),
                "Demand should advance after accumulated time exceeds 120 seconds");
    }
}