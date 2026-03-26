package model;
import java.util.List;

public class Route {

    private final String id;
    private final String name;
    private final List<Stop> stops;

    public Route(String id, String name, List<Stop> stops) {
        this.id = id;
        this.name = name;
        this.stops = stops;
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

    public boolean hasStops() {
        return !stops.isEmpty();
    }
}
