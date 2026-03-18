package model;

import java.util.ArrayList;
import java.util.List;

public class Facility {
    private final String id;
    private final String name;
    private final Position origin;   // top-left tile
    private final int width;         // in tiles (min 2)
    private final int height;        // in tiles (min 2)
    private final List<Position> tiles;

    // What this facility produces and consumes (cargo type names)
    private final List<String> produces;
    private final List<String> consumes;

    public Facility(String id, String name, int originX, int originY, int width, int height,
                    List<String> produces, List<String> consumes) {
        this.id = id;
        this.name = name;
        this.origin = new Position(originX, originY);
        this.width = width;
        this.height = height;
        this.produces = produces;
        this.consumes = consumes;
        this.tiles = new ArrayList<>();
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < width; dx++) {
                tiles.add(new Position(originX + dx, originY + dy));
            }
        }
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public Position getOrigin()        { return origin; }
    public int getWidth()              { return width; }
    public int getHeight()             { return height; }
    public List<Position> getTiles()   { return tiles; }
    public List<String> getProduces()  { return produces; }
    public List<String> getConsumes()  { return consumes; }

    public Position getCenter() {
        return new Position(origin.getX() + width / 2, origin.getY() + height / 2);
    }
}
