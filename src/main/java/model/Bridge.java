package model;



public class Bridge extends MapEntity {

    public enum Orientation { HORIZONTAL, VERTICAL }

    private final int length;           
    private final Orientation orientation;

    public Bridge(String id, String name, int originX, int originY,
                  int length, Orientation orientation) {
        super(id, name, originX, originY,
                orientation == Orientation.HORIZONTAL ? length : 1,
                orientation == Orientation.VERTICAL ? length : 1);
        this.length      = length;
        this.orientation = orientation;
    }

    public int getLength()             { return length; }
    public Orientation getOrientation(){ return orientation; }
}

