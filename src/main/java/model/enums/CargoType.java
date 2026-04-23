package model.enums;

/** CargoType — categories of goods that vehicles can transport. */
public enum CargoType {
    PASSENGERS,
    WOOD,
    IRON,
    COAL,
    OIL;

    public String displayName() {
        String n = name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }

    public String abbreviation() {
        return switch (this) {
            case PASSENGERS -> "Pax";
            case WOOD       -> "W";
            case IRON       -> "Fe";
            case COAL       -> "Co";
            case OIL        -> "Oil";
        };
    }
}
