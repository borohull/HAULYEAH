package view.panel;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import model.Bridge;
import model.City;
import model.Facility;
import model.Game;
import model.MapEntity;
import model.Position;
import model.Road;
import model.Stop;
import model.Tile;
import model.TrafficLight;
import model.Vehicle;
import model.enums.CargoType;
import model.enums.Direction;
import model.enums.TileType;

import java.util.List;
import java.util.Random;

public class MapPanel extends Canvas {

    private static final int TILE_W = 64;
    private static final int TILE_H = 32;
    private static final int WALL_H = 24;

    private static final int ORIGIN_X = 0;
    private static final int ORIGIN_Y = WALL_H + 20;

    private static final Color COL_EMPTY_TOP  = Color.rgb(140, 185, 110);
    private static final Color COL_ROAD_TOP   = Color.rgb(90, 90, 90);

    private static final Color COL_CITY_TOP   = Color.rgb(80, 130, 210);
    private static final Color COL_CITY_LEFT  = Color.rgb(50, 90, 160);
    private static final Color COL_CITY_RIGHT = Color.rgb(60, 110, 190);
    private static final Color COL_CITY_ROOF  = Color.rgb(110, 165, 240);

    private static final Color COL_CITY_EMPTY_TOP = Color.rgb(150, 150, 150);

    private static final Color COL_FAC_TOP    = Color.rgb(200, 145, 55);
    private static final Color COL_FAC_LEFT   = Color.rgb(140, 95, 30);
    private static final Color COL_FAC_RIGHT  = Color.rgb(170, 115, 40);
    private static final Color COL_FAC_ROOF   = Color.rgb(230, 175, 90);

    private static final Color COL_WATER_TOP   = Color.rgb(55, 130, 200);
    private static final Color COL_WATER_LEFT  = Color.rgb(30, 90, 155);
    private static final Color COL_WATER_RIGHT = Color.rgb(40, 110, 175);
    private static final Color COL_WATER_ROOF  = Color.rgb(90, 175, 240);

    private static final Color COL_FOREST_TOP   = Color.rgb(45, 120, 55);
    private static final Color COL_FOREST_LEFT  = Color.rgb(25, 80, 35);
    private static final Color COL_FOREST_RIGHT = Color.rgb(35, 100, 45);
    private static final Color COL_FOREST_ROOF  = Color.rgb(75, 160, 85);

    private static final Color COL_BRIDGE_TOP   = Color.rgb(180, 160, 120);
    private static final Color COL_BRIDGE_LEFT  = Color.rgb(120, 105, 75);
    private static final Color COL_BRIDGE_RIGHT = Color.rgb(150, 130, 95);
    private static final Color COL_BRIDGE_ROOF  = Color.rgb(210, 195, 155);

    private static final Color COL_STOP_TOP = Color.rgb(220, 80, 80);
    private static final Color COL_CITY_ROAD = Color.rgb(90, 90, 90);
    private static final Color COL_GRID     = Color.rgb(0, 0, 0, 0.08);
    private static final Color COL_LABEL    = Color.rgb(0, 0, 0, 0.55);

    private final Image grassImage      = loadTileImage("/images/grass.png");
    private final Image waterImage      = loadTileImage("/images/water.png");
    private final Image fieldImage      = loadTileImage("/images/field.png");
    private final Image plantationImage = loadTileImage("/images/plantation.png");
    private final Image stopImage       = loadTileImage("/images/stop.png");
    private final Image roadHorImage    = loadTileImage("/images/roadHor.png");
    private final Image roadVertImage   = loadTileImage("/images/roadVert.png");
    private final Image busImage        = loadTileImage("/images/bus.png");
    private final Image truckImage      = loadTileImage("/images/truck.png");

    private final Image[] facilityImages = {
            loadTileImage("/images/facility1.png"),
            loadTileImage("/images/facility2.png"),
            loadTileImage("/images/facility3.png"),
            loadTileImage("/images/facility4.png"),
            loadTileImage("/images/facility5.png")
    };

