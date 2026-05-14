package model.service;

import model.Game;
import model.Position;
import model.Route;
import model.Stop;
import model.Vehicle;
import model.enums.VehicleType;

/**
 * Manages vehicle lifecycle: spawning new vehicles and assigning routes.
 *
 * <p>Vehicles are created with an auto-incremented ID ({@code "vehicle-N"}) and
 * immediately registered with the {@link Game}. Route assignment delegates to
 * {@link Vehicle#assignRoute(Route)}, which reactivates parked vehicles.
 */
public class VehicleService {

    private int vehicleCounter = 0;

    /**
     * Spawns a new vehicle at the position of the given stop.
     *
     * @param game      the world the vehicle is added to
     * @param type      vehicle type determining speed, capacity, and cargo
     * @param startStop stop where the vehicle will initially appear
     * @return the newly created vehicle
     */
    public Vehicle spawnVehicle(Game game, VehicleType type, Stop startStop) {
        return spawnAtPosition(game, type, startStop.getPosition());
    }

    /**
     * Spawns a vehicle at an explicit position (used when a route has no stops).
     *
     * @param game the world the vehicle is added to
     * @param type vehicle type
     * @param pos  initial grid position
     * @return the newly created vehicle
     */
    public Vehicle spawnAtPosition(Game game, VehicleType type, Position pos) {
        String id = "vehicle-" + (++vehicleCounter);
        Vehicle v = new Vehicle(id, type, pos);
        game.addVehicle(v);
        return v;
    }

    /**
     * Assigns a route to a vehicle, resetting its traversal to the start.
     *
     * @param game    unused but kept for API symmetry with other service methods
     * @param vehicle the vehicle to configure
     * @param route   the route to assign
     */
    public void assignRoute(Game game, Vehicle vehicle, Route route) {
        vehicle.assignRoute(route);
    }
}
