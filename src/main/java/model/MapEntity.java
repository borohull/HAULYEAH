package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all area-based map objects.
 *
 * <p>Provides the common identity ({@code id}, {@code name}), bounding-rectangle geometry
 * ({@code origin}, {@code width}, {@code height}), and tile footprint list shared by
 * {@link City}, {@link Facility}, {@link WaterBody}, {@link Forest}, and {@link Bridge}.
 *
 * <p>The constructor automatically fills {@link #tiles} with every position inside the
 * rectangular footprint. Subclasses may replace or augment this list (e.g. {@link WaterBody}
 * supports custom irregular tile sets).
 */
public abstract class MapEntity {
    /** Unique identifier used for linking tiles back to their entity. */
    protected final String id;
    /** Human-readable display name shown on the map and in the HUD. */
    protected final String name;
    /** Top-left (north-west) corner of the bounding rectangle. */
    protected final Position origin;
    /** Width of the bounding rectangle in tiles. */
    protected final int width;
    /** Height of the bounding rectangle in tiles. */
    protected final int height;
    /** All tile positions that belong to this entity (may be non-rectangular for water bodies). */
    protected final List<Position> tiles;

    /**
     * Creates a map entity with a rectangular footprint. All positions within the rectangle
     * are added to {@link #tiles} automatically.
     *
     * @param id      unique identifier
     * @param name    display name
     * @param originX top-left column
     * @param originY top-left row
     * @param width   footprint width in tiles
     * @param height  footprint height in tiles
     */
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

    public boolean containsPosition(int x, int y) {
        return x >= origin.getX() && x < origin.getX() + width &&
                y >= origin.getY() && y < origin.getY() + height;
    }
}
