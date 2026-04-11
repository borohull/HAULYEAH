package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.service.ConstructionService;

public class MapGenerator {
    private static final int FAC_W = 2;
    private static final int FAC_H = 2;
    private static final int PADDING = 2;

    private static final String[][] FACILITY_DATA = {
            {"Iron Mine", "IRON", ""},
            {"Lumber Mill", "WOOD", ""},
            {"Steel Works", "STEEL", "IRON"},
            {"Paper Mill", "PAPER", "WOOD"},
            {"Farm", "WOOD", ""}
    };

    private static final String[] WATER_NAMES = {
            "River Danube", "Blue Lake", "Crystal Creek", "Silent Pond"
    };

    private static final String[] FOREST_NAMES = {
            "Dark Wood", "Pine Forest", "Birch Grove", "Oak Thicket"
    };

    private final Random rng;
    private List<CityTemplate> cityTemplates;

    public MapGenerator() {
        this.rng = new Random();
        this.cityTemplates = new ArrayList<>();
        initializeCityTemplates();
    }

    public MapGenerator(long seed) {
        this.rng = new Random(seed);
        this.cityTemplates = new ArrayList<>();
        initializeCityTemplates();
    }

    private void initializeCityTemplates() {
        cityTemplates.add(new CityTemplate(
                "Debrecen",
                "B.H.B",
                ".B.B.",
                "H.X.H",
                ".B.B.",
                "B.H.B"
        ));

        cityTemplates.add(new CityTemplate(
                "Budapest",
                "B.B.H.B.B.H.B.B",
                ".B.B.B.B.B.B.B.",
                "B.B.H.B.B.H.B.B",
                ".B.B.B.B.B.B.B.",
                "B.B.H.B.B.H.B.B",
                ".B.B.B.B.B.B.B.",
                "B.B.H.B.B.H.B.B",
                ".B.B.B.B.B.B.B.",
                "B.B.H.B.B.H.B.B",
                ".B.B.B.B.B.B.B.",
                "B.B.H.B.B.H.B.B",
                ".B.B.B.B.B.B.B."
        ));

        cityTemplates.add(new CityTemplate(
                "Szeged",
                "B..H.",
                ".B.B.",
                "H.X.H",
                ".B.B.",
                ".H..B"
        ));

        cityTemplates.add(new CityTemplate(
                "Pecs",
                "B.H.B",
                ".B.B.",
                "H.X.H",
                ".B.B.",
                "B.H.B"
        ));

        cityTemplates.add(new CityTemplate(
                "Miskolc",
                "B..H.",
                ".B.B.",
                "H.X.H",
                ".B.B.",
                ".H..B"
        ));

        cityTemplates.add(new CityTemplate(
                "Gyor",
                "B.B.H.B.B",
                ".B.B.B.B.",
                "B.B.H.B.B",
                ".B.B.B.B.",
                "B.B.H.B.B",
                ".B.B.B.B.",
                "B.B.H.B.B",
                ".B.B.B.B."
        ));
    }

    public Game generate(int width, int height, int numCities, int numFacilities) {
        Game game = new Game(width, height);

        // Fixed positions for cities
        int[][] cityPositions = {
            {10, 10}, // Debrecen
            {50, 30}, // Budapest
            {20, 50}, // Szeged
            {60, 10}, // Pecs
            {30, 70}, // Miskolc
            {70, 60}  // Gyor
        };

        for (int i = 0; i < Math.min(numCities, cityPositions.length); i++) {
            CityTemplate template = cityTemplates.get(i % cityTemplates.size());
            int[] pos = cityPositions[i];
            City city = createCityFromTemplate("city_" + i, template, pos[0], pos[1]);
            game.addCity(city);
        }

        // Fixed positions for facilities
        int[][] facilityPositions = {
            {5, 40},
            {15, 20},
            {25, 60},
            {35, 15},
            {45, 55},
            {55, 25},
            {65, 45},
            {75, 35},
            {10, 65},
            {40, 75}
        };

        for (int i = 0; i < Math.min(numFacilities, facilityPositions.length) && i < FACILITY_DATA.length; i++) {
            String[] data = FACILITY_DATA[i % FACILITY_DATA.length];
            List<String> produces = new ArrayList<>();
            List<String> consumes = new ArrayList<>();
            if (!data[1].isEmpty()) produces.add(data[1]);
            if (!data[2].isEmpty()) consumes.add(data[2]);
            int[] pos = facilityPositions[i];
            Facility fac = new Facility(
                    "facility_" + i,
                    data[0],
                    pos[0],
                    pos[1],
                    FAC_W,
                    FAC_H,
                    produces,
                    consumes
            );
            game.addFacility(fac);
        }

        // Fixed water bodies
        WaterBody water1 = new WaterBody("water_0", "River Danube", 5, 25, 15, 2); // longer river
        game.addWaterBody(water1);
        placeBridgesOnWater(game, water1, 0);

        WaterBody water2 = new WaterBody("water_1", "Blue Lake", 35, 5, 6, 8);
        game.addWaterBody(water2);
        placeBridgesOnWater(game, water2, 1);

        WaterBody water3 = new WaterBody("water_2", "Tisza River", 25, 10, 2, 20); // vertical river
        game.addWaterBody(water3);
        placeBridgesOnWater(game, water3, 2);

        WaterBody water4 = new WaterBody("water_3", "Balaton Lake", 10, 30, 8, 5);
        game.addWaterBody(water4);
        placeBridgesOnWater(game, water4, 3);

        // Fixed forests
        Forest forest1 = new Forest("forest_0", "Dark Wood", 15, 35, 4, 3);
        game.addForest(forest1);

        Forest forest2 = new Forest("forest_1", "Pine Forest", 55, 15, 3, 4);
        game.addForest(forest2);

        Forest forest3 = new Forest("forest_2", "Oak Grove", 45, 5, 2, 2);
        game.addForest(forest3);

        Forest forest4 = new Forest("forest_3", "Birch Woods", 5, 60, 3, 2);
        game.addForest(forest4);

        Forest forest5 = new Forest("forest_4", "Maple Forest", 65, 70, 2, 3);
        game.addForest(forest5);

        return game;
    }

