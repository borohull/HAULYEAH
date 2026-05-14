package model.enums;

/**
 * Categories of goods (and passengers) that vehicles can transport.
 *
 * <p>
 * Each {@link VehicleType} is restricted to exactly one {@code CargoType}.
 * Cities have
 * a demand sequence drawn from the non-passenger types. Passenger vehicles may
 * unload at any
 * city stop; cargo vehicles must deliver to a city that currently demands that
 * cargo type,
 * or to a facility that consumes it.
 */
public enum CargoType {
    /** Human passengers — carried by buses, deliverable at any city. */
    PASSENGERS(10),
    /**
     * Timber/logs — produced by Wood Factories; consumed by Coal Mines and cities.
     */
    WOOD(20),
    /**
     * Coal — produced by Coal Mines from wood; consumed by Iron Mines and cities.
     */
    COAL(30),
    /**
     * Iron ore — produced by Iron Mines from coal; consumed by Oil Rigs and cities.
     */
    IRON(40),
    /** Crude oil — produced by Oil Rigs from iron; consumed by cities. */
    OIL(50);

    private final double incomePerUnit;

    CargoType(double incomePerUnit) {
        this.incomePerUnit = incomePerUnit;
    }

    public double getIncomePerUnit() {
        return incomePerUnit;
    }

    /**
     * Returns a human-readable title-case name (e.g. {@code "Passengers"}).
     */
    public String displayName() {
        String n = name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }

    /**
     * Returns a short abbreviation used on minimap demand badges
     * (e.g. {@code "Pax"}, {@code "Fe"}, {@code "Co"}).
     */
    public String abbreviation() {
        return switch (this) {
            case PASSENGERS -> "Pax";
            case WOOD -> "W";
            case IRON -> "Fe";
            case COAL -> "Co";
            case OIL -> "Oil";
        };
    }
}
