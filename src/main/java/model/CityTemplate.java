package model;

import java.util.Random;

/**
 * Defines a fixed city layout template with building patterns.
 * Can be reused and randomly placed on the map.
 */
public class CityTemplate {
    private final String name;
    private final int width;
    private final int height;
    private final double buildingPercentage;
    private final boolean[][] layout;  // true = building, false = empty

    public CityTemplate(String name, int width, int height, double buildingPercentage) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.buildingPercentage = buildingPercentage;
        this.layout = new boolean[width][height];
        generateLayout();
    }

    private void generateLayout() {
        Random rng = new Random();

        // Fill border with buildings for enclosure
        for (int x = 0; x < width; x++) {
            layout[x][0] = true;
            layout[x][height - 1] = true;
        }
        for (int y = 0; y < height; y++) {
            layout[0][y] = true;
            layout[width - 1][y] = true;
        }

        // Fill interior with buildings based on percentage
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                layout[x][y] = rng.nextDouble() < buildingPercentage;
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean[][] getLayout() {
        return layout;
    }

    public boolean hasBuilding(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return layout[x][y];
    }
}

