package model;

import java.util.List;
public class Facility extends MapEntity {

    private final List<String> produces;
    private final List<String> consumes;

    public Facility(String id, String name, int originX, int originY, int width, int height,
                    List<String> produces, List<String> consumes) {
        super(id, name, originX, originY, width, height);
        this.produces = produces;
        this.consumes = consumes;
    }


    public List<String> getProduces()  { return produces; }
    public List<String> getConsumes()  { return consumes; }
}