    private final Image[] cityBuildingImages = {
            loadTileImage("/images/buildings/building.png"),
            loadTileImage("/images/buildings/building-1.png"),
            loadTileImage("/images/buildings/building-2.png"),
            loadTileImage("/images/buildings/commercial-building.png"),
            loadTileImage("/images/buildings/hotel.png"),
            loadTileImage("/images/buildings/hotel-1.png"),
            loadTileImage("/images/buildings/hotel-service.png"),
            loadTileImage("/images/buildings/house-building.png"),
            loadTileImage("/images/buildings/plaza.png"),
            loadTileImage("/images/buildings/plaza-1.png"),
            loadTileImage("/images/buildings/restaurant.png"),
            loadTileImage("/images/buildings/shop.png"),
            loadTileImage("/images/buildings/shopping-center.png"),
            loadTileImage("/images/buildings/shopping-complex.png"),
            loadTileImage("/images/buildings/shopping-mall.png"),
            loadTileImage("/images/buildings/skyscraper.png"),
            loadTileImage("/images/buildings/skyscraper-1.png"),
            loadTileImage("/images/buildings/store.png"),
            loadTileImage("/images/buildings/store-1.png"),
            loadTileImage("/images/buildings/structure.png")
    };

    private Game currentGame;
    private int storedOriginX;
    private int storedOriginY;

    private final Random rng = new Random();
    private final javafx.scene.canvas.Canvas overlayCanvas = new javafx.scene.canvas.Canvas();

    public javafx.scene.canvas.Canvas getOverlayCanvas() {
        return overlayCanvas;
    }

