package model;

public enum VehicleType {
    CITY_BUS("Passenger", 40, 30, 80),
    LOG_TRUCK("Raw material", 35, 40, 110),
    FOOD_TRUCK("Product", 45, 35, 95),
    GOODS_TRUCK("Product", 38, 45, 125);

    private final String category;
    private final int speed;
    private final int capacity;
    private final int maintenanceCost;

    VehicleType(String category, int speed, int capacity, int maintenanceCost) {
        this.category = category;
        this.speed = speed;
        this.capacity = capacity;
        this.maintenanceCost = maintenanceCost;
    }

    public String getCategory() {
        return category;
    }

    public int getSpeed() {
        return speed;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getMaintenanceCost() {
        return maintenanceCost;
    }
}
