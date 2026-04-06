package model;

/**
 * Represents a player-built road tile on the map.
 *
 * Player-built roads:
 * - Can be placed on EMPTY or FOREST tiles
 * - Cannot be placed inside cities or facilities
 * - Are marked as TileType.ROAD in the world
 * - Can connect to city entrances to form transport routes
 *
 * Note: Internal city roads are handled separately via TileType.CITY_ROAD
 * and are generated during city creation. They use RoadType for rendering variation only.
 */
public class Road {

    /**
     * RoadType used for rendering variation of road graphics.
     * Does NOT affect game logic - it's purely visual.
     * HORIZONTAL: Rendered with horizontal road image
     * VERTICAL: Rendered with vertical road image
     */
    public enum RoadType { HORIZONTAL, VERTICAL }

    private final String   id;
    private final Position position;
    private final RoadType type;  // Used only for rendering, not game logic

    public Road(String id, int x, int y, RoadType type) {
        this.id       = id;
        this.position = new Position(x, y);
        this.type     = type;
    }

    public String   getId()       { return id; }
    public Position getPosition() { return position; }
    public RoadType getType()     { return type; }
}


