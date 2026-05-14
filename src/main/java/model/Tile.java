package model;

import model.enums.TileType;

/**
 * A single cell in the game-world grid.
 *
 * <p>Every tile carries a {@link TileType} that drives both rendering and construction
 * validation. Tiles that belong to a named entity (city building, road, facility, stop, …)
 * also store the entity's {@code id} and display {@code name}.
 *
 * <p>Forest tiles additionally maintain a {@code treeCount} (clamped 0–4). The
 * {@link model.service.SimulationEngine} increments this value over time to simulate
 * forest growth; setting it to 0 automatically converts the tile back to {@code EMPTY}.
 */
public class Tile {
    /** Current classification of this cell; drives rendering and build rules. */
    private TileType type;
    /** Immutable grid coordinate of this tile. */
    private final Position position;

    /** ID of the entity (road, stop, city, …) that occupies this tile, or {@code null}. */
    private String entityId;
    /** Display name of the occupying entity, or {@code null}. */
    private String entityName;

    /** Number of trees on this tile (0–4); only meaningful when {@code type == FOREST}. */
    private int treeCount;

    /**
     * Creates an empty tile at the given grid coordinates.
     *
     * @param x column index
     * @param y row index
     */
    public Tile(int x, int y) {
        this.position = new Position(x, y);
        this.type = TileType.EMPTY;
        this.treeCount = 0;
    }

    /** Returns this tile's current type. */
    public TileType getType()       { return type; }
    /** Returns the immutable grid position of this tile. */
    public Position getPosition()   { return position; }
    /** Returns the id of the entity occupying this tile, or {@code null} if unoccupied. */
    public String getEntityId()     { return entityId; }
    /** Returns the display name of the entity occupying this tile, or {@code null}. */
    public String getEntityName()   { return entityName; }
    /** Returns the number of trees on this tile (0–4). */
    public int getTreeCount()       { return treeCount; }

    /** Sets the tile type directly (used by construction and loading logic). */
    public void setType(TileType type)           { this.type = type; }
    /** Associates an entity id with this tile. */
    public void setEntityId(String entityId)     { this.entityId = entityId; }
    /** Associates a display name with this tile. */
    public void setEntityName(String entityName) { this.entityName = entityName; }

    /**
     * Sets the tree count, clamped to [0, 4]. Automatically transitions the tile
     * type between {@code EMPTY} and {@code FOREST}.
     *
     * @param treeCount desired tree count
     */
    public void setTreeCount(int treeCount) {
        this.treeCount = Math.max(0, Math.min(4, treeCount));
        if (this.treeCount > 0 && type == TileType.EMPTY) {
            type = TileType.FOREST;
        } else if (this.treeCount == 0 && type == TileType.FOREST) {
            type = TileType.EMPTY;
        }
    }

    /** Increments the tree count by one (capped at 4). */
    public void addTree() {
        setTreeCount(treeCount + 1);
    }

    /** Returns {@code true} if this tile has at least one tree. */
    public boolean hasTrees() {
        return treeCount > 0;
    }

    /**
     * Returns {@code true} if a player-built road or stop can be placed here
     * ({@code EMPTY} or {@code FOREST} tiles only).
     */
    public boolean isBuildable() {
        return type == TileType.EMPTY || type == TileType.FOREST;
    }

    @Override
    public String toString() {
        return "Tile[" + position + ", " + type + ", trees=" + treeCount + "]";
    }
}
