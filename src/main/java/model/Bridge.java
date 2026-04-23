package model;



public class Bridge extends MapEntity {

    public enum Orientation { HORIZONTAL, VERTICAL }

    public enum BridgeType {
        WOODEN(100),
        STEEL(500),
        SUSPENSION(1000);

        private final int cost;

        BridgeType(int cost) {
            this.cost = cost;
        }

        public int getCost() {
            return cost;
        }
    }

    private final int length;
    private final Orientation orientation;
    private final BridgeType bridgeType;

    public Bridge(String id, String name, int originX, int originY,
                  int length, Orientation orientation, BridgeType bridgeType) {
        super(id, name, originX, originY,
                orientation == Orientation.HORIZONTAL ? length : 1,
                orientation == Orientation.VERTICAL ? length : 1);
        this.length      = length;
        this.orientation = orientation;
        this.bridgeType  = bridgeType;
    }

    public int getLength()             { return length; }
    public Orientation getOrientation(){ return orientation; }
    public BridgeType getBridgeType() {
        return bridgeType;
    }
}








