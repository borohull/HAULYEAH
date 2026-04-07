package model;

import java.util.ArrayList;
import java.util.List;

/**
 * MapEntity
 * A shared abstract superclass that encapsulates data common to all area-based objects 
 * on the map (such as Cities, Forests, Facilities, WaterBodies, and Bridges).
 */
public abstract class MapEntity {
    protected final String id;
    protected final String name;
    protected final Position origin;
    protected final int width;
    protected final int height;
    protected final List<Position> tiles;

    public MapEntity(String id, String name, int originX, int originY, int width, int height) {
        this.id = id;
        this.name = name;
        this.origin = new Position(originX, originY);
        this.width = width;
        this.height = height;
        this.tiles = new ArrayList<>();
        
        // Populates the rectangular footprint automatically
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < width; dx++) {
                tiles.add(new Position(originX + dx, originY + dy));
            }
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Position getOrigin() { return origin; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Position> getTiles() { return tiles; }

    public Position getCenter() {
        return new Position(origin.getX() + width / 2, origin.getY() + height / 2);
    }
}
