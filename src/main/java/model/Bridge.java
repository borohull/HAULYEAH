package model;

import java.util.ArrayList;
import java.util.List;


public class Bridge {

    public enum Orientation { HORIZONTAL, VERTICAL }

    private final String id;
    private final String name;
    private final Position origin;      
    private final int length;           
    private final Orientation orientation;
    private final List<Position> tiles;

    public Bridge(String id, String name, int originX, int originY,
                  int length, Orientation orientation) {
        this.id          = id;
        this.name        = name;
        this.origin      = new Position(originX, originY);
        this.length      = length;
        this.orientation = orientation;
        this.tiles       = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            int tx = originX + (orientation == Orientation.HORIZONTAL ? i : 0);
            int ty = originY + (orientation == Orientation.VERTICAL   ? i : 0);
            tiles.add(new Position(tx, ty));
        }
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public Position getOrigin()        { return origin; }
    public int getLength()             { return length; }
    public Orientation getOrientation(){ return orientation; }
    public List<Position> getTiles()   { return tiles; }

    public Position getCenter() {
        int half = length / 2;
        return orientation == Orientation.HORIZONTAL
                ? new Position(origin.getX() + half, origin.getY())
                : new Position(origin.getX(), origin.getY() + half);
    }
}

