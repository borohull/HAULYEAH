package model.service;

import model.Game;
import model.Position;
import model.Route;
import model.Stop;
import model.Vehicle;
import model.enums.VehicleType;

/**
 * VehicleService — manages vehicle lifecycle (spawn and route assignment).
 */
public class VehicleService {

    private int vehicleCounter = 0;

    public Vehicle spawnVehicle(Game game, VehicleType type, Stop startStop) {
        return spawnAtPosition(game, type, startStop.getPosition());
    }

    /** Spawns a vehicle at an explicit position (used when route has no stops). */
    public Vehicle spawnAtPosition(Game game, VehicleType type, Position pos) {
        String id = "vehicle-" + (++vehicleCounter);
        Vehicle v = new Vehicle(id, type, pos);
        game.addVehicle(v);
        return v;
    }

    public void assignRoute(Game game, Vehicle vehicle, Route route) {
        vehicle.assignRoute(route);
    }
}
