package model.service;

import model.Game;
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
        String id = "vehicle-" + (++vehicleCounter);
        Vehicle v = new Vehicle(id, type, startStop.getPosition());
        game.addVehicle(v);
        return v;
    }

    public void assignRoute(Game game, Vehicle vehicle, Route route) {
        vehicle.assignRoute(route);
    }
}
