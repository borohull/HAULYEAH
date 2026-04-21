package model;

import model.enums.CargoType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class City extends MapEntity {
    public enum CityRoadType {
        HORIZONTAL,
        VERTICAL,
        CROSSROAD
    }


    private List<CargoType> demandSequence = new ArrayList<>();
    private int demandIndex = 0;

    private final List<Position> buildingTiles;
    private final List<Position> emptyTiles;
    private final List<Position> roadTiles;
    private final Set<Position> entrances;

    private final List<Position> horizontalRoadTiles;
    private final List<Position> verticalRoadTiles;
    private final List<Position> crossroadTiles;

    public City(String id, String name, int originX, int originY, int width, int height) {
        super(id, name, originX, originY, width, height);

        this.buildingTiles = new ArrayList<>();
        this.emptyTiles = new ArrayList<>();
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

    public void addEmptyTile(int x, int y) {
        Position p = new Position(x, y);
        tiles.add(p);
        emptyTiles.add(p);
    }


    public List<Position> getBuildingTiles() { return buildingTiles; }
    public List<Position> getEmptyTiles() { return emptyTiles; }
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


    public boolean isEntrance(Position p) {
        return entrances.contains(p);
    }

    public boolean containsPosition(int x, int y) {
        return x >= origin.getX() && x < origin.getX() + width &&
                y >= origin.getY() && y < origin.getY() + height;
    }

    public void setDemandSequence(List<CargoType> seq) { this.demandSequence = new ArrayList<>(seq); }
    public List<CargoType> getDemandSequence()          { return demandSequence; }
    public int  getDemandIndex()                        { return demandIndex; }
    public void setDemandIndex(int i)                   { this.demandIndex = Math.floorMod(i, Math.max(1, demandSequence.size())); }

    public CargoType getCurrentDemand() {
        return demandSequence.isEmpty() ? null : demandSequence.get(demandIndex);
    }

    public void advanceDemand() {
        if (!demandSequence.isEmpty()) {
            demandIndex = (demandIndex + 1) % demandSequence.size();
        }
    }
}