    private Image loadTileImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("Could not load image: " + path);
            return null;
        }
    }

    public MapPanel() {
        super(0, 0);
    }

    public void drawGame(Game game) {
        int cols = game.getWidth();
        int rows = game.getHeight();

        double canvasW = (cols + rows) * (TILE_W / 2.0) + TILE_W;
        double canvasH = (cols + rows) * (TILE_H / 2.0) + WALL_H + 60;
        setWidth(canvasW);
        setHeight(canvasH);
        overlayCanvas.setWidth(canvasW);
        overlayCanvas.setHeight(canvasH);

        int originX = rows * (TILE_W / 2) + ORIGIN_X;
        int originY = ORIGIN_Y;

        this.currentGame = game;
        this.storedOriginX = originX;
        this.storedOriginY = originY;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                drawGround(gc, game.getTile(x, y), x, y, originX, originY);
            }
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                drawStructure(gc, game.getTile(x, y), x, y, originX, originY);
            }
        }

        for (Facility facility : game.getFacilities()) {
            drawFacility(gc, facility, originX, originY);
        }

        gc.setFont(Font.font("Monospace", 11));
        gc.setTextAlign(TextAlignment.CENTER);

        for (MapEntity entity : game.getAllEntities()) {
            Position c = entity.getCenter();
            drawLabel(gc, c.getX(), c.getY(), entity.getName(), originX, originY);
        }
        for (Stop stop : game.getStops()) {
            Position c = stop.getPosition();
            drawLabel(gc, c.getX(), c.getY(), stop.getName(), originX, originY);
        }

        for (TrafficLight tl : game.getTrafficLights().values()) {
            drawTrafficLight(gc, tl, originX, originY);
        }

        drawDemandBadges(gc, game, originX, originY);

        for (Vehicle vehicle : game.getVehicles()) {
            drawVehicle(gc, vehicle, originX, originY);
        }
    }

    private void drawTrafficLight(GraphicsContext gc, TrafficLight tl, int ox, int oy) {
        int tx = tl.getPosition().getX();
        int ty = tl.getPosition().getY();

        double isoX = isoScreenX(tx, ty, ox);
        double isoY = isoScreenY(tx, ty, oy);

        Direction curGreen = tl.getCurrentGreenDirection();
        List<Direction> active = tl.getActiveDirections();

        double phaseDuration = curGreen != null ? tl.getGreenDuration(curGreen) : 10.0;
        boolean phaseIsGreen = tl.getPhaseTimer() > phaseDuration * 0.50;

        for (Direction dir : active) {
            double cx, cy;
            switch (dir) {
                case NORTH -> { cx = isoX + TILE_W / 4.0; cy = isoY + TILE_H / 4.0; }
                case EAST  -> { cx = isoX + TILE_W / 4.0; cy = isoY + TILE_H * 0.75; }
                case SOUTH -> { cx = isoX - TILE_W / 4.0; cy = isoY + TILE_H * 0.75; }
                case WEST  -> { cx = isoX - TILE_W / 4.0; cy = isoY + TILE_H / 4.0; }
                default    -> { cx = isoX;                cy = isoY + TILE_H / 2.0; }
            }
            boolean isGreen = (dir == curGreen) && phaseIsGreen;
            drawMiniLight(gc, cx, cy, isGreen);
        }
    }

    private void drawMiniLight(GraphicsContext gc, double cx, double cy, boolean isGreen) {
        double r = 4.0;
        double gap = 2.0;
        double totalW = r * 4 + gap;
        double startX = cx - totalW / 2.0;

        Color redColor = isGreen ? Color.rgb(60, 10, 10) : Color.rgb(255, 40, 40);
        gc.setFill(redColor);
        gc.fillOval(startX, cy - r, r * 2, r * 2);

        Color greenColor = isGreen ? Color.rgb(30, 215, 70) : Color.rgb(10, 45, 10);
        gc.setFill(greenColor);
        gc.fillOval(startX + r * 2 + gap, cy - r, r * 2, r * 2);

        gc.setGlobalAlpha(0.35);
        if (isGreen) {
            gc.setFill(Color.rgb(30, 220, 70));
            gc.fillOval(startX + r * 2 + gap - r * 0.4, cy - r * 1.4, r * 2.8, r * 2.8);
        } else {
            gc.setFill(Color.rgb(255, 40, 40));
            gc.fillOval(startX - r * 0.4, cy - r * 1.4, r * 2.8, r * 2.8);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawVehicle(GraphicsContext gc, Vehicle vehicle, int ox, int oy) {
        double cx = isoScreenXd(vehicle.getSmoothX(), vehicle.getSmoothY(), ox);
        double cy = isoScreenYd(vehicle.getSmoothX(), vehicle.getSmoothY(), oy);

        boolean isPassenger = vehicle.getType().isPassenger();
        Image img = isPassenger ? busImage : truckImage;

        double imgW = TILE_W * 0.95;
        double imgH = imgW * 0.70;
        double imgX = cx - imgW / 2.0;
        double imgY = cy + TILE_H * 0.02 - imgH / 2.0;

        gc.setFill(Color.rgb(0, 0, 0, 0.22));
        gc.fillOval(cx - imgW * 0.28, cy + TILE_H * 0.18, imgW * 0.56, TILE_H * 0.18);

        if (img != null) {
            gc.drawImage(img, imgX, imgY, imgW, imgH);
        } else {
            gc.setFill(isPassenger ? Color.rgb(30, 120, 220) : Color.rgb(220, 100, 30));
            gc.fillOval(cx - 8, cy, 16, 10);
        }
    }

    private Image getGroundImage(Tile tile) {
        return switch (tile.getType()) {
            case WATER -> null;
            case FOREST -> grassImage;
            case FACILITY -> null;
            case CITY, CITY_EMPTY, ROAD, CITY_ROAD -> null;
            case STOP, CITY_STOP -> stopImage != null ? stopImage : plantationImage;
            case BRIDGE -> waterImage;
            case EMPTY -> grassImage;
        };
    }

    private boolean isRoadLike(int x, int y) {
        if (currentGame == null || !currentGame.inBounds(x, y)) return false;
        TileType type = currentGame.getTile(x, y).getType();
        return type == TileType.ROAD || type == TileType.CITY_ROAD || type == TileType.BRIDGE;
    }

    private Image getCityBuildingImage(int tx, int ty) {
        Image[] available = getAvailableCityBuildingImages();
        if (available.length == 0) return null;
        int rowSeed = Math.floorMod((ty / 2) * 17 + (tx / 4) * 31, available.length);
        int variation = Math.floorMod(tx + ty, 3);
        return available[(rowSeed + variation) % available.length];
    }

    private Image[] getAvailableCityBuildingImages() {
        return java.util.Arrays.stream(cityBuildingImages)
                .filter(java.util.Objects::nonNull)
                .toArray(Image[]::new);
    }

    private void drawGround(GraphicsContext gc, Tile tile, int tx, int ty, int ox, int oy) {
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);

        double[] xs = diamondXs(tx, ty, ox);
        double[] ys = diamondYs(tx, ty, oy);

        TileType type = tile.getType();

        if (type == TileType.ROAD || type == TileType.CITY_ROAD) {
            Image roadImg = getRoadImage(tile, tx, ty);
            if (roadImg != null) {
                gc.drawImage(roadImg, cx - TILE_W / 2.0, cy - WALL_H, TILE_W, TILE_H + WALL_H * 2);
            } else {
                gc.setFill(COL_ROAD_TOP);
                gc.fillPolygon(xs, ys, 4);
            }
            return;
        }

        gc.setFill(groundColor(type));
        gc.fillPolygon(xs, ys, 4);

        Image tileImage = getGroundImage(tile);
        if (tileImage != null) {
            double imageX = cx - TILE_W / 2.0;
            double imageY = cy - TILE_H / 2.0;
            double imageW = TILE_W;
            double imageH = TILE_H * 2.0;
            gc.drawImage(tileImage, imageX, imageY, imageW, imageH);
        }
    }

    private Image getRoadImage(Tile tile, int tx, int ty) {
        return roadHorImage;
    }

    private Color groundColor(TileType type) {
        return switch (type) {
            case ROAD, CITY_ROAD -> COL_ROAD_TOP;
            case STOP, CITY_STOP -> COL_STOP_TOP;
            case CITY -> COL_CITY_TOP;
            case CITY_EMPTY -> COL_CITY_EMPTY_TOP;
            case FACILITY -> COL_FAC_TOP;
            case WATER -> COL_WATER_TOP;
            case FOREST -> COL_FOREST_TOP;
            case BRIDGE -> COL_BRIDGE_TOP;
            default -> COL_EMPTY_TOP;
        };
    }

    private void drawStructure(GraphicsContext gc, Tile tile, int tx, int ty, int ox, int oy) {
        switch (tile.getType()) {
            case CITY -> drawCityBuilding(gc, tile, tx, ty, ox, oy);
            case FACILITY -> { }
            case WATER -> { }
            case FOREST -> drawForestTree(gc, tx, ty, ox, oy);
            case BRIDGE -> drawBox(gc, tx, ty, ox, oy, COL_BRIDGE_LEFT, COL_BRIDGE_RIGHT, COL_BRIDGE_ROOF);
            case ROAD, CITY_ROAD, STOP, CITY_STOP -> { }
            default -> { }
        }
    }

    private void drawCityBuilding(GraphicsContext gc, Tile tile, int tx, int ty, int ox, int oy) {
        if (tile.getType() != TileType.CITY) return;
        Image img = getCityBuildingImage(tx, ty);
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);
        if (img != null) {
            double drawW = TILE_W * 1.08;
            double drawH = TILE_H + WALL_H + 14;
            gc.drawImage(img, cx - drawW / 2.0, cy - WALL_H - 10, drawW, drawH);

        }
    }
    private void drawFacility(GraphicsContext gc, Facility facility, int ox, int oy) {
        Image img = getFacilityImage(facility);
        if (img == null) return;

        Position center = facility.getCenter();

        double cx = isoScreenX(center.getX(), center.getY(), ox);
        double cy = isoScreenY(center.getX(), center.getY(), oy);

        double drawW = TILE_W * 1.65;
        double drawH = drawW * 0.74;

        double drawX = cx - TILE_W * 0.42;
        double drawY = cy - TILE_H * 1.02;


        gc.drawImage(img, drawX, drawY, drawW, drawH);
    }

    private Image getFacilityImage(Facility facility) {
        Image[] available = java.util.Arrays.stream(facilityImages)
                .filter(java.util.Objects::nonNull)
                .toArray(Image[]::new);

        if (available.length == 0) return null;

        int hash = Math.abs(facility.getName().hashCode());
        return available[hash % available.length];
    }

    private void drawBox(GraphicsContext gc, int tx, int ty, int ox, int oy,
                         Color colLeft, Color colRight, Color colRoof) {
        double inset = 6;
        double hw = TILE_W / 2.0 - inset;
        double hh = TILE_H / 2.0 - inset / 2.0;

        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);

        double topX = cx, topY = cy;
        double rightX = cx + hw, rightY = cy + hh;
        double leftX = cx - hw, leftY = cy + hh;
        double botY = cy + TILE_H - inset;
        double wh = WALL_H;

        gc.setFill(colLeft);
        gc.fillPolygon(
                new double[]{leftX, leftX, topX, topX},
                new double[]{leftY, leftY - wh, topY - wh, topY}, 4);
        gc.setFill(colRight);
        gc.fillPolygon(
                new double[]{topX, topX, rightX, rightX},
                new double[]{topY, topY - wh, rightY - wh, rightY}, 4);
        gc.setFill(colRoof);
        gc.fillPolygon(
                new double[]{topX, rightX, cx, leftX},
                new double[]{topY - wh, rightY - wh, botY - wh, leftY - wh}, 4);

        gc.setStroke(Color.rgb(0, 0, 0, 0.18));
        gc.setLineWidth(0.6);
        gc.strokePolygon(
                new double[]{leftX, leftX, topX, topX},
                new double[]{leftY, leftY - wh, topY - wh, topY}, 4);
        gc.strokePolygon(
                new double[]{topX, topX, rightX, rightX},
                new double[]{topY, topY - wh, rightY - wh, rightY}, 4);
        gc.strokePolygon(
                new double[]{topX, rightX, cx, leftX},
                new double[]{topY - wh, rightY - wh, botY - wh, leftY - wh}, 4);
    }

    private void drawWaterSurface(GraphicsContext gc, int tx, int ty, int ox, int oy) {
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);
        double hw = TILE_W / 2.0;
        double hh = TILE_H / 2.0;

        gc.setStroke(COL_WATER_ROOF);
        gc.setLineWidth(1.0);
        gc.strokeLine(cx - hw * 0.5, cy + hh * 0.5, cx + hw * 0.5, cy + hh * 0.5);
        gc.strokeLine(cx - hw * 0.3, cy + hh * 0.8, cx + hw * 0.3, cy + hh * 0.8);

        gc.setStroke(COL_WATER_LEFT);
        gc.setLineWidth(0.8);
        gc.strokePolygon(diamondXs(tx, ty, ox), diamondYs(tx, ty, oy), 4);
    }

    private void drawForestTree(GraphicsContext gc, int tx, int ty, int ox, int oy) {
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);
        double hw = TILE_W / 2.0 * 0.45;
        double hh = TILE_H / 2.0 * 0.45;

        Random tileRng = new Random((long) tx * 10000 + ty);
        int numTrees = 1 + tileRng.nextInt(4);
        for (int i = 0; i < numTrees; i++) {
            double offsetX = (tileRng.nextDouble() - 0.5) * TILE_W * 0.6;
            double offsetY = (tileRng.nextDouble() - 0.5) * TILE_H * 0.6;
            double treeCx = cx + offsetX;
            double treeCy = cy + offsetY;

            gc.setFill(Color.rgb(80, 50, 20));
            gc.fillRect(treeCx - 1.5, treeCy + hh - 1, 3, WALL_H * 0.2);

            double treeH = WALL_H * 0.8;
            gc.setFill(COL_FOREST_LEFT);
            gc.fillPolygon(
                    new double[]{treeCx - hw, treeCx, treeCx},
                    new double[]{treeCy + hh, treeCy - treeH, treeCy + hh}, 3);
            gc.setFill(COL_FOREST_ROOF);
            gc.fillPolygon(
                    new double[]{treeCx, treeCx + hw, treeCx},
                    new double[]{treeCy - treeH, treeCy + hh, treeCy + hh}, 3);

            gc.setStroke(Color.rgb(0, 0, 0, 0.20));
            gc.setLineWidth(0.3);
            gc.strokePolygon(
                    new double[]{treeCx - hw, treeCx + hw, treeCx},
                    new double[]{treeCy + hh, treeCy + hh, treeCy - treeH}, 3);
        }
    }
    private void drawDemandBadges(GraphicsContext gc, Game game, int ox, int oy) {
        gc.setFont(Font.font("Monospace", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        for (City city : game.getCities()) {
            CargoType demand = city.getCurrentDemand();
            if (demand == null) continue;
            Position center = city.getCenter();
            double cx = isoScreenX(center.getX(), center.getY(), ox);
            double cy = isoScreenY(center.getX(), center.getY(), oy) - WALL_H - 72;
            String text = "Needs: " + demand.displayName();
            double tw = text.length() * 6.0;
            gc.setFill(Color.rgb(0, 0, 0, 0.55));
            gc.fillRoundRect(cx - tw / 2 - 4, cy - 11, tw + 8, 16, 5, 5);
            gc.setFill(Color.WHITE);
            gc.fillText(text, cx, cy);
        }

        for (Facility facility : game.getFacilities()) {
            CargoType production = facility.getPrimaryProduction();
            if (production == null) continue;
            Position center = facility.getCenter();
            double cx = isoScreenX(center.getX(), center.getY(), ox);
            double cy = isoScreenY(center.getX(), center.getY(), oy) - WALL_H - 38;
            String text = "Produces: " + production.displayName();
            double tw = text.length() * 6.0;
            gc.setFill(Color.rgb(0, 0, 0, 0.55));
            gc.fillRoundRect(cx - tw / 2 - 4, cy - 11, tw + 8, 16, 5, 5);
            gc.setFill(Color.WHITE);
            gc.fillText(text, cx, cy);
        }
    }

    private void drawLabel(GraphicsContext gc, int tx, int ty, String text, int ox, int oy) {
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy) - WALL_H - 54;

        double tw = text.length() * 6.5;
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRoundRect(cx - tw / 2 - 4, cy - 11, tw + 8, 16, 5, 5);

        gc.setFill(Color.WHITE);
        gc.fillText(text, cx, cy);
    }


    private double isoScreenX(int tx, int ty, int ox) {
        return ox + (tx - ty) * (TILE_W / 2.0);
    }

    private double isoScreenY(int tx, int ty, int oy) {
        return oy + (tx + ty) * (TILE_H / 2.0);
    }

    private double isoScreenXd(double tx, double ty, int ox) {
        return ox + (tx - ty) * (TILE_W / 2.0);
    }

    private double isoScreenYd(double tx, double ty, int oy) {
        return oy + (tx + ty) * (TILE_H / 2.0);
    }

    private double[] diamondXs(int tx, int ty, int ox) {
        double cx = isoScreenX(tx, ty, ox);
        return new double[]{cx, cx + TILE_W / 2.0, cx, cx - TILE_W / 2.0};
    }

    private double[] diamondYs(int tx, int ty, int oy) {
        double cy = isoScreenY(tx, ty, oy);
        return new double[]{cy, cy + TILE_H / 2.0, cy + TILE_H, cy + TILE_H / 2.0};
    }

    public int getTileW() {
        return TILE_W;
    }

    public int getTileH() {
        return TILE_H;
    }

    public int[] screenToTile(double screenX, double screenY) {
        double dx = screenX - storedOriginX;
        double dy = screenY - storedOriginY - TILE_H / 2.0;

        double txRaw = (dx / (TILE_W / 2.0) + dy / (TILE_H / 2.0)) / 2.0;
        double tyRaw = (dy / (TILE_H / 2.0) - dx / (TILE_W / 2.0)) / 2.0;

        int baseX = (int) Math.floor(txRaw);
        int baseY = (int) Math.floor(tyRaw);

        int bestX = baseX;
        int bestY = baseY;
        double bestDistance = Double.MAX_VALUE;

        for (int y = baseY - 1; y <= baseY + 1; y++) {
            for (int x = baseX - 1; x <= baseX + 1; x++) {
                if (pointInsideDiamond(screenX, screenY, x, y)) {
                    return new int[]{x, y};
                }

                double centerX = isoScreenX(x, y, storedOriginX);
                double centerY = isoScreenY(x, y, storedOriginY) + TILE_H / 2.0;
                double distance = Math.hypot(screenX - centerX, screenY - centerY);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        return new int[]{bestX, bestY};
    }

    private boolean pointInsideDiamond(double px, double py, int tx, int ty) {
        double centerX = isoScreenX(tx, ty, storedOriginX);
        double centerY = isoScreenY(tx, ty, storedOriginY) + TILE_H / 2.0;

        double normalized =
                Math.abs(px - centerX) / (TILE_W / 2.0) +
                        Math.abs(py - centerY) / (TILE_H / 2.0);

        return normalized <= 1.0;
    }

    public void redrawTile(int tx, int ty) {
        if (currentGame == null || !currentGame.inBounds(tx, ty)) return;
        Tile tile = currentGame.getTile(tx, ty);
        GraphicsContext gc = getGraphicsContext2D();
        drawGround(gc, tile, tx, ty, storedOriginX, storedOriginY);
        drawStructure(gc, tile, tx, ty, storedOriginX, storedOriginY);
    }

    public void drawRoutePathOverlay(java.util.List<model.Position> path) {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        if (path == null || path.isEmpty()) return;

        for (int i = 0; i < path.size(); i++) {
            model.Position p = path.get(i);
            double[] xs = diamondXs(p.getX(), p.getY(), storedOriginX);
            double[] ys = diamondYs(p.getX(), p.getY(), storedOriginY);

            gc.setFill(Color.rgb(255, 200, 0, 0.40));
            gc.fillPolygon(xs, ys, 4);
            gc.setStroke(Color.rgb(255, 160, 0, 0.90));
            gc.setLineWidth(2.0);
            gc.strokePolygon(xs, ys, 4);

            double cx = isoScreenX(p.getX(), p.getY(), storedOriginX);
            double cy = isoScreenY(p.getX(), p.getY(), storedOriginY) + TILE_H * 0.25;
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Monospace", 10));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(String.valueOf(i + 1), cx, cy);

            if (i < path.size() - 1) {
                model.Position next = path.get(i + 1);
                double x1 = isoScreenX(p.getX(), p.getY(), storedOriginX);
                double y1 = isoScreenY(p.getX(), p.getY(), storedOriginY) + TILE_H * 0.5;
                double x2 = isoScreenX(next.getX(), next.getY(), storedOriginX);
                double y2 = isoScreenY(next.getX(), next.getY(), storedOriginY) + TILE_H * 0.5;
                gc.setStroke(Color.rgb(255, 220, 50, 0.85));
                gc.setLineWidth(2.5);
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }

    public void clearHoverOverlay() {
        overlayCanvas.getGraphicsContext2D()
                .clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
    }

    public void drawHoverOverlay(int tx, int ty, boolean valid) {
        drawHoverOverlay(tx, ty, valid, false);
    }

    public void drawHoverOverlay(int tx, int ty, boolean valid, boolean grey) {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        drawHoverTile(gc, tx, ty, valid, grey);
    }

    public void drawHoverOnTopOfRoute(int tx, int ty, boolean valid) {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        drawHoverTile(gc, tx, ty, valid, false);
    }

    private void drawHoverTile(GraphicsContext gc, int tx, int ty, boolean valid, boolean grey) {
        double[] xs = diamondXs(tx, ty, storedOriginX);
        double[] ys = diamondYs(tx, ty, storedOriginY);
        if (grey) {
            gc.setFill(Color.rgb(180, 180, 180, 0.35));
            gc.fillPolygon(xs, ys, 4);
            gc.setStroke(Color.rgb(160, 160, 160, 0.8));
        } else {
            gc.setFill(valid ? Color.rgb(0, 255, 0, 0.25) : Color.rgb(255, 0, 0, 0.25));
            gc.fillPolygon(xs, ys, 4);
            gc.setStroke(valid ? Color.LIMEGREEN : Color.RED);
        }
        gc.setLineWidth(1.5);
        gc.strokePolygon(xs, ys, 4);
    }
}

