package model;



public class Bridge extends MapEntity {

    public enum Orientation { HORIZONTAL, VERTICAL }

    public enum BridgeType {
        WOODEN(100, 3),
        STEEL(500, 10),
        SUSPENSION(1000, 6);

        private final int cost;
        private final int maxSpan;

        BridgeType(int cost, int maxSpan) {
            this.cost = cost;
            this.maxSpan = maxSpan;
        }

        public int getCost() {
            return cost;
        }

        public int getMaxSpan() {
            return maxSpan;
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








