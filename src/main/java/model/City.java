package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class City {
    public enum CityRoadType {
        HORIZONTAL,
        VERTICAL,
        CROSSROAD
    }

    private final String id;
    private final String name;
    private final Position origin;
    private final int width;
    private final int height;

    private final List<Position> tiles;
    private final List<Position> buildingTiles;
    private final List<Position> roadTiles;
    private final Set<Position> entrances;

    private final List<Position> horizontalRoadTiles;
    private final List<Position> verticalRoadTiles;
    private final List<Position> crossroadTiles;

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

        this.horizontalRoadTiles = new ArrayList<>();
        this.verticalRoadTiles = new ArrayList<>();
        this.crossroadTiles = new ArrayList<>();
    }

    public void addBuildingTile(int x, int y) {
        Position p = new Position(x, y);
        tiles.add(p);
        buildingTiles.add(p);
    }

    public void addRoadTile(int x, int y) {
        addRoadTile(x, y, CityRoadType.HORIZONTAL);
    }

    public void addRoadTile(int x, int y, CityRoadType type) {
        Position p = new Position(x, y);
        tiles.add(p);
        roadTiles.add(p);

        switch (type) {
            case HORIZONTAL -> horizontalRoadTiles.add(p);
            case VERTICAL -> verticalRoadTiles.add(p);
            case CROSSROAD -> crossroadTiles.add(p);
        }
    }

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

    public List<Position> getHorizontalRoadTiles() { return horizontalRoadTiles; }
    public List<Position> getVerticalRoadTiles() { return verticalRoadTiles; }
    public List<Position> getCrossroadTiles() { return crossroadTiles; }

    public boolean isHorizontalRoad(Position p) {
        return horizontalRoadTiles.contains(p);
    }

    public boolean isVerticalRoad(Position p) {
        return verticalRoadTiles.contains(p);
    }

    public boolean isCrossroad(Position p) {
        return crossroadTiles.contains(p);
    }

    public Position getCenter() {
        return new Position(origin.getX() + width / 2, origin.getY() + height / 2);
    }

    public boolean isEntrance(Position p) {
        return entrances.contains(p);
    }

    public boolean containsPosition(int x, int y) {
        return x >= origin.getX() && x < origin.getX() + width &&
                y >= origin.getY() && y < origin.getY() + height;
    }
}