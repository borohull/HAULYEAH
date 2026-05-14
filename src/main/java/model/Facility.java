package model;

import model.enums.CargoType;
import java.util.List;

/**
 * An industrial facility (mine, factory, oil rig, etc.) on the map.
 *
 * <p>Facilities occupy a rectangular footprint and declare which {@link CargoType}s they
 * produce and consume. Vehicles with a matching cargo category can load from an adjacent
 * stop, and cities or other facilities can accept deliveries.
 */
public class Facility extends MapEntity {

    /** Cargo types this facility produces and makes available for pickup. */
    private final List<CargoType> produces;
    /** Cargo types this facility accepts as deliveries. */
    private final List<CargoType> consumes;

    /**
     * Creates a facility with explicit production and consumption lists.
     *
     * @param id       unique entity identifier
     * @param name     display name (e.g. "Iron Mine")
     * @param originX  top-left column of the footprint
     * @param originY  top-left row of the footprint
     * @param width    width in tiles
     * @param height   height in tiles
     * @param produces cargo types produced by this facility
     * @param consumes cargo types consumed (accepted) by this facility
     */
    public Facility(String id, String name, int originX, int originY, int width, int height,
                    List<CargoType> produces, List<CargoType> consumes) {
        super(id, name, originX, originY, width, height);
        this.produces = produces;
        this.consumes = consumes;
    }

    /** Returns the list of cargo types this facility produces. */
    public List<CargoType> getProduces() { return produces; }

    /** Returns the list of cargo types this facility accepts as deliveries. */
    public List<CargoType> getConsumes() { return consumes; }

    /**
     * Returns the first entry from the {@code produces} list, or {@code null} if empty.
     * Convenience accessor used by {@link model.service.DeliveryService} when loading cargo.
     */
    public CargoType getPrimaryProduction() {
        return produces.isEmpty() ? null : produces.get(0);
    }
}


