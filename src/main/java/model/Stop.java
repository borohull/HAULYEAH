package model;

public class Stop {

    private final String   id;
    private final Position position;
    private final String   name;

    public Stop(String id, int x, int y, String name) {
        this.id       = id;
        this.position = new Position(x, y);
        this.name     = name;
    }

    public String   getId()       { return id; }
    public Position getPosition() { return position; }
    public String   getName()     { return name; }
}
