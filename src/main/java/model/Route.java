package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Route — a named circular path the player draws on the map.
 *
 * tilePath : the exact ordered list of tile positions the vehicle follows
 * (player draws this tile by tile on the map).
 * stops : subset of tilePath positions that are Stop tiles
 * (auto-detected when the path is built).
 */
public class Route {

    private final String id;
    private String name; // no longer final — supports renaming
    private final List<Stop> stops;
    private final List<Position> tilePath;
    private boolean reversed;
    private boolean circular;

    /** Full constructor — used when player draws the route on the map. */
    public Route(String id, String name, List<Stop> stops, List<Position> tilePath) {
        this.id = id;
        this.name = name;
        this.stops = new ArrayList<>(stops);
        this.tilePath = new ArrayList<>(tilePath);
        this.reversed = false;
        this.circular = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Stop> getStops() {
        return List.copyOf(stops);
    }

    public List<Position> getTilePath() {
        return List.copyOf(tilePath);
    }

    public boolean hasStops() {
        return !stops.isEmpty();
    }

    public boolean hasTilePath() {
        return !tilePath.isEmpty();
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean rev) {
        this.reversed = rev;
    }

    public boolean isCircular() {
        return circular;
    }

    public void setCircular(boolean circ) {
        this.circular = circ;
    }

    /**
     * Renames this route.
     * Null or blank input is ignored — the name stays unchanged.
     *
     * @param name the new display name
     */
    public void setName(String name) {
        if (name == null || name.isBlank())
            return;
        this.name = name.trim();
    }
}
