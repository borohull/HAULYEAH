package model;


public class Road {

    public enum RoadType { HORIZONTAL, VERTICAL }

    private final String   id;
    private final Position position;
    private final RoadType type;

    public Road(String id, int x, int y, RoadType type) {
        this.id       = id;
        this.position = new Position(x, y);
        this.type     = type;
    }

    public String   getId()       { return id; }
    public Position getPosition() { return position; }
    public RoadType getType()     { return type; }
}
