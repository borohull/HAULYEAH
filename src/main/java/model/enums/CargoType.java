package model.enums;

/** CargoType — categories of goods that vehicles can transport. */
public enum CargoType {
    PASSENGERS,
    WOOD,
    IRON,
    STEEL,
    PAPER,
    COAL,
    OIL,
    PLASTIC,
    TEXTILE,
    COTTON;

    public String displayName() {
        String n = name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }

    public String abbreviation() {
        return switch (this) {
            case PASSENGERS -> "Pax";
            case WOOD       -> "W";
            case IRON       -> "Fe";
            case STEEL      -> "St";
            case PAPER      -> "Pa";
            case COAL       -> "Co";
            case OIL        -> "Oil";
            case PLASTIC    -> "Pl";
            case TEXTILE    -> "Tx";
            case COTTON     -> "Ct";
        };
    }
}
