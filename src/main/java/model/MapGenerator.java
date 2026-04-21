package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MapGenerator {
    private static final int FAC_W = 4;
    private static final int FAC_H = 3;
;
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

        int[][] cityPositions = {
                {10, 10},
                {50, 10},
                {20, 50},
                {50, 46},
                {30, 70},
                {70, 60},
                {28, 0}
        };

        for (int i = 0; i < Math.min(numCities, cityPositions.length); i++) {
            CityTemplate template = cityTemplates.get(i % cityTemplates.size());
            int[] pos = cityPositions[i];
            City city = createCityFromTemplate("city_" + i, template, pos[0], pos[1]);

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

        List<int[]> cityRects = new ArrayList<>();
        for (City city : game.getCities()) {
            cityRects.add(new int[]{city.getOrigin().getX(), city.getOrigin().getY(), city.getWidth(), city.getHeight()});
        }

        int[][] facilityPositions = {
                {6, 42},
                {14, 22},
                {22, 62},
                {31, 16},
                {47, 58},
                {58, 24},
                {62, 42},
                {66, 30},
                {10, 66},
                {50, 72},
                {4, 14},
                {8, 8},
                {62, 8},
                {20, 12},
                {56, 52}
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
                facW = 3;
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

        Facility specialFac = new Facility(
                "facility_special",
                "Large Factory",
                4,
                4,
                3,
                3,
                List.of("STEEL"),
                List.of("IRON")
        );

        game.addFacility(specialFac);

        generateFixedWater(game);

        if (!overlaps(15, 35, 4, 3, cityRects) && !overlapsWater(game, 15, 35, 4, 3)) {
            game.addForest(new Forest("forest_0", "Dark Wood", 15, 35, 4, 3));
        }

        if (!overlaps(55, 15, 3, 4, cityRects) && !overlapsWater(game, 55, 15, 3, 4)) {
            game.addForest(new Forest("forest_1", "Pine Forest", 55, 15, 3, 4));
        }

        if (!overlaps(45, 5, 2, 2, cityRects) && !overlapsWater(game, 45, 5, 2, 2)) {
            game.addForest(new Forest("forest_2", "Oak Grove", 45, 5, 2, 2));
        }

        if (!overlaps(5, 60, 3, 2, cityRects) && !overlapsWater(game, 5, 60, 3, 2)) {
            game.addForest(new Forest("forest_3", "Birch Woods", 5, 60, 3, 2));
        }

        if (!overlaps(65, 70, 2, 3, cityRects) && !overlapsWater(game, 65, 70, 2, 3)) {
            game.addForest(new Forest("forest_4", "Maple Forest", 65, 70, 2, 3));
        }

        if (!overlaps(0, 35, 3, 2, cityRects) && !overlapsWater(game, 0, 35, 3, 2)) {
            game.addForest(new Forest("forest_5", "Willow Grove", 0, 35, 3, 2));
        }

        if (!overlaps(10, 35, 4, 3, cityRects) && !overlapsWater(game, 10, 35, 4, 3)) {
            game.addForest(new Forest("forest_6", "Cedar Woods", 10, 35, 4, 3));
        }

        if (!overlaps(20, 35, 3, 2, cityRects) && !overlapsWater(game, 20, 35, 3, 2)) {
            game.addForest(new Forest("forest_7", "Elm Forest", 20, 35, 3, 2));
        }

        if (!overlaps(70, 35, 2, 3, cityRects) && !overlapsWater(game, 70, 35, 2, 3)) {
            game.addForest(new Forest("forest_8", "Spruce Thicket", 70, 35, 2, 3));
        }

        if (!overlaps(75, 35, 3, 2, cityRects) && !overlapsWater(game, 75, 35, 3, 2)) {
            game.addForest(new Forest("forest_9", "Ash Woods", 75, 35, 3, 2));
        }

        if (!overlaps(41, 0, 2, 4, cityRects) && !overlapsWater(game, 41, 0, 2, 4)) {
            game.addForest(new Forest("forest_10", "Lake Pines", 41, 0, 2, 4));
        }

        if (!overlaps(41, 10, 3, 3, cityRects) && !overlapsWater(game, 41, 10, 3, 3)) {
            game.addForest(new Forest("forest_11", "River Oaks", 41, 10, 3, 3));
        }

        if (!overlaps(41, 20, 2, 2, cityRects) && !overlapsWater(game, 41, 20, 2, 2)) {
            game.addForest(new Forest("forest_12", "Water Birches", 41, 20, 2, 2));
        }

        if (!overlaps(41, 40, 3, 2, cityRects) && !overlapsWater(game, 41, 40, 3, 2)) {
            game.addForest(new Forest("forest_13", "Shore Maples", 41, 40, 3, 2));
        }

        if (!overlaps(41, 70, 2, 3, cityRects) && !overlapsWater(game, 41, 70, 2, 3)) {
            game.addForest(new Forest("forest_14", "Bay Cedars", 41, 70, 2, 3));
        }

        if (!overlaps(30, 0, 2, 4, cityRects) && !overlapsWater(game, 30, 0, 2, 4)) {
            game.addForest(new Forest("forest_15", "Coastal Pines", 30, 0, 2, 4));
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

        String cityName = template.getName();
        if (cityName.equals("Miskolc")) {
            for (int x = 30; x <= 34; x++) {
                Position p = new Position(x, 72);
                city.addRoadTile(x, 72, City.CityRoadType.HORIZONTAL);
                city.getEmptyTiles().remove(p);
            }
            for (int y = 70; y <= 74; y++) {
                Position p = new Position(32, y);
                city.addRoadTile(32, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            Position p = new Position(32, 72);
            city.addRoadTile(32, 72, City.CityRoadType.CROSSROAD);
            city.getEmptyTiles().remove(p);
        } else if (cityName.equals("Szeged")) {
            for (int x = 20; x <= 24; x++) {
                Position p = new Position(x, 52);
                city.addRoadTile(x, 52, City.CityRoadType.HORIZONTAL);
                city.getEmptyTiles().remove(p);
            }
            for (int y = 50; y <= 54; y++) {
                Position p = new Position(22, y);
                city.addRoadTile(22, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            Position p = new Position(22, 52);
            city.addRoadTile(22, 52, City.CityRoadType.CROSSROAD);
            city.getEmptyTiles().remove(p);
        } else if (cityName.equals("Debrecen")) {
            for (int y = 10; y <= 14; y++) {
                Position p = new Position(12, y);
                city.addRoadTile(12, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
        } else if (cityName.equals("Gyor")) {
            for (int y = 60; y <= 67; y++) {
                Position p = new Position(74, y);
                city.addRoadTile(74, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
        } else if (cityName.equals("Budapest")) {
            for (int y = 10; y <= 21; y++) {
                Position p = new Position(60, y);
                city.addRoadTile(60, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            for (int y = 10; y <= 21; y++) {
                Position p = new Position(54, y);
                city.addRoadTile(54, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
        } else if (cityName.equals("Pecs")) {
            for (int x = 50; x <= 54; x++) {
                Position p = new Position(x, 48);
                city.addRoadTile(x, 48, City.CityRoadType.HORIZONTAL);
                city.getEmptyTiles().remove(p);
            }
            for (int y = 46; y <= 50; y++) {
                Position p = new Position(52, y);
                city.addRoadTile(52, y, City.CityRoadType.VERTICAL);
                city.getEmptyTiles().remove(p);
            }
            Position p = new Position(52, 48);
            city.addRoadTile(52, 48, City.CityRoadType.CROSSROAD);
            city.getEmptyTiles().remove(p);
        } else if (cityName.equals("Sopron")) {
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

    private boolean overlapsWater(Game game, int x, int y, int w, int h) {
        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                Tile tile = game.getTile(x + dx, y + dy);
                if (tile != null && tile.getType() == model.enums.TileType.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    private void generateFixedWater(Game game) {
        Set<Position> riverTiles = new LinkedHashSet<>();

        // Prettier fixed river, fully connected, top to bottom
        addRiverStroke(riverTiles, 39, 0, 4, 12, 0, 0);
        addRiverStroke(riverTiles, 39, 12, 4, 10, 3, -1);
        addRiverStroke(riverTiles, 36, 22, 5, 12, 0, 0);
        addRiverStroke(riverTiles, 36, 34, 4, 12, 4, 1);
        addRiverStroke(riverTiles, 39, 46, 5, 12, 0, 0);
        addRiverStroke(riverTiles, 39, 58, 4, 10, 3, 1);
        addRiverStroke(riverTiles, 42, 68, 5, 12, 0, 0);

        WaterBody mainRiver = createWaterBodyFromTiles("water_0", "Silver River", riverTiles);
        if (mainRiver != null) {
            game.addWaterBody(mainRiver);
        }

        Set<Position> lake1Tiles = new LinkedHashSet<>();
        addLakeEllipse(lake1Tiles, 9, 53, 4, 3);

        WaterBody lake1 = createWaterBodyFromTiles("water_2", "Blue Lake", lake1Tiles);
        if (lake1 != null) {
            game.addWaterBody(lake1);
        }

        Set<Position> lake2Tiles = new LinkedHashSet<>();
        addLakeEllipse(lake2Tiles, 63, 69, 4, 3);

        WaterBody lake2 = createWaterBodyFromTiles("water_3", "Crystal Lake", lake2Tiles);
        if (lake2 != null) {
            game.addWaterBody(lake2);
        }
    }

    private void addRiverStroke(Set<Position> tiles, int startX, int startY, int width, int length, int shiftEvery, int shiftDirection) {
        int x = startX;
        for (int row = 0; row < length; row++) {
            for (int dx = 0; dx < width; dx++) {
                tiles.add(new Position(x + dx, startY + row));
            }
            if (shiftEvery > 0 && (row + 1) % shiftEvery == 0) {
                x += shiftDirection;
            }
        }
    }

    private void addLakeEllipse(Set<Position> tiles, int centerX, int centerY, int radiusX, int radiusY) {
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                double nx = dx / (double) radiusX;
                double ny = dy / (double) radiusY;
                if ((nx * nx) + (ny * ny) <= 1.0) {
                    tiles.add(new Position(centerX + dx, centerY + dy));
                }
            }
        }
    }

    private WaterBody createWaterBodyFromTiles(String id, String name, Set<Position> tiles) {
        if (tiles.isEmpty()) return null;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Position p : tiles) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }

        return new WaterBody(
                id,
                name,
                minX,
                minY,
                (maxX - minX) + 1,
                (maxY - minY) + 1,
                new ArrayList<>(tiles)
        );
    }
}
