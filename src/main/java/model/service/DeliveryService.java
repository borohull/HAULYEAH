package model.service;

import model.City;
import model.Facility;
import model.Game;
import model.GameState;
import model.Position;
import model.Stop;
import model.Vehicle;
import model.enums.CargoType;
import model.enums.TransactionType;

import java.util.List;

/**
 * Handles cargo loading/unloading when vehicles arrive at stops,
 * and advances city demand on a timer.
 */
public class DeliveryService {

    private static final double INCOME_PER_UNIT       = 150.0;
    private static final double DEMAND_ADVANCE_INTERVAL = 120.0; // game-seconds

    private double demandTimer = 0;

    // ── Called by SimulationEngine each tick ──────────────────────────────────

    public void tickDemand(Game game, double dt) {
        demandTimer += dt;
        if (demandTimer >= DEMAND_ADVANCE_INTERVAL) {
            demandTimer = 0;
            for (City city : game.getCities()) {
                if (!city.getDemandSequence().isEmpty()) city.advanceDemand();
            }
        }
    }

    // ── Called when a vehicle reaches a Stop tile ─────────────────────────────

    public void handleStopArrival(Vehicle vehicle, Stop stop, GameState state) {
        Game game = state.getMap();
        Position pos = stop.getPosition();

        // UNLOAD: vehicle is carrying cargo that an adjacent city demands
        if (vehicle.isCarrying()) {
            City city = findAdjacentCity(pos, game, stop.isInsideCity());
            if (city != null && city.getCurrentDemand() == vehicle.getCargoType()) {
                int delivered = vehicle.unloadCargo();
                state.getPlayer().getLedger().earn(
                    delivered * INCOME_PER_UNIT,
                    TransactionType.DELIVERY,
                    "Delivered " + city.getCurrentDemand().displayName() + " to " + city.getName()
                );
                city.advanceDemand();
                return;
            }
            // Also unload any cargo at a facility that consumes it
            Facility facility = findAdjacentFacility(pos, game);
            if (facility != null && facility.getConsumes().contains(vehicle.getCargoType())) {
                vehicle.unloadCargo();
                return;
            }
        }

        // LOAD: vehicle is empty, adjacent facility has production
        if (!vehicle.isCarrying()) {
            Facility facility = findAdjacentFacility(pos, game);
            if (facility != null && facility.getPrimaryProduction() != null) {
                vehicle.loadCargo(facility.getPrimaryProduction(), vehicle.getCapacity());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private City findAdjacentCity(Position pos, Game game, boolean isInsideCity) {
        if (isInsideCity) {
            // Stop is inside a city — find which one contains this position
            for (City city : game.getCities()) {
                if (city.containsPosition(pos.getX(), pos.getY())) return city;
            }
        }
        // Check the 4 orthogonal neighbors
        int[][] deltas = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : deltas) {
            int nx = pos.getX() + d[0];
            int ny = pos.getY() + d[1];
            for (City city : game.getCities()) {
                if (city.containsPosition(nx, ny)) return city;
            }
        }
        return null;
    }

    private Facility findAdjacentFacility(Position pos, Game game) {
        int[][] deltas = {{1,0},{-1,0},{0,1},{0,-1},{0,0}};
        for (int[] d : deltas) {
            int nx = pos.getX() + d[0];
            int ny = pos.getY() + d[1];
            for (Facility fac : game.getFacilities()) {
                if (fac.containsPosition(nx, ny)) return fac;
            }
        }
        return null;
    }

    // ── Helper: find a Stop at a given position ───────────────────────────────

    public static Stop findStopAt(Position pos, List<Stop> stops) {
        for (Stop s : stops) {
            if (s.getPosition().equals(pos)) return s;
        }
        return null;
    }
}
