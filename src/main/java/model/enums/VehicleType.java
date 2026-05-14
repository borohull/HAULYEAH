package model.enums;

/**
 * All purchasable vehicle types with their statically-defined stats.
 *
 * <p>Each vehicle type is bound to exactly one {@link CargoType} and has fixed values for
 * speed (game units/sec before SPEED_SCALE division), cargo capacity, per-game-minute
 * maintenance cost, and one-off purchase price.
 *
 * <p>The two passenger types ({@code CITY_BUS}, {@code EXPRESS_BUS}) can deliver to any
 * city; all other types deliver the matching raw material cargo type.
 */
public enum VehicleType {
    // Passengers
    CITY_BUS      (CargoType.PASSENGERS, 40, 30,  80, 2500),
    EXPRESS_BUS   (CargoType.PASSENGERS, 60, 20, 130, 4000),
    // Wood
    LOG_TRUCK     (CargoType.WOOD,       35, 50,  90, 3000),
    TIMBER_LORRY  (CargoType.WOOD,       50, 30, 110, 3800),
    // Iron
    ORE_TRUCK     (CargoType.IRON,       30, 60, 100, 3500),
    IRON_HAULER   (CargoType.IRON,       45, 40, 120, 4200),
    // Coal
    COAL_CART     (CargoType.COAL,       25, 70,  85, 2800),
    COAL_TRUCK    (CargoType.COAL,       40, 45, 105, 3600),
    // Oil
    OIL_TANKER    (CargoType.OIL,        28, 65, 110, 4000),
    FUEL_TRUCK    (CargoType.OIL,        48, 35, 130, 4500);

    private final CargoType allowedCargo;
    private final int       speed;
    private final int       capacity;
    private final int       maintenanceCost;
    private final int       purchasePrice;

    VehicleType(CargoType allowedCargo, int speed, int capacity, int maintenanceCost, int purchasePrice) {
        this.allowedCargo    = allowedCargo;
        this.speed           = speed;
        this.capacity        = capacity;
        this.maintenanceCost = maintenanceCost;
        this.purchasePrice   = purchasePrice;
    }

    /** Returns the cargo type this vehicle is allowed to carry. */
    public CargoType getAllowedCargo()    { return allowedCargo; }
    /** Returns the display-name category string (e.g. "Wood", "Iron"). */
    public String    getCategory()       { return allowedCargo.displayName(); }
    /** Returns the movement speed in game units per second (before SPEED_SCALE division). */
    public int       getSpeed()          { return speed; }
    /** Returns the maximum cargo load this vehicle can carry per trip. */
    public int       getCapacity()       { return capacity; }
    /** Returns the per-game-minute maintenance drain from the player's capital. */
    public int       getMaintenanceCost(){ return maintenanceCost; }
    /** Returns the one-time purchase price deducted from the player's capital. */
    public int       getPurchasePrice()  { return purchasePrice; }
    /** Returns {@code true} if this vehicle type transports passengers. */
    public boolean   isPassenger()       { return allowedCargo == CargoType.PASSENGERS; }
}
