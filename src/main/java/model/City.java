package model;

import model.enums.CargoType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A city on the map — the primary destination for cargo deliveries and passenger transport.
 *
 * <p>Each city has a demand sequence: an ordered list of {@link CargoType}s it wants. The
 * current demand advances whenever a delivery is completed or the global demand timer fires.
 * The city footprint is divided into building tiles, empty tiles, and internal road tiles
 * generated from a {@link CityTemplate}. Entrances are the border cells through which
 * player-built roads may connect.
 */
public class City extends MapEntity {

    /**
     * Visual variant of an internal city road tile, used for rendering only.
     */
    public enum CityRoadType {
        /** Road runs east-west. */
        HORIZONTAL,
        /** Road runs north-south. */
        VERTICAL,
        /** Intersection of horizontal and vertical roads. */
        CROSSROAD
    }


    /** Rotating list of cargo types this city demands; cycles after each successful delivery. */
    private List<CargoType> demandSequence = new ArrayList<>();
    /** Index into {@code demandSequence} pointing to the cargo currently demanded. */
    private int demandIndex = 0;

    private final List<Position> buildingTiles;
    private final List<Position> emptyTiles;
    /** All internal road tiles (horizontal + vertical + crossroad). */
    private final List<Position> roadTiles;
    /** Border cells where player roads may connect to the city. */
    private final Set<Position> entrances;

    private final List<Position> horizontalRoadTiles;
    private final List<Position> verticalRoadTiles;
    private final List<Position> crossroadTiles;

    /**
     * Creates a city with the given footprint; tile categorisation is populated
     * incrementally via the {@code add*} methods called from {@link MapGenerator}.
     *
     * @param id      unique entity identifier
     * @param name    display name (city name, e.g. "Budapest")
     * @param originX top-left column of the bounding rectangle
     * @param originY top-left row of the bounding rectangle
     * @param width   width in tiles
     * @param height  height in tiles
     */
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

    /** Registers a tile as a building (occupied city block). */
    public void addBuildingTile(int x, int y) {
        Position p = new Position(x, y);
        tiles.add(p);
        buildingTiles.add(p);
    }

    /** Registers an internal road tile with the default {@code HORIZONTAL} variant. */
    public void addRoadTile(int x, int y) {
        addRoadTile(x, y, CityRoadType.HORIZONTAL);
    }

    /** Registers an internal road tile with an explicit road type (for rendering). */
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

    /** Marks a border cell as an entrance point where external roads may connect. */
    public void addEntrance(int x, int y) {
        entrances.add(new Position(x, y));
    }

    /** Registers a tile as an unbuilt (empty) city cell. */
    public void addEmptyTile(int x, int y) {
        Position p = new Position(x, y);
        tiles.add(p);
        emptyTiles.add(p);
    }


    /** Returns all building tiles within this city. */
    public List<Position> getBuildingTiles() { return buildingTiles; }
    /** Returns all empty (undeveloped) tiles within this city. */
    public List<Position> getEmptyTiles() { return emptyTiles; }
    /** Returns all internal road tiles within this city. */
    public List<Position> getRoadTiles() { return roadTiles; }
    /** Returns a snapshot of the set of entrance positions on this city's border. */
    public Set<Position> getEntrances() { return new HashSet<>(entrances); }

    /** Returns internal road tiles rendered as east-west roads. */
    public List<Position> getHorizontalRoadTiles() { return horizontalRoadTiles; }
    /** Returns internal road tiles rendered as north-south roads. */
    public List<Position> getVerticalRoadTiles() { return verticalRoadTiles; }
    /** Returns internal road tiles at intersections. */
    public List<Position> getCrossroadTiles() { return crossroadTiles; }

    /** Returns {@code true} if {@code p} is an east-west internal road tile. */
    public boolean isHorizontalRoad(Position p) {
        return horizontalRoadTiles.contains(p);
    }

    /** Returns {@code true} if {@code p} is a north-south internal road tile. */
    public boolean isVerticalRoad(Position p) {
        return verticalRoadTiles.contains(p);
    }

    /** Returns {@code true} if {@code p} is an intersection tile. */
    public boolean isCrossroad(Position p) {
        return crossroadTiles.contains(p);
    }

    /** Returns {@code true} if {@code p} is a border entrance cell. */
    public boolean isEntrance(Position p) {
        return entrances.contains(p);
    }

    /** Returns {@code true} if the tile at (x, y) is within this city's bounding rectangle. */
    public boolean containsPosition(int x, int y) {
        return x >= origin.getX() && x < origin.getX() + width &&
                y >= origin.getY() && y < origin.getY() + height;
    }

    /** Replaces the demand sequence (used by {@link MapGenerator} after world generation). */
    public void setDemandSequence(List<CargoType> seq) { this.demandSequence = new ArrayList<>(seq); }
    /** Returns the full ordered demand sequence for this city. */
    public List<CargoType> getDemandSequence()         { return demandSequence; }
    /** Returns the index into the demand sequence that is currently active. */
    public int  getDemandIndex()                       { return demandIndex; }
    /**
     * Sets the demand index (wraps around the sequence length).
     *
     * @param i the desired index (may be out of bounds — will be wrapped)
     */
    public void setDemandIndex(int i)                  { this.demandIndex = Math.floorMod(i, Math.max(1, demandSequence.size())); }

    /**
     * Returns the cargo type currently demanded by this city, or {@code null} if no
     * demand sequence has been assigned.
     */
    public CargoType getCurrentDemand() {
        return demandSequence.isEmpty() ? null : demandSequence.get(demandIndex);
    }

    /** Advances the demand to the next entry in the sequence (wraps around). */
    public void advanceDemand() {
        if (!demandSequence.isEmpty()) {
            demandIndex = (demandIndex + 1) % demandSequence.size();
        }
    }
}