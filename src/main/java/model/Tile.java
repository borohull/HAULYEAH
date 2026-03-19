package model;

public class Tile {
    private TileType type;
    private final Position position;

    
    private String entityId;   
    private String entityName; 

    public Tile(int x, int y) {
        this.position = new Position(x, y);
        this.type = TileType.EMPTY;
    }

    
    public TileType getType()       { return type; }
    public Position getPosition()   { return position; }
    public String getEntityId()     { return entityId; }
    public String getEntityName()   { return entityName; }

    
    public void setType(TileType type)           { this.type = type; }
    public void setEntityId(String entityId)     { this.entityId = entityId; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    
    public boolean isBuildable() {
        return type == TileType.EMPTY;
    }

    @Override
    public String toString() {
        return "Tile[" + position + ", " + type + "]";
    }
}
