package model.enums;

/** VehicleType — types of vehicles available for purchase. */
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

    public CargoType getAllowedCargo()    { return allowedCargo; }
    public String    getCategory()       { return allowedCargo.displayName(); }
    public int       getSpeed()          { return speed; }
    public int       getCapacity()       { return capacity; }
    public int       getMaintenanceCost(){ return maintenanceCost; }
    public int       getPurchasePrice()  { return purchasePrice; }
    public boolean   isPassenger()       { return allowedCargo == CargoType.PASSENGERS; }
}
