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
 * and advances city demand on a periodic timer.
 *
 * <p>
 * Loading/unloading priority order at each stop arrival:
 * <ol>
 * <li>Unload passengers at any adjacent city ({@code INCOME_PER_UNIT} per
 * passenger).</li>
 * <li>Unload cargo if the adjacent city currently demands it; advance the
 * city's demand sequence.</li>
 * <li>Unload cargo at an adjacent facility that consumes it (no income).</li>
 * <li>Load passengers onto buses from any adjacent city.</li>
 * <li>Load cargo from an adjacent facility that produces the vehicle's cargo
 * type.</li>
 * </ol>
 *
 * <p>
 * City demand advances automatically every {@code DEMAND_ADVANCE_INTERVAL}
 * game-seconds
 * (via {@link #tickDemand}) and also immediately on a successful delivery.
 */
public class DeliveryService {

    private static final double DEMAND_ADVANCE_INTERVAL = 120.0; // game-seconds

    // ── Called by SimulationEngine each tick ──────────────────────────────────

    /**
     * Advances the demand-rotation timer and rotates all cities' demand
     * when the interval elapses.
     *
     * @param game the world containing all cities
     * @param dt   elapsed simulation time in seconds this frame
     */
    public void tickDemand(Game game, GameState state, double dt) {
        double timer = state.getDemandTimer() + dt;
        if (timer >= DEMAND_ADVANCE_INTERVAL) {
            timer = 0;
            for (City city : game.getCities()) {
                if (!city.getDemandSequence().isEmpty())
                    city.advanceDemand();
            }
        }
        state.setDemandTimer(timer);
    }

    // ── Called when a vehicle reaches a Stop tile ─────────────────────────────

    /**
     * Processes cargo load/unload logic when {@code vehicle} arrives at
     * {@code stop}.
     * See class-level doc for the full priority order.
     *
     * @param vehicle the vehicle that just reached the stop
     * @param stop    the stop tile that was reached
     * @param state   full game state providing access to city/facility lists and
     *                the ledger
     */
    public void handleStopArrival(Vehicle vehicle, Stop stop, GameState state) {
        Game game = state.getMap();
        Position pos = stop.getPosition();

        // UNLOAD: passengers are always accepted by any city
        if (vehicle.isCarrying() && vehicle.getCargoType() == CargoType.PASSENGERS) {
            City city = findAdjacentCity(pos, game, stop.isInsideCity());
            if (city != null) {
                int delivered = vehicle.unloadCargo();
                state.getPlayer().getLedger().earn(
                        delivered * CargoType.PASSENGERS.getIncomePerUnit(),
                        TransactionType.DELIVERY,
                        "Transported passengers to " + city.getName());
                return;
            }
        }

        // UNLOAD: vehicle is carrying cargo that an adjacent city demands
        if (vehicle.isCarrying()) {
            City city = findAdjacentCity(pos, game, stop.isInsideCity());
            if (city != null && city.getCurrentDemand() == vehicle.getCargoType()) {
                int delivered = vehicle.unloadCargo();
                state.getPlayer().getLedger().earn(
                        delivered * city.getCurrentDemand().getIncomePerUnit(),
                        TransactionType.DELIVERY,
                        "Delivered " + city.getCurrentDemand().displayName() + " to " + city.getName());
                city.advanceDemand();
                if (!city.getDemandSequence().isEmpty() && !vehicle.isLooping()) {
                    vehicle.setActive(false);
                }
                return;
            }
            // Also unload any cargo at a facility that consumes it
            Facility facility = findAdjacentFacility(pos, game);
            if (facility != null && facility.getConsumes().contains(vehicle.getCargoType())) {
                vehicle.unloadCargo();
                return;
            }
        }

        // LOAD: buses pick up passengers from any adjacent city
        if (!vehicle.isCarrying() && vehicle.getType().isPassenger()) {
            City city = findAdjacentCity(pos, game, stop.isInsideCity());
            if (city != null) {
                vehicle.loadCargo(CargoType.PASSENGERS, vehicle.getCapacity());
            }
            return;
        }

        // LOAD: vehicle is empty, adjacent facility produces cargo this vehicle can
        // carry
        if (!vehicle.isCarrying()) {
            Facility facility = findAdjacentFacility(pos, game);
            if (facility != null && facility.getPrimaryProduction() != null) {
                CargoType prod = facility.getPrimaryProduction();
                if (vehicle.getType().getAllowedCargo() == prod) {
                    vehicle.loadCargo(prod, vehicle.getCapacity());
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private City findAdjacentCity(Position pos, Game game, boolean isInsideCity) {
        if (isInsideCity) {
            // Stop is inside a city — find which one contains this position
            for (City city : game.getCities()) {
                if (city.containsPosition(pos.getX(), pos.getY()))
                    return city;
            }
        }
        // Check the 4 orthogonal neighbors
        int[][] deltas = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : deltas) {
            int nx = pos.getX() + d[0];
            int ny = pos.getY() + d[1];
            for (City city : game.getCities()) {
                if (city.containsPosition(nx, ny))
                    return city;
            }
        }
        return null;
    }

    private Facility findAdjacentFacility(Position pos, Game game) {
        int[][] deltas = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, { 0, 0 } };
        for (int[] d : deltas) {
            int nx = pos.getX() + d[0];
            int ny = pos.getY() + d[1];
            for (Facility fac : game.getFacilities()) {
                if (fac.containsPosition(nx, ny))
                    return fac;
            }
        }
        return null;
    }

    // ── Helper: find a Stop at a given position ───────────────────────────────

    /**
     * Finds the stop located at {@code pos}, or {@code null} if none exists there.
     *
     * @param pos   position to search
     * @param stops list of all stops in the world
     * @return the matching stop, or {@code null}
     */
    public static Stop findStopAt(Position pos, List<Stop> stops) {
        for (Stop s : stops) {
            if (s.getPosition().equals(pos))
                return s;
        }
        return null;
    }
}
