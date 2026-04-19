package model;

import java.util.List;

public class WaterBody extends MapEntity {

    public WaterBody(String id, String name, int originX, int originY, int width, int height) {
        super(id, name, originX, originY, width, height);
    }

    public WaterBody(String id, String name, int originX, int originY, int width, int height, List<Position> customTiles) {
        super(id, name, originX, originY, width, height);
        this.tiles.clear();
        this.tiles.addAll(customTiles);
    }
}
