package model.enums;

/**
 * Cardinal compass direction.
 *
 * <p>Used by {@link model.Vehicle} (travel direction), {@link model.TrafficLight}
 * (managed approach directions), and {@link model.Stop} (orientation).
 * In the isometric grid: {@code NORTH} decreases y, {@code SOUTH} increases y,
 * {@code EAST} increases x, {@code WEST} decreases x.
 */
public enum Direction {
    /** Decreasing y (upward in grid coordinates). */
    NORTH,
    /** Increasing y (downward in grid coordinates). */
    SOUTH,
    /** Increasing x (rightward in grid coordinates). */
    EAST,
    /** Decreasing x (leftward in grid coordinates). */
    WEST
}
