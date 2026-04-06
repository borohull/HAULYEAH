package model;

public enum TileType {
    EMPTY,
    CITY,
    FACILITY,
    ROAD,
    STOP,
    WATER,
    FOREST,
    BRIDGE,
    CITY_ROAD,  // Internal roads within cities - not modifiable by player
    CITY_STOP   // Stops inside cities at internal road intersections
}
