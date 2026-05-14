package model;

/**
 * A bridge spanning one or more consecutive {@link model.enums.TileType#WATER} tiles.
 *
 * <p>Bridges are either horizontal or vertical and are limited to a maximum consecutive
 * span determined by their {@link BridgeType}. The speed at which vehicles cross a bridge
 * is capped to the bridge's {@code maxSpeedMultiplier} regardless of the current game
 * speed setting.
 */
public class Bridge extends MapEntity {

    /**
     * Layout direction of the bridge on the map.
     * The footprint extends along the chosen axis.
     */
    public enum Orientation { HORIZONTAL, VERTICAL }

    /**
     * Material tier of the bridge, controlling construction cost, maximum span, and
     * the maximum game-speed multiplier allowed while crossing.
     */
    public enum BridgeType {
        /** Cheap wooden bridge: max 3-tile span, speed capped at 1× game speed. */
        WOODEN(100, 3, 1),
        /** Steel bridge: max 10-tile span, speed capped at 2× game speed. */
        STEEL(500, 10, 2),
        /** Suspension bridge: max 6-tile span, speed capped at 4× game speed. */
        SUSPENSION(1000, 6, 4);

        private final int cost;
        private final int maxSpan;
        private final int maxSpeedMultiplier;

        BridgeType(int cost, int maxSpan, int maxSpeedMultiplier) {
            this.cost = cost;
            this.maxSpan = maxSpan;
            this.maxSpeedMultiplier = maxSpeedMultiplier;
        }

        /** Returns the one-time construction cost of this bridge type. */
        public int getCost() {
            return cost;
        }

        /** Returns the maximum number of consecutive tiles this bridge type may span. */
        public int getMaxSpan() {
            return maxSpan;
        }

        /**
         * Returns the maximum game-speed multiplier at which vehicles may cross.
         * Vehicles travelling faster will have their effective delta-time scaled down.
         */
        public int getMaxSpeedMultiplier() {
            return maxSpeedMultiplier;
        }
    }

    /** Number of water tiles spanned by this bridge. */
    private final int length;
    /** Whether this bridge runs east-west or north-south. */
    private final Orientation orientation;
    /** Material tier that determines cost, span limit, and speed cap. */
    private final BridgeType bridgeType;

    /**
     * Creates a single-tile-wide bridge segment.
     *
     * @param id          unique entity identifier
     * @param name        display name
     * @param originX     column of the western-most (or northern-most) tile
     * @param originY     row of the western-most (or northern-most) tile
     * @param length      number of water tiles the bridge covers
     * @param orientation {@code HORIZONTAL} or {@code VERTICAL}
     * @param bridgeType  material tier
     */
    public Bridge(String id, String name, int originX, int originY,
                  int length, Orientation orientation, BridgeType bridgeType) {
        super(id, name, originX, originY,
                orientation == Orientation.HORIZONTAL ? length : 1,
                orientation == Orientation.VERTICAL ? length : 1);
        this.length      = length;
        this.orientation = orientation;
        this.bridgeType  = bridgeType;
    }

    /** Returns the number of tiles this bridge spans. */
    public int getLength()              { return length; }

    /** Returns whether this bridge runs horizontally or vertically. */
    public Orientation getOrientation() { return orientation; }

    /** Returns the material tier of this bridge. */
    public BridgeType getBridgeType()   { return bridgeType; }
}








