package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class MapGenerator {
    private static final int CITY_MIN_W = 16;
    private static final int CITY_MAX_W = 20;
    private static final int CITY_MIN_H = 16;
    private static final int CITY_MAX_H = 20;
    private static final int FAC_W = 2;
    private static final int FAC_H = 2;
    private static final int PADDING = 2;

    private static final String[] CITY_NAMES = {
            "Budapest", "Debrecen", "Pecs", "Gyongyos", "Nyiregyhaza"
    };

    private static final String[][] FACILITY_DATA = {
            {"Iron Mine", "IRON", ""},
            {"Lumber Mill", "WOOD", ""},
            {"Steel Works", "STEEL", "IRON"},
            {"Paper Mill", "PAPER", "WOOD"},
            {"Farm", "WOOD", ""},
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

    private void initializeCityTemplates() {
        // Debrecen: 20% buildings, 80% empty
        cityTemplates.add(new CityTemplate("Debrecen", 16, 16, 0.20));

        // Budapest: 30% buildings, 70% empty
        cityTemplates.add(new CityTemplate("Budapest", 20, 20, 0.30));
    }

    public MapGenerator(long seed) {
        this.rng = new Random(seed);
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
            // Use predefined city templates instead of random generation
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

        boolean[][] layout = template.getLayout();

        // Mark border as entrances
        for (int x = 0; x < template.getWidth(); x++) {
            city.addEntrance(startX + x, startY);
            city.addEntrance(startX + x, startY + template.getHeight() - 1);
        }
        for (int y = 0; y < template.getHeight(); y++) {
            city.addEntrance(startX, startY + y);
            city.addEntrance(startX + template.getWidth() - 1, startY + y);
        }

        // Add buildings according to template layout
        for (int y = 0; y < template.getHeight(); y++) {
            for (int x = 0; x < template.getWidth(); x++) {
                int worldX = startX + x;
                int worldY = startY + y;

                if (layout[x][y]) {
                    city.addBuildingTile(worldX, worldY);
                }
            }
        }

        return city;
    }

    private List<Integer> generateRoadLines(int length) {
        Set<Integer> lines = new TreeSet<>();

        // always keep a center spine
        addLineIfValid(lines, length / 2, length);

        int pos = 2 + rng.nextInt(2); // 2 or 3
        while (pos < length - 2) {
            addLineIfValid(lines, pos, length);
            pos += 4 + rng.nextInt(3); // spacing 4..6
        }

        // sometimes add one more road near an edge
        if (rng.nextBoolean()) {
            addLineIfValid(lines, 1, length);
        }
        if (rng.nextBoolean()) {
            addLineIfValid(lines, length - 2, length);
        }

        // if too few roads, force a couple more
        addLineIfValid(lines, 3, length);
        addLineIfValid(lines, length - 4, length);

        return new ArrayList<>(lines);
    }

    private void addLineIfValid(Set<Integer> lines, int value, int length) {
        if (value <= 0 || value >= length - 1) return;

        for (int existing : lines) {
            if (Math.abs(existing - value) < 3) {
                return; // avoid roads too close to each other
            }
        }
        lines.add(value);
    }

    private void addExtraConnectors(boolean[][] roads,
                                    int width,
                                    int height,
                                    List<Integer> verticalRoads,
                                    List<Integer> horizontalRoads) {

        if (verticalRoads.size() >= 2) {
            for (int i = 0; i < verticalRoads.size() - 1; i++) {
                if (rng.nextDouble() < 0.55) {
                    int y = 2 + rng.nextInt(Math.max(1, height - 4));
                    for (int x = verticalRoads.get(i); x <= verticalRoads.get(i + 1); x++) {
                        roads[x][y] = true;
                    }
                }
            }
        }

        if (horizontalRoads.size() >= 2) {
            for (int i = 0; i < horizontalRoads.size() - 1; i++) {
                if (rng.nextDouble() < 0.55) {
                    int x = 2 + rng.nextInt(Math.max(1, width - 4));
                    for (int y = horizontalRoads.get(i); y <= horizontalRoads.get(i + 1); y++) {
                        roads[x][y] = true;
                    }
                }
            }
        }
    }

    private void carveOpenLots(boolean[][] buildings, boolean[][] roads, int width, int height) {
        int lotCount = 2 + rng.nextInt(3);

        for (int i = 0; i < lotCount; i++) {
            int lotW = 2 + rng.nextInt(3); // 2..4
            int lotH = 2 + rng.nextInt(3); // 2..4

            if (width - lotW - 2 <= 0 || height - lotH - 2 <= 0) continue;

            int x = 1 + rng.nextInt(width - lotW - 1);
            int y = 1 + rng.nextInt(height - lotH - 1);

            for (int dy = 0; dy < lotH; dy++) {
                for (int dx = 0; dx < lotW; dx++) {
                    if (!roads[x + dx][y + dy]) {
                        buildings[x + dx][y + dy] = false;
                    }
                }
            }
        }
    }

    private boolean touchesIntersection(boolean[][] roads, int x, int y) {
        int[][] deltas = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] d : deltas) {
            int rx = x + d[0];
            int ry = y + d[1];

            if (!isRoad(roads, rx, ry)) continue;

            int degree = countRoadNeighbours(roads, rx, ry);
            if (degree >= 3) {
                return true;
            }
        }

        return false;
    }

    private int countRoadNeighbours(boolean[][] roads, int x, int y) {
        int count = 0;
        if (isRoad(roads, x, y - 1)) count++;
        if (isRoad(roads, x, y + 1)) count++;
        if (isRoad(roads, x - 1, y)) count++;
        if (isRoad(roads, x + 1, y)) count++;
        return count;
    }

    private boolean isRoad(boolean[][] roads, int x, int y) {
        return x >= 0
                && x < roads.length
                && y >= 0
                && y < roads[0].length
                && roads[x][y];
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

    private int[] findFreePosition(int mapW, int mapH, int entityW, int entityH,
                                   List<int[]> occupied) {
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