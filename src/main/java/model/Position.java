package model;

import java.util.Objects;

/**
 * Immutable 2D grid coordinate used throughout the simulation.
 *
 * <p>Tiles, stops, vehicles, and all map entities are located by (x, y) pairs.
 * {@code x} increases eastward (column index), {@code y} increases southward (row index).
 * Implements value equality so instances can be used as {@link java.util.Map} keys or in
 * {@link java.util.Set}s.
 */
public class Position {
    /** Column index (0-based, increases eastward). */
    private final int x;
    /** Row index (0-based, increases southward). */
    private final int y;

    /**
     * Creates a new position.
     *
     * @param x column index
     * @param y row index
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Returns the column (x) coordinate. */
    public int getX() { return x; }

    /** Returns the row (y) coordinate. */
    public int getY() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