    private City createCityFromTemplate(String id, CityTemplate template, int startX, int startY) {
        City city = new City(id, template.getName(), startX, startY, template.getWidth(), template.getHeight());

        for (int y = 0; y < template.getHeight(); y++) {
            for (int x = 0; x < template.getWidth(); x++) {
                int worldX = startX + x;
                int worldY = startY + y;
                char symbol = template.getTileSymbol(x, y);

                switch (symbol) {
                    case 'B' -> city.addBuildingTile(worldX, worldY);
                    case '.' -> city.addEmptyTile(worldX, worldY);
                    case 'H' -> city.addRoadTile(worldX, worldY, City.CityRoadType.HORIZONTAL);
                    case 'V' -> city.addRoadTile(worldX, worldY, City.CityRoadType.VERTICAL);
                    case 'X' -> city.addRoadTile(worldX, worldY, City.CityRoadType.CROSSROAD);
                }
            }
        }

        for (Position p : city.getEmptyTiles()) {
            if (p.getX() == startX ||
                    p.getX() == startX + template.getWidth() - 1 ||
                    p.getY() == startY ||
                    p.getY() == startY + template.getHeight() - 1) {
                city.addEntrance(p.getX(), p.getY());
            }
        }

        return city;
    }

    private void placeBridgesOnWater(Game game, WaterBody water, int index) {
        int ox = water.getOrigin().getX();
        int oy = water.getOrigin().getY();
        int ww = water.getWidth();
        int wh = water.getHeight();

        Bridge bridge;
        if (ww >= wh) {
            int midRow = oy + wh / 2;
            bridge = new Bridge(
                    "bridge_" + index,
                    water.getName() + " Bridge",
                    ox,
                    midRow,
                    ww,
                    Bridge.Orientation.HORIZONTAL
            );
        } else {
            int midCol = ox + ww / 2;
            bridge = new Bridge(
                    "bridge_" + index,
                    water.getName() + " Bridge",
                    midCol,
                    oy,
                    wh,
                    Bridge.Orientation.VERTICAL
            );
        }

        new ConstructionService().buildBridge(game, bridge);
    }

    private int[] findFreePosition(int mapW, int mapH, int entityW, int entityH, List<int[]> occupied) {
        if (mapW - entityW - 2 <= 0 || mapH - entityH - 2 <= 0) {
            return null;
        }

        for (int attempt = 0; attempt < 200; attempt++) {
            int x = 1 + rng.nextInt(Math.max(1, mapW - entityW - 2));
            int y = 1 + rng.nextInt(Math.max(1, mapH - entityH - 2));

            if (!overlaps(x, y, entityW, entityH, occupied)) {
                return new int[]{x, y};
            }
        }

        return null;
    }

    private boolean overlaps(int x, int y, int w, int h, List<int[]> occupied) {
        for (int[] rect : occupied) {
            int rx = rect[0];
            int ry = rect[1];
            int rw = rect[2];
            int rh = rect[3];

            if (x < rx + rw && x + w > rx && y < ry + rh && y + h > ry) {
                return true;
            }
        }
        return false;
    }
}