package model;

import model.enums.CargoType;
import java.util.List;

public class Facility extends MapEntity {

    private final List<CargoType> produces;
    private final List<CargoType> consumes;

    public Facility(String id, String name, int originX, int originY, int width, int height,
                    List<CargoType> produces, List<CargoType> consumes) {
        super(id, name, originX, originY, width, height);
        this.produces = produces;
        this.consumes = consumes;
    }

    public List<CargoType> getProduces() { return produces; }
    public List<CargoType> getConsumes() { return consumes; }

    public CargoType getPrimaryProduction() {
        return produces.isEmpty() ? null : produces.get(0);
    }
}


