package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                "BBHBBVBBHBBVBBHB",
                "VVXVVXVVXVVXVVXV",
                "BBHBBVBBHBBVBBHB",
                "BBHBVHBBHBVHBBHB",
                "VVXVXVVVXVXVVVXV",
                "BBHVBBBBHVBBBBHB",
                "BBHVBBBBHVBBBBHB",
                "VVXVVVVVXVVVVVXV",
                "BBHBBVBBHBBVBBHB",
                "BBHBVHBBHBVHBBHB",
                "VVXVXVVVXVXVVVXV",
                "BBHVBBBBHVBBBBHB",
                "BBHBBVBBHBBVBBHB",
                "VVXVVXVVXVVXVVXV",
                "BBHBBVBBHBBVBBHB",
                "BBHBBVBBHBBVBBHB"
        ));

        cityTemplates.add(new CityTemplate(
                "Budapest",
                "BBHBBVBBBHBBVBBBHBB",
                "VVXVVXVVVXVVXVVVXVV",
                "BBHBVHBBBHBVHBBBHBB",
                "BBHVBBBBHBBVBBBBHBB",
                "VVXVVXVVVXVVXVVVXVV",
                "BBHBBVBBBHBBVBBBHBB",
                "BBHBVHBBBHBVHBBBHBB",
                "VVXVXVVVVXVXVVVVXVV",
                "BBHVBBBBHBBVBBBBHBB",
                "BBHBBVBBBHBBVBBBHBB",
                "VVXVVXVVVXVVXVVVXVV",
                "BBHBVHBBBHBVHBBBHBB",
                "BBHVBBBBHBBVBBBBHBB",
                "VVXVVXVVVXVVXVVVXVV",
                "BBHBBVBBBHBBVBBBHBB",
                "BBHBVHBBBHBVHBBBHBB",
                "VVXVXVVVVXVXVVVVXVV",
                "BBHVBBBBHBBVBBBBHBB",
                "VVXVVXVVVXVVXVVVXVV",
                "BBHBBVBBBHBBVBBBHBB"
        ));
    }

    public Game generate(int width, int height, int numCities, int numFacilities) {
        Game game = new Game(width, height);
        List<int[]> occupied = new ArrayList<>();

        int numWater = 2 + rng.nextInt(2);
        for (int i = 0; i < numWater; i++) {
            int ww = 3 + rng.nextInt(3);
            int wh = 2 + rng.nextInt(3);

            int[] pos = findFreePosition(width, height, ww, wh, occupied);
            if (pos == null) continue;

            String name = WATER_NAMES[i % WATER_NAMES.length];
            WaterBody water = new WaterBody("water_" + i, name, pos[0], pos[1], ww, wh);
            game.addWaterBody(water);

            occupied.add(new int[]{pos[0] - 1, pos[1] - 1, ww + 2, wh + 2});
            placeBridgesOnWater(game, water, i);
        }

        int numForests = 2 + rng.nextInt(3);
        for (int i = 0; i < numForests; i++) {
            int fw = 2 + rng.nextInt(3);
            int fh = 2 + rng.nextInt(3);

            int[] pos = findFreePosition(width, height, fw, fh, occupied);
            if (pos == null) continue;

            String name = FOREST_NAMES[i % FOREST_NAMES.length];
            Forest forest = new Forest("forest_" + i, name, pos[0], pos[1], fw, fh);
            game.addForest(forest);

            occupied.add(new int[]{
                    pos[0] - PADDING,
                    pos[1] - PADDING,
                    fw + PADDING * 2,
                    fh + PADDING * 2
            });
        }

        for (int i = 0; i < numCities; i++) {
            CityTemplate template = cityTemplates.get(i % cityTemplates.size());

            int[] pos = findFreePosition(width, height, template.getWidth(), template.getHeight(), occupied);
            if (pos == null) continue;

            City city = createCityFromTemplate("city_" + i, template, pos[0], pos[1]);
            game.addCity(city);

            occupied.add(new int[]{
                    pos[0] - PADDING,
                    pos[1] - PADDING,
                    template.getWidth() + PADDING * 2,
                    template.getHeight() + PADDING * 2
            });
        }

        for (int i = 0; i < numFacilities && i < FACILITY_DATA.length; i++) {
            String[] data = FACILITY_DATA[i];

            List<String> produces = new ArrayList<>();
            List<String> consumes = new ArrayList<>();

            if (!data[1].isEmpty()) produces.add(data[1]);
            if (!data[2].isEmpty()) consumes.add(data[2]);

            int[] pos = findFreePosition(width, height, FAC_W, FAC_H, occupied);
            if (pos == null) continue;

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

            occupied.add(new int[]{
                    pos[0] - PADDING,
                    pos[1] - PADDING,
                    FAC_W + PADDING * 2,
                    FAC_H + PADDING * 2
            });
        }

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
                    case 'H' -> city.addRoadTile(worldX, worldY, City.CityRoadType.HORIZONTAL);
                    case 'V' -> city.addRoadTile(worldX, worldY, City.CityRoadType.VERTICAL);
                    case 'X' -> city.addRoadTile(worldX, worldY, City.CityRoadType.CROSSROAD);
                }
            }
        }

        for (Position p : city.getRoadTiles()) {
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

        game.addBridge(bridge);
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