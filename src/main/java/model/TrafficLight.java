package model;

import model.enums.Direction;
import model.enums.LightState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TrafficLight {

    private static final double DEFAULT_GREEN_SECONDS = 10.0;

    private final String   id;
    private final Position position;


    private final List<Direction> activeDirections = new ArrayList<>();
    private final Map<Direction, Double> greenDurations = new LinkedHashMap<>();


    private int currentPhaseIndex = 0;

    private double phaseTimer = DEFAULT_GREEN_SECONDS;

    public TrafficLight(String id, Position position, List<Direction> activeDirections) {
        this.id       = id;
        this.position = position;
        this.activeDirections.addAll(activeDirections);
        for (Direction d : activeDirections) {
            greenDurations.put(d, DEFAULT_GREEN_SECONDS);
        }
        if (!activeDirections.isEmpty()) {
            phaseTimer = greenDurations.get(activeDirections.get(0));
        }
    }


    public void tick(double dt) {
        if (activeDirections.isEmpty()) return;
        phaseTimer -= dt;
        if (phaseTimer <= 0) {
            currentPhaseIndex = (currentPhaseIndex + 1) % activeDirections.size();
            Direction next = activeDirections.get(currentPhaseIndex);
            phaseTimer = greenDurations.getOrDefault(next, DEFAULT_GREEN_SECONDS);
        }
    }

    /** Returns GREEN if d is the current phase direction, RED otherwise.
     *  If d is not a registered direction, returns GREEN (don't block unmanaged approaches). */
    public LightState getStateFor(Direction d) {
        if (activeDirections.isEmpty() || !activeDirections.contains(d)) return LightState.GREEN;
        return activeDirections.get(currentPhaseIndex) == d ? LightState.GREEN : LightState.RED;
    }

    public Direction getCurrentGreenDirection() {
        if (activeDirections.isEmpty()) return null;
        return activeDirections.get(currentPhaseIndex);
    }

    /** Immediately jump to the next green phase (manual override by player). */
    public void cycleToNextPhase() {
        if (activeDirections.isEmpty()) return;
        currentPhaseIndex = (currentPhaseIndex + 1) % activeDirections.size();
        Direction next = activeDirections.get(currentPhaseIndex);
        phaseTimer = greenDurations.getOrDefault(next, DEFAULT_GREEN_SECONDS);
    }

    /** Set green duration for a direction (minimum 1 second). */
    public void setGreenDuration(Direction d, double seconds) {
        if (activeDirections.contains(d)) {
            greenDurations.put(d, Math.max(1.0, seconds));
        }
    }

    public double getGreenDuration(Direction d) {
        return greenDurations.getOrDefault(d, DEFAULT_GREEN_SECONDS);
    }

    public String          getId()               { return id; }
    public Position        getPosition()         { return position; }
    public List<Direction> getActiveDirections() { return activeDirections; }
    public double          getPhaseTimer()       { return phaseTimer; }

    public int getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    public void restoreState(int phaseIndex, double phaseTimer) {
        if (activeDirections.isEmpty()) {
            currentPhaseIndex = 0;
            this.phaseTimer = DEFAULT_GREEN_SECONDS;
            return;
        }

        currentPhaseIndex = Math.floorMod(phaseIndex, activeDirections.size());
        Direction current = activeDirections.get(currentPhaseIndex);
        double maxDuration = greenDurations.getOrDefault(current, DEFAULT_GREEN_SECONDS);
        this.phaseTimer = Math.max(0.0, Math.min(phaseTimer, maxDuration));
    }

}
