package model;

public class Tile {
    private TileType type;
    private final Position position;

    // For CITY / FACILITY tiles: which entity occupies this tile
    private String entityId;   // e.g. "city_0", "facility_2"
    private String entityName; // human-readable label

    public Tile(int x, int y) {
        this.position = new Position(x, y);
        this.type = TileType.EMPTY;
    }

    // --- Getters ---
    public TileType getType()       { return type; }
    public Position getPosition()   { return position; }
    public String getEntityId()     { return entityId; }
    public String getEntityName()   { return entityName; }

    // --- Setters ---
    public void setType(TileType type)           { this.type = type; }
    public void setEntityId(String entityId)     { this.entityId = entityId; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    /** Convenience: is this tile buildable (can a road go here)? */
    public boolean isBuildable() {
        return type == TileType.EMPTY;
    }

    @Override
    public String toString() {
        return "Tile[" + position + ", " + type + "]";
    }
}
