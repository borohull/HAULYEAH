package model.enums;

/**
 * TimeSpeed — simulation speed levels.
 * Moved from model.GameSpeed → model.enums.TimeSpeed (matches UML).
 *
 * PAUSED   → no time advances
 * X1       → 1× real-time
 * X2       → 2× real-time
 * X4       → 4× real-time
 */
public enum TimeSpeed {
    PAUSED(0,  "⏸"),
    X1(1,      "▶  1×"),
    X2(2,      "▶▶ 2×"),
    X4(4,      "▶▶▶ 4×");

    private final int    multiplier;
    private final String label;

    TimeSpeed(int multiplier, String label) {
        this.multiplier = multiplier;
        this.label      = label;
    }

    /** Returns the integer multiplier applied to the simulation delta-time (0 when paused). */
    public int    getMultiplier() { return multiplier; }
    /** Returns the human-readable speed label shown in the HUD (e.g. "▶▶ 2×"). */
    public String getLabel()      { return label; }

    /** Cycles: PAUSED → X1 → X2 → X4 → PAUSED */
    public TimeSpeed next() {
        return switch (this) {
            case PAUSED -> X1;
            case X1     -> X2;
            case X2     -> X4;
            case X4     -> PAUSED;
        };
    }
}
