package model;

import java.util.ArrayList;
import java.util.List;

public class City {
    private final String id;
    private final String name;
    private final Position origin;
    private final int width;
    private final int height;

    private final List<Position> tiles;
    private final List<Position> buildingTiles;
    private final List<Position> roadTiles;

    public City(String id, String name, int originX, int originY, int width, int height) {
        this.id = id;
        this.name = name;
        this.origin = new Position(originX, originY);
        this.width = width;
        this.height = height;

        this.tiles = new ArrayList<>();
        this.buildingTiles = new ArrayList<>();
        this.roadTiles = new ArrayList<>();
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

    public String getId() { return id; }
    public String getName() { return name; }
    public Position getOrigin() { return origin; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public List<Position> getTiles() { return tiles; }
    public List<Position> getBuildingTiles() { return buildingTiles; }
    public List<Position> getRoadTiles() { return roadTiles; }

    public Position getCenter() {
        return new Position(origin.getX() + width / 2, origin.getY() + height / 2);
    }
}