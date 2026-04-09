package model.service;

import model.GameState;

/**
 * SimulationEngine — advances the game simulation by dt seconds each frame.
 */
public class SimulationEngine {

    private final VehicleService vehicleService = new VehicleService();

    public void tick(GameState state, double dt) {
        // Movement not implemented yet
    }

    public VehicleService getVehicleService() { return vehicleService; }
}
