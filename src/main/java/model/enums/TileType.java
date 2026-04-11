package model.enums;

/**
 * TileType — every possible state a map tile can have.
 * Moved from model.TileType → model.enums.TileType
 */
public enum TileType {
    EMPTY,
    CITY,
    CITY_EMPTY,
    FACILITY,
    ROAD,
    STOP,
    WATER,
    FOREST,
    BRIDGE,
    CITY_ROAD,   // Internal roads within cities — not modifiable by player
    CITY_STOP    // Stops inside cities at internal road intersections
}
