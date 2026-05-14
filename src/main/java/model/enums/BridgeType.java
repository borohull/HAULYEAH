package model.enums;

/**
 * Material tier of a bridge, used as a top-level enum alias for {@link model.Bridge.BridgeType}.
 *
 * <p>This enum is referenced by the UML diagram and can be used in contexts where the
 * nested {@code Bridge.BridgeType} is inconvenient. The values mirror those of the inner enum
 * exactly: each tier differs in construction cost, maximum span, and crossing speed cap.
 */
public enum BridgeType {
    /** Cheap wooden bridge — max 3-tile span, crossing speed capped at 1× game speed. */
    WOODEN,
    /** Steel bridge — max 10-tile span, crossing speed capped at 2× game speed. */
    STEEL,
    /** Suspension bridge — max 6-tile span, crossing speed capped at 4× game speed. */
    SUSPENSION
}