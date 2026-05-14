package model.enums;

/**
 * Every possible visual/functional state a {@link model.Tile} can have.
 *
 * <p>The type drives both rendering (which sprite or polygon to draw) and construction
 * validation (what the player is allowed to build or place on the tile).
 */
public enum TileType {
    /** Undeveloped grass — roads and stops can be built here. */
    EMPTY,
    /** A city building tile — not buildable by the player. */
    CITY,
    /** An undeveloped tile inside a city bounding rectangle. */
    CITY_EMPTY,
    /** A production facility tile — not buildable by the player. */
    FACILITY,
    /** A player-built road. */
    ROAD,
    /** A player-built transport stop adjacent to a road or city. */
    STOP,
    /** A water tile — only bridges may be built here. */
    WATER,
    /** A tile with trees; road building requires a clearing surcharge. */
    FOREST,
    /** A bridge tile spanning water. */
    BRIDGE,
    /** An internal city road tile generated from a {@link model.CityTemplate} — not modifiable. */
    CITY_ROAD,
    /** A stop placed on a city internal road. */
    CITY_STOP
}
