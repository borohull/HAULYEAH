package model;

/**
 * A rectangular forest area on the map.
 *
 * <p>Forests are placed procedurally by {@link MapGenerator} and can spread at runtime
 * via the {@link model.service.SimulationEngine} forest-growth mechanic. Players may
 * build roads through forest tiles (at an extra clearing cost). Individual tiles track
 * a tree density in {@link Tile#getTreeCount()}.
 */
public class Forest extends MapEntity {

    /**
     * Creates a forest with a rectangular footprint.
     *
     * @param id      unique entity identifier
     * @param name    display name (e.g. "Dark Wood")
     * @param originX top-left column of the bounding rectangle
     * @param originY top-left row of the bounding rectangle
     * @param width   width in tiles
     * @param height  height in tiles
     */
    public Forest(String id, String name, int originX, int originY, int width, int height) {
        super(id, name, originX, originY, width, height);
    }
}



