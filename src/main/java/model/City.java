package model;

import java.util.ArrayList;
import java.util.List;

public class City {
    private final String id;
    private final String name;
    private final Position origin;   // top-left tile of the city's bounding box
    private final int width;         // in tiles (min 3)
    private final int height;        // in tiles (min 3)
    private final List<Position> tiles; // all tiles this city occupies

    public City(String id, String name, int originX, int originY, int width, int height) {
        this.id = id;
        this.name = name;
        this.origin = new Position(originX, originY);
        this.width = width;
        this.height = height;
        this.tiles = new ArrayList<>();
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < width; dx++) {
                tiles.add(new Position(originX + dx, originY + dy));
            }
        }
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public Position getOrigin()       { return origin; }
    public int getWidth()             { return width; }
    public int getHeight()            { return height; }
    public List<Position> getTiles()  { return tiles; }

    /** Returns the center tile position (for label rendering). */
    public Position getCenter() {
        return new Position(origin.getX() + width / 2, origin.getY() + height / 2);
    }
}
