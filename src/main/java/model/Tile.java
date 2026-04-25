package model;

import model.enums.TileType;

public class Tile {
    private TileType type;
    private final Position position;

    private String entityId;
    private String entityName;

    private int treeCount;

    public Tile(int x, int y) {
        this.position = new Position(x, y);
        this.type = TileType.EMPTY;
        this.treeCount = 0;
    }

    public TileType getType()       { return type; }
    public Position getPosition()   { return position; }
    public String getEntityId()     { return entityId; }
    public String getEntityName()   { return entityName; }
    public int getTreeCount()       { return treeCount; }

    public void setType(TileType type)           { this.type = type; }
    public void setEntityId(String entityId)     { this.entityId = entityId; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public void setTreeCount(int treeCount) {
        this.treeCount = Math.max(0, Math.min(4, treeCount));
        if (this.treeCount > 0 && type == TileType.EMPTY) {
            type = TileType.FOREST;
        } else if (this.treeCount == 0 && type == TileType.FOREST) {
            type = TileType.EMPTY;
        }
    }

    public void addTree() {
        setTreeCount(treeCount + 1);
    }

    public boolean hasTrees() {
        return treeCount > 0;
    }

    public boolean isBuildable() {
        return type == TileType.EMPTY || type == TileType.FOREST;
    }

    @Override
    public String toString() {
        return "Tile[" + position + ", " + type + ", trees=" + treeCount + "]";
    }
}
