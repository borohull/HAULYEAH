package model.enums;

/**
 * Possible signal states of a {@link model.TrafficLight} for a given approach direction.
 *
 * <p>{@code GREEN} means vehicles approaching from that direction may proceed.
 * {@code RED} means they must wait until the phase advances.
 */
public enum LightState {
    /** Vehicles from this direction must stop and wait. */
    RED,
    /** Vehicles from this direction may proceed. */
    GREEN
}
