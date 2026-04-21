package model.enums;

/**
 * VehicleType — types of vehicles available for purchase.
 * Moved from model.VehicleType → model.enums.VehicleType
 */
public enum VehicleType {
    CITY_BUS     ("Passenger",    40, 30,  80, 2500),
    EXPRESS_BUS  ("Passenger",    60, 20, 130, 4000),
    LOG_TRUCK    ("Raw material", 35, 40, 110, 3000),
    FLATBED_TRUCK("Raw material", 25, 60, 100, 3500),
    FOOD_TRUCK   ("Product",      45, 35,  95, 2800),
    GOODS_TRUCK  ("Product",      38, 45, 125, 3200);

    private final String category;
    private final int    speed;
    private final int    capacity;
    private final int    maintenanceCost;
    private final int    purchasePrice;

    VehicleType(String category, int speed, int capacity, int maintenanceCost, int purchasePrice) {
        this.category        = category;
        this.speed           = speed;
        this.capacity        = capacity;
        this.maintenanceCost = maintenanceCost;
        this.purchasePrice   = purchasePrice;
    }

    public String  getCategory()       { return category; }
    public int     getSpeed()          { return speed; }
    public int     getCapacity()       { return capacity; }
    public int     getMaintenanceCost(){ return maintenanceCost; }
    public int     getPurchasePrice()  { return purchasePrice; }
    public boolean isPassenger()       { return category.equals("Passenger"); }
}
