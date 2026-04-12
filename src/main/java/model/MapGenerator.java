package model;

import java.util.ArrayList;
import java.util.Collections;
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
            {"Farm", "WOOD", ""},
            {"Coal Mine", "COAL", ""},
            {"Oil Refinery", "OIL", ""},
            {"Plastic Factory", "PLASTIC", "OIL"},
            {"Textile Mill", "TEXTILE", "COTTON"},
            {"Cotton Farm", "COTTON", ""}
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
                "B.B.B",
                ".B.B.",
                "B.V.B",
                ".B.B.",
                "B.B.B"
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

        cityTemplates.add(new CityTemplate(
                "Sopron",
                "B.B.B.B.B.B",
                ".B.B.B.B.B.",
                "HHHHHHHHHHH",
                ".B.B.B.B.B.",
                "B.B.B.B.B.B",
                ".B.B.B.B.B."
        ));
    }

    public Game generate(int width, int height, int numCities, int numFacilities) {
        Game game = new Game(width, height);

        // Fixed positions for cities
        int[][] cityPositions = {
                {10, 10}, // Debrecen
                {50, 10}, // Budapest
                {20, 50}, // Szeged
                {50, 46}, // Pecs
                {30, 70}, // Miskolc
                {70, 60}, // Gyor
                {28, 0}   // Sopron
        };

        for (int i = 0; i < Math.min(numCities, cityPositions.length); i++) {
            CityTemplate template = cityTemplates.get(i % cityTemplates.size());
            int[] pos = cityPositions[i];
            City city = createCityFromTemplate("city_" + i, template, pos[0], pos[1]);

            // Place buildings on 30% of empty tiles
            List<Position> empties = new ArrayList<>(city.getEmptyTiles());
            int numBuildings = (int) Math.ceil(empties.size() * 0.3);
            Collections.shuffle(empties, new Random(rng.nextLong()));
            for (int j = 0; j < numBuildings && j < empties.size(); j++) {
                Position p = empties.get(j);
                city.getEmptyTiles().remove(p);
                city.addBuildingTile(p.getX(), p.getY());
            }

            game.addCity(city);
        }

        // Collect city rectangles to avoid placing forests inside cities
        List<int[]> cityRects = new ArrayList<>();
        for (City city : game.getCities()) {
            cityRects.add(new int[]{city.getOrigin().getX(), city.getOrigin().getY(), city.getWidth(), city.getHeight()});
        }

        // Fixed positions for facilities
        int[][] facilityPositions = {
                {5, 40},
                {15, 20},
                {25, 60},
                {33, 15},  // Paper Mill moved to occupy (33,15)(34,15)(33,16)(34,16)
                {45, 55},
                {55, 25},
                {65, 45},
                {75, 35},
                {10, 65},
                {40, 75},  // Cotton Farm
                {0, 10},
                {5, 5},
                {70, 5},
                {20, 10},
                {50, 50}
        };

        for (int i = 0; i < Math.min(numFacilities, facilityPositions.length) && i < FACILITY_DATA.length; i++) {
            String[] data = FACILITY_DATA[i % FACILITY_DATA.length];
            List<String> produces = new ArrayList<>();
            List<String> consumes = new ArrayList<>();
            if (!data[1].isEmpty()) produces.add(data[1]);
            if (!data[2].isEmpty()) consumes.add(data[2]);
            int[] pos = facilityPositions[i];
            int facW = FAC_W;
            int facH = FAC_H;
            if (data[0].equals("Cotton Farm")) {
                facW = 3;  // Make Cotton Farm 3x2 to occupy additional tiles (42,75) and (42,76)
            }
            Facility fac = new Facility(
                    "facility_" + i,
                    data[0],
                    pos[0],
                    pos[1],
                    facW,
                    facH,
                    produces,
                    consumes
            );
            game.addFacility(fac);
        }

        // Special 3x3 facility
        Facility specialFac = new Facility(
                "facility_special",
                "Large Factory",
                0,
                0,
                3,
                3,
                List.of("STEEL"),
                List.of("IRON")
        );
        game.addFacility(specialFac);

        // Fixed water bodies
        WaterBody water1 = new WaterBody("water_0", "Balaton Lake", 0, 30, 80, 5);
        game.addWaterBody(water1);

        WaterBody water2 = new WaterBody("water_1", "Blue Lake", 35, 0, 6, 80);
        game.addWaterBody(water2);

        // Fixed forests
        if (!overlaps(15, 35, 4, 3, cityRects)) {
            Forest forest1 = new Forest("forest_0", "Dark Wood", 15, 35, 4, 3);
            game.addForest(forest1);
        }

        if (!overlaps(55, 15, 3, 4, cityRects)) {
            Forest forest2 = new Forest("forest_1", "Pine Forest", 55, 15, 3, 4);
            game.addForest(forest2);
        }

        if (!overlaps(45, 5, 2, 2, cityRects)) {
            Forest forest3 = new Forest("forest_2", "Oak Grove", 45, 5, 2, 2);
            game.addForest(forest3);
        }

        if (!overlaps(5, 60, 3, 2, cityRects)) {
            Forest forest4 = new Forest("forest_3", "Birch Woods", 5, 60, 3, 2);
            game.addForest(forest4);
        }

        if (!overlaps(65, 70, 2, 3, cityRects)) {
            Forest forest5 = new Forest("forest_4", "Maple Forest", 65, 70, 2, 3);
            game.addForest(forest5);
        }

        // Additional forests near lakes
        if (!overlaps(0, 35, 3, 2, cityRects)) {
            Forest forest6 = new Forest("forest_5", "Willow Grove", 0, 35, 3, 2);
            game.addForest(forest6);
        }

        if (!overlaps(10, 35, 4, 3, cityRects)) {
            Forest forest7 = new Forest("forest_6", "Cedar Woods", 10, 35, 4, 3);
            game.addForest(forest7);
        }

        if (!overlaps(20, 35, 3, 2, cityRects)) {
            Forest forest8 = new Forest("forest_7", "Elm Forest", 20, 35, 3, 2);
            game.addForest(forest8);
        }

        if (!overlaps(70, 35, 2, 3, cityRects)) {
            Forest forest9 = new Forest("forest_8", "Spruce Thicket", 70, 35, 2, 3);
            game.addForest(forest9);
        }

        if (!overlaps(75, 35, 3, 2, cityRects)) {
            Forest forest10 = new Forest("forest_9", "Ash Woods", 75, 35, 3, 2);
            game.addForest(forest10);
        }

        if (!overlaps(41, 0, 2, 4, cityRects)) {
            Forest forest11 = new Forest("forest_10", "Lake Pines", 41, 0, 2, 4);
            game.addForest(forest11);
        }

        if (!overlaps(41, 10, 3, 3, cityRects)) {
            Forest forest12 = new Forest("forest_11", "River Oaks", 41, 10, 3, 3);
            game.addForest(forest12);
        }

        if (!overlaps(41, 20, 2, 2, cityRects)) {
            Forest forest13 = new Forest("forest_12", "Water Birches", 41, 20, 2, 2);
            game.addForest(forest13);
        }

        if (!overlaps(41, 40, 3, 2, cityRects)) {
            Forest forest14 = new Forest("forest_13", "Shore Maples", 41, 40, 3, 2);
            game.addForest(forest14);
        }

        if (!overlaps(41, 70, 2, 3, cityRects)) {
            Forest forest15 = new Forest("forest_14", "Bay Cedars", 41, 70, 2, 3);
            game.addForest(forest15);
        }

        if (!overlaps(30, 0, 2, 4, cityRects)) {
            Forest forest16 = new Forest("forest_15", "Coastal Pines", 30, 0, 2, 4);
            game.addForest(forest16);
        }

        return game;
    }

    private City createCityFromTemplate(String id, CityTemplate template, int startX, int startY) {
        City city = new City(id, template.getName(), startX, startY, template.getWidth(), template.getHeight());

        for (int y = 0; y < template.getHeight(); y++) {
            for (int x = 0; x < template.getWidth(); x++) {
                int worldX = startX + x;
                int worldY = startY + y;
                city.addEmptyTile(worldX, worldY);
            }
        }

        // Add roads for specific cities
        String cityName = template.getName();
        if (cityName.equals("Miskolc")) {
            // Horizontal road (30,72)-(34,72)
            for (int x = 30; x <= 34; x++) {
                Position p = new Position(x, 72);
                city.addRoadTile(x, 72, City.CityRoadType.HORIZONTAL);
                city.getEmptyTiles().remove(p);
            }
            // Vertical road (32,70)-(32,74)
            for (int y = 70; y <= 74; y++) {
                Position p = new Position(32, y);
                city.addRoadTile(32, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            // Crossroad at intersection (32,72)
            Position p = new Position(32, 72);
            city.addRoadTile(32, 72, City.CityRoadType.CROSSROAD);
            city.getEmptyTiles().remove(p);
        } else if (cityName.equals("Szeged")) {
            // Horizontal road (20,52)-(24,52)
            for (int x = 20; x <= 24; x++) {
                Position p = new Position(x, 52);
                city.addRoadTile(x, 52, City.CityRoadType.HORIZONTAL);
                city.getEmptyTiles().remove(p);
            }
            // Vertical road (22,50)-(22,54)
            for (int y = 50; y <= 54; y++) {
                Position p = new Position(22, y);
                city.addRoadTile(22, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            // Crossroad at intersection (22,52)
            Position p = new Position(22, 52);
            city.addRoadTile(22, 52, City.CityRoadType.CROSSROAD);
            city.getEmptyTiles().remove(p);
        } else if (cityName.equals("Debrecen")) {
            // Vertical road (12,10)-(12,14)
            for (int y = 10; y <= 14; y++) {
                Position p = new Position(12, y);
                city.addRoadTile(12, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
        } else if (cityName.equals("Gyor")) {
            // Vertical road (74,60)-(74,67)
            for (int y = 60; y <= 67; y++) {
                Position p = new Position(74, y);
                city.addRoadTile(74, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
        } else if (cityName.equals("Budapest")) {
            // Vertical road (60,10)-(60,21)
            for (int y = 10; y <= 21; y++) {
                Position p = new Position(60, y);
                city.addRoadTile(60, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            // Vertical road (54,10)-(54,21)
            for (int y = 10; y <= 21; y++) {
                Position p = new Position(54, y);
                city.addRoadTile(54, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
        } else if (cityName.equals("Pecs")) {
            // Horizontal road (50,48)-(54,48)
            for (int x = 50; x <= 54; x++) {
                Position p = new Position(x, 48);
                city.addRoadTile(x, 48, City.CityRoadType.HORIZONTAL);
                city.getEmptyTiles().remove(p);
            }
            // Vertical road (52,46)-(52,50)
            for (int y = 46; y <= 50; y++) {
                Position p = new Position(52, y);
                city.addRoadTile(52, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            // Crossroad at intersection (52,48)
            Position p = new Position(52, 48);
            city.addRoadTile(52, 48, City.CityRoadType.CROSSROAD);
            city.getEmptyTiles().remove(p);
        } else if (cityName.equals("Sopron")) {
            // Horizontal road (28,2)-(34,2)
            for (int x = 28; x <= 34; x++) {
                Position p = new Position(x, 2);
                city.addRoadTile(x, 2, City.CityRoadType.HORIZONTAL);
                city.getEmptyTiles().remove(p);
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
