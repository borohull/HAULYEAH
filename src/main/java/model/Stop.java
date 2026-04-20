package model;

/**
 * Represents a transport stop (station) on the map.
 *
 * Stops:
 * - Occupy exactly 1 tile
 * - Must be adjacent to a ROAD, CITY_ROAD, or FACILITY tile
 * - Can be either external (TileType.STOP) or internal to a city (TileType.CITY_STOP)
 * - Are endpoints in vehicle routes
 * - Store and manage cargo/passengers
 */
public class Stop {

    private final String   id;
    private final Position position;
    private final String   name;
    private boolean        isInsideCity;  // True if this stop is inside a city (on city roads)
    private model.enums.Direction orientation; // The direction vehicles face at this stop

    public Stop(String id, int x, int y, String name) {
        this.id       = id;
        this.position = new Position(x, y);
        this.name     = name;
        this.isInsideCity = false;
        this.orientation  = model.enums.Direction.EAST; // Default orientation
    }

    public String   getId()            { return id; }
    public Position getPosition()      { return position; }
    public String   getName()          { return name; }
    public boolean  isInsideCity()     { return isInsideCity; }
    public model.enums.Direction getOrientation() { return orientation; }

    public void setInsideCity(boolean inside) {
        this.isInsideCity = inside;
    }

    /** Cycles the stop's orientation clockwise. */
    public void cycleOrientation() {
        this.orientation = switch (this.orientation) {
            case EAST  -> model.enums.Direction.SOUTH;
            case SOUTH -> model.enums.Direction.WEST;
            case WEST  -> model.enums.Direction.NORTH;
            case NORTH -> model.enums.Direction.EAST;
        };
    }
}
