package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class City {
    private final String id;
    private final String name;
    private final Position origin;
    private final int width;
    private final int height;

    private final List<Position> tiles;
    private final List<Position> buildingTiles;
    private final List<Position> roadTiles;
    private final Set<Position> entrances;  // Edge road tiles where external roads can connect

    public City(String id, String name, int originX, int originY, int width, int height) {
        this.id = id;
        this.name = name;
        this.origin = new Position(originX, originY);
        this.width = width;
        this.height = height;

        this.tiles = new ArrayList<>();
        this.buildingTiles = new ArrayList<>();
        this.roadTiles = new ArrayList<>();
        this.entrances = new HashSet<>();
    }

    public void addBuildingTile(int x, int y) {
        Position p = new Position(x, y);
        tiles.add(p);
        buildingTiles.add(p);
    }

    public void addRoadTile(int x, int y) {
        Position p = new Position(x, y);
        tiles.add(p);
        roadTiles.add(p);
    }

    /**
     * Marks entrance tiles - road tiles on the city edges where external roads can connect.
     * These are typically the border road tiles.
     */
    public void addEntrance(int x, int y) {
        entrances.add(new Position(x, y));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Position getOrigin() { return origin; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public List<Position> getTiles() { return tiles; }
    public List<Position> getBuildingTiles() { return buildingTiles; }
    public List<Position> getRoadTiles() { return roadTiles; }
    public Set<Position> getEntrances() { return new HashSet<>(entrances); }

    public Position getCenter() {
        return new Position(origin.getX() + width / 2, origin.getY() + height / 2);
    }

    /**
     * Check if a position is an entrance tile of this city.
     */
    public boolean isEntrance(Position p) {
        return entrances.contains(p);
    }

    /**
     * Check if a position is inside this city's bounds.
     */
    public boolean containsPosition(int x, int y) {
        return x >= origin.getX() && x < origin.getX() + width &&
               y >= origin.getY() && y < origin.getY() + height;
    }
}