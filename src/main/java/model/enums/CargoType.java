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
    /** Timber/logs — produced by wood factories, consumed by cities. */
    WOOD(20),
    /** Iron ore — produced by iron mines, consumed by cities. */
    IRON(40),
    /** Coal — produced by coal factories, consumed by cities. */
    COAL(30),
    /** Crude oil — produced by oil rigs, consumed by cities. */
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
