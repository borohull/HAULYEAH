package view.panel;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import model.Bridge;
import model.MapEntity;
import model.Game;
import model.City;
import model.Position;
import model.Stop;
import model.Tile;
import model.TrafficLight;
import model.Vehicle;
import model.enums.Direction;
import model.enums.LightState;
import model.enums.TileType;
import model.enums.VehicleType;
import model.Road;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Random;


public class MapPanel extends Canvas {

    
    private static final int TILE_W = 64;   
    private static final int TILE_H = 32;   
    private static final int WALL_H = 24;   

    private static final int ORIGIN_X = 0;
    private static final int ORIGIN_Y = WALL_H + 20;

    
    
    
    private static final Color COL_EMPTY_TOP  = Color.rgb(140, 185, 110);
    private static final Color COL_ROAD_TOP   = Color.rgb( 90,  90,  90);

    private static final Color COL_CITY_TOP   = Color.rgb( 80, 130, 210);
    private static final Color COL_CITY_LEFT  = Color.rgb( 50,  90, 160);
    private static final Color COL_CITY_RIGHT = Color.rgb( 60, 110, 190);
    private static final Color COL_CITY_ROOF  = Color.rgb(110, 165, 240);

    private static final Color COL_FAC_TOP    = Color.rgb(200, 145,  55);
    private static final Color COL_FAC_LEFT   = Color.rgb(140,  95,  30);
    private static final Color COL_FAC_RIGHT  = Color.rgb(170, 115,  40);
    private static final Color COL_FAC_ROOF   = Color.rgb(230, 175,  90);

    
    private static final Color COL_WATER_TOP   = Color.rgb( 55, 130, 200);
    private static final Color COL_WATER_LEFT  = Color.rgb( 30,  90, 155);
    private static final Color COL_WATER_RIGHT = Color.rgb( 40, 110, 175);
    private static final Color COL_WATER_ROOF  = Color.rgb( 90, 175, 240);

    
    private static final Color COL_FOREST_TOP   = Color.rgb( 45, 120,  55);
    private static final Color COL_FOREST_LEFT  = Color.rgb( 25,  80,  35);
    private static final Color COL_FOREST_RIGHT = Color.rgb( 35, 100,  45);
    private static final Color COL_FOREST_ROOF  = Color.rgb( 75, 160,  85);

    
    private static final Color COL_BRIDGE_TOP   = Color.rgb(180, 160, 120);
    private static final Color COL_BRIDGE_LEFT  = Color.rgb(120, 105,  75);
    private static final Color COL_BRIDGE_RIGHT = Color.rgb(150, 130,  95);
    private static final Color COL_BRIDGE_ROOF  = Color.rgb(210, 195, 155);

    private static final Color COL_STOP_TOP = Color.rgb(220,  80,  80);
    private static final Color COL_CITY_ROAD = Color.rgb(90, 90, 90);
    private static final Color COL_GRID     = Color.rgb(0, 0, 0, 0.08);
    private static final Color COL_LABEL    = Color.rgb(0, 0, 0, 0.55);

    private final Image grassImage       = loadTileImage("/images/grass.png");
    private final Image waterImage       = loadTileImage("/images/water.png");
    private final Image fieldImage       = loadTileImage("/images/field.png");
    private final Image plantationImage  = loadTileImage("/images/plantation.png");
    private final Image stopImage        = loadTileImage("/images/stop.png");
    private final Image roadHorImage     = loadTileImage("/images/roadHor.png");
    private final Image roadVertImage    = loadTileImage("/images/roadVert.png");
    // ── Vehicle sprites — one per VehicleType ────────────────────────────────
    private final Image cityBusImage     = loadTileImage("/images/vehicles/city_bus.png");
    private final Image expressBusImage  = loadTileImage("/images/vehicles/express_bus.png");
    private final Image logTruckImage    = loadTileImage("/images/vehicles/log_truck.png");
    private final Image flatbedImage     = loadTileImage("/images/vehicles/flatbed_truck.png");
    private final Image foodTruckImage   = loadTileImage("/images/vehicles/food_truck.png");
    private final Image goodsTruckImage  = loadTileImage("/images/vehicles/goods_truck.png");

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
    private int  storedOriginX;
    private int  storedOriginY;

    private final Random rng = new Random();
    private final javafx.scene.canvas.Canvas overlayCanvas = new javafx.scene.canvas.Canvas();

    public javafx.scene.canvas.Canvas getOverlayCanvas() { return overlayCanvas; }

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

        this.currentGame   = game;
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

        // ── Draw traffic lights ───────────────────────────────────────────────
        for (TrafficLight tl : game.getTrafficLights().values()) {
            drawTrafficLight(gc, tl, originX, originY);
        }

        // ── Draw vehicles ────────────────────────────────────────────────────
        for (Vehicle vehicle : game.getVehicles()) {
            drawVehicle(gc, vehicle, originX, originY);
        }
    }

    /**
     * Draws one small traffic light at each corner of the junction diamond,
     * one per active direction. The corner that matches the current green
     * direction shows a bright green bulb; all others show red.
     */
    private void drawTrafficLight(GraphicsContext gc, TrafficLight tl, int ox, int oy) {
        int tx = tl.getPosition().getX();
        int ty = tl.getPosition().getY();

        double isoX = isoScreenX(tx, ty, ox);
        double isoY = isoScreenY(tx, ty, oy);

        Direction curGreen = tl.getCurrentGreenDirection();
        List<Direction> active = tl.getActiveDirections();

        // Determine overall green/red using the 50 % phase-timer threshold
        double phaseDuration = curGreen != null ? tl.getGreenDuration(curGreen) : 10.0;
        boolean phaseIsGreen = tl.getPhaseTimer() > phaseDuration * 0.50;

        for (Direction dir : active) {
            // Each road arm connects to the junction along one EDGE of the diamond.
            // Place the light at the midpoint of that edge — the true entry point.
            //   NE edge midpoint (NORTH road) = (isoX + TW/4, isoY + TH/4)
            //   SE edge midpoint (EAST road)  = (isoX + TW/4, isoY + 3*TH/4)
            //   SW edge midpoint (SOUTH road) = (isoX - TW/4, isoY + 3*TH/4)
            //   NW edge midpoint (WEST road)  = (isoX - TW/4, isoY + TH/4)
            double cx, cy;
            switch (dir) {
                case NORTH -> { cx = isoX + TILE_W / 4.0;  cy = isoY + TILE_H / 4.0; }
                case EAST  -> { cx = isoX + TILE_W / 4.0;  cy = isoY + TILE_H * 0.75; }
                case SOUTH -> { cx = isoX - TILE_W / 4.0;  cy = isoY + TILE_H * 0.75; }
                case WEST  -> { cx = isoX - TILE_W / 4.0;  cy = isoY + TILE_H / 4.0; }
                default    -> { cx = isoX;                  cy = isoY + TILE_H / 2.0; }
            }
            boolean isGreen = (dir == curGreen) && phaseIsGreen;
            drawMiniLight(gc, cx, cy, isGreen);
        }
    }

    /** Draws a red dot and green dot side-by-side (no pole, no housing). */
    private void drawMiniLight(GraphicsContext gc, double cx, double cy, boolean isGreen) {
        double r = 4.0;          // dot radius
        double gap = 2.0;        // gap between dots
        double totalW = r * 4 + gap;
        double startX = cx - totalW / 2.0;

        // Red dot (left)
        Color redColor = isGreen ? Color.rgb(60, 10, 10) : Color.rgb(255, 40, 40);
        gc.setFill(redColor);
        gc.fillOval(startX, cy - r, r * 2, r * 2);

        // Green dot (right)
        Color greenColor = isGreen ? Color.rgb(30, 215, 70) : Color.rgb(10, 45, 10);
        gc.setFill(greenColor);
        gc.fillOval(startX + r * 2 + gap, cy - r, r * 2, r * 2);

        // Soft glow on the active dot
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
        // Use smooth (interpolated) position for fluid animation between tiles
        double cx = isoScreenXd(vehicle.getSmoothX(), vehicle.getSmoothY(), ox);
        double cy = isoScreenYd(vehicle.getSmoothX(), vehicle.getSmoothY(), oy);

        // Select the correct 2.5D sprite for this vehicle type
        Image img = switch (vehicle.getType()) {
            case CITY_BUS      -> cityBusImage;
            case EXPRESS_BUS   -> expressBusImage;
            case LOG_TRUCK     -> logTruckImage;
            case FLATBED_TRUCK -> flatbedImage;
            case FOOD_TRUCK    -> foodTruckImage;
            case GOODS_TRUCK   -> goodsTruckImage;
        };

        double imgW = TILE_W * 0.95;
        double imgH = imgW * 0.70;
        double imgX = cx - imgW / 2.0;
        double imgY = cy + TILE_H * 0.02 - imgH / 2.0;

        // Determine exact travel direction from the grid path
        Direction travelDir = Direction.EAST; // Default fallback for newly bought vehicles
        if (vehicle.getRoute() != null && vehicle.getRoute().hasTilePath()) {
            java.util.List<Position> path = vehicle.getRoute().getTilePath();
            int curIdx  = vehicle.getRoutePathIndex();
            int nextIdx = (curIdx + 1) % path.size();
            Position curPos  = path.get(curIdx);
            Position nextPos = path.get(nextIdx);

            int dx = nextPos.getX() - curPos.getX();
            int dy = nextPos.getY() - curPos.getY();

            if      (dx > 0) travelDir = Direction.EAST;  // moving +X (down-right)
            else if (dx < 0) travelDir = Direction.WEST;  // moving -X (up-left)
            else if (dy > 0) travelDir = Direction.SOUTH; // moving +Y (down-left)
            else if (dy < 0) travelDir = Direction.NORTH; // moving -Y (up-right)

            // Override with Stop orientation if currently at/leaving a stop
            if (currentGame != null) {
                model.Stop currentStop = currentGame.getStopAt(curPos);
                if (currentStop != null) {
                    travelDir = currentStop.getOrientation();
                }
            }
        }

        // In 2.5D isometric: WEST and SOUTH directions travel "left" across the screen.
        // If the sprite natively faces EAST/NORTH (right), we must flip it horizontally.
        boolean flipX = (travelDir == Direction.WEST || travelDir == Direction.SOUTH);


        // Shadow sits directly on the road center
        gc.setFill(Color.rgb(0, 0, 0, 0.22));
        gc.fillOval(cx - imgW * 0.28, cy + TILE_H * 0.18, imgW * 0.56, TILE_H * 0.18);

        if (img != null) {
            if (flipX) {
                // Draw mirrored: start from right edge, use negative width
                gc.drawImage(img, imgX + imgW, imgY, -imgW, imgH);
            } else {
                gc.drawImage(img, imgX, imgY, imgW, imgH);
            }
        } else {
            // Fallback: coloured dot if image failed to load
            boolean isPassenger = vehicle.getType().isPassenger();
            gc.setFill(isPassenger ? Color.rgb(30, 120, 220) : Color.rgb(220, 100, 30));
            gc.fillOval(cx - 8, cy, 16, 10);
        }
    }

    private Image getGroundImage(Tile tile) {
        return switch (tile.getType()) {
            case WATER     -> waterImage;
            case FOREST    -> grassImage;
            case FACILITY  -> fieldImage;
            case CITY, CITY_EMPTY, ROAD, CITY_ROAD -> null;
            case STOP, CITY_STOP -> stopImage != null ? stopImage : plantationImage;
            case BRIDGE    -> waterImage;
            case EMPTY     -> grassImage;
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
        int rowSeed   = Math.floorMod((ty / 2) * 17 + (tx / 4) * 31, available.length);
        int variation = Math.floorMod(tx + ty, 3);
        return available[(rowSeed + variation) % available.length];
    }

    private Image[] getAvailableCityBuildingImages() {
        return java.util.Arrays.stream(cityBuildingImages)
                .filter(java.util.Objects::nonNull)
                .toArray(Image[]::new);
    }

    private void drawGround(GraphicsContext gc, Tile tile,
                            int tx, int ty, int ox, int oy) {
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);

        double[] xs = diamondXs(tx, ty, ox);
        double[] ys = diamondYs(tx, ty, oy);

        TileType type = tile.getType();

        // ── Road tiles: use PNG image instead of color fill ───────────────────
        if (type == TileType.ROAD || type == TileType.CITY_ROAD) {
            Image roadImg = getRoadImage(tile, tx, ty);
            if (roadImg != null) {
                gc.drawImage(roadImg, cx - TILE_W / 2.0, cy - WALL_H, TILE_W, TILE_H + WALL_H * 2);
            } else {
                // Fallback to color fill if PNG missing
                gc.setFill(COL_ROAD_TOP);
                gc.fillPolygon(xs, ys, 4);
            }
            return;
        }

        // ── All other tiles ───────────────────────────────────────────────────
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

    /** Returns the road PNG — single image used for all road tiles. */
    private Image getRoadImage(Tile tile, int tx, int ty) {
        return roadHorImage;
    }


    private Color groundColor(TileType type) {
        return switch (type) {
            case ROAD, CITY_ROAD     -> COL_ROAD_TOP;
            case STOP, CITY_STOP     -> COL_STOP_TOP;
            case CITY, CITY_EMPTY    -> COL_CITY_TOP;
            case FACILITY            -> COL_FAC_TOP;
            case WATER               -> COL_WATER_TOP;
            case FOREST              -> COL_FOREST_TOP;
            case BRIDGE              -> COL_BRIDGE_TOP;
            default                  -> COL_EMPTY_TOP;
        };
    }

    private void drawStructure(GraphicsContext gc, Tile tile,
                               int tx, int ty, int ox, int oy) {
        switch (tile.getType()) {
            case CITY     -> drawCityBuilding(gc, tile, tx, ty, ox, oy);
            case FACILITY -> drawBox(gc, tx, ty, ox, oy, COL_FAC_LEFT, COL_FAC_RIGHT, COL_FAC_ROOF);
            case WATER    -> drawWaterSurface(gc, tx, ty, ox, oy);
            case FOREST   -> drawForestTree(gc, tx, ty, ox, oy);
            case BRIDGE   -> drawBox(gc, tx, ty, ox, oy, COL_BRIDGE_LEFT, COL_BRIDGE_RIGHT, COL_BRIDGE_ROOF);
            case ROAD, CITY_ROAD -> { }
            case STOP, CITY_STOP -> drawStopArrow(gc, tx, ty, ox, oy);
            default -> { }
        }
    }

    private void drawStopArrow(GraphicsContext gc, int tx, int ty, int ox, int oy) {
        if (currentGame == null) return;
        model.Stop stop = currentGame.getStopAt(new model.Position(tx, ty));
        if (stop == null) return;

        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);
        
        // Draw a small white triangle indicating the departure direction
        gc.setFill(Color.rgb(255, 255, 255, 0.7));
        double size = 12.0;

        switch (stop.getOrientation()) {
            case EAST -> gc.fillPolygon(
                    new double[]{cx, cx + size, cx},
                    new double[]{cy - size/2, cy, cy + size/2}, 3);
            case SOUTH -> gc.fillPolygon(
                    new double[]{cx - size/2, cx, cx + size/2},
                    new double[]{cy, cy + size, cy}, 3);
            case WEST -> gc.fillPolygon(
                    new double[]{cx, cx - size, cx},
                    new double[]{cy - size/2, cy, cy + size/2}, 3);
            case NORTH -> gc.fillPolygon(
                    new double[]{cx - size/2, cx, cx + size/2},
                    new double[]{cy, cy - size, cy}, 3);
        }
    }

    private void drawCityBuilding(GraphicsContext gc, Tile tile, int tx, int ty, int ox, int oy) {
        if (tile.getType() != TileType.CITY) return;
        Image img = getCityBuildingImage(tx, ty);
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);
        if (img != null) {
            double drawW = TILE_W * 0.92;
            double drawH = TILE_H + WALL_H + 6;
            gc.drawImage(img, cx - drawW / 2.0, cy - WALL_H - 4, drawW, drawH);
        }
    }

    private void drawBox(GraphicsContext gc, int tx, int ty, int ox, int oy,
                         Color colLeft, Color colRight, Color colRoof) {
        double inset = 6;
        double hw = TILE_W / 2.0 - inset;
        double hh = TILE_H / 2.0 - inset / 2.0;

        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);

        double topX   = cx,      topY   = cy;
        double rightX = cx + hw, rightY = cy + hh;
        double leftX  = cx - hw, leftY  = cy + hh;
        double botY   = cy + TILE_H - inset;
        double wh     = WALL_H;

        gc.setFill(colLeft);
        gc.fillPolygon(
                new double[]{ leftX, leftX,       topX,      topX  },
                new double[]{ leftY, leftY - wh,  topY - wh, topY  }, 4);
        gc.setFill(colRight);
        gc.fillPolygon(
                new double[]{ topX,  topX,       rightX,      rightX },
                new double[]{ topY,  topY - wh,  rightY - wh, rightY }, 4);
        gc.setFill(colRoof);
        gc.fillPolygon(
                new double[]{ topX,      rightX,      cx,       leftX     },
                new double[]{ topY - wh, rightY - wh, botY - wh,leftY - wh }, 4);

        gc.setStroke(Color.rgb(0, 0, 0, 0.18));
        gc.setLineWidth(0.6);
        gc.strokePolygon(
                new double[]{ leftX, leftX,      topX,      topX  },
                new double[]{ leftY, leftY - wh, topY - wh, topY  }, 4);
        gc.strokePolygon(
                new double[]{ topX,  topX,       rightX,      rightX },
                new double[]{ topY,  topY - wh,  rightY - wh, rightY }, 4);
        gc.strokePolygon(
                new double[]{ topX,      rightX,      cx,       leftX     },
                new double[]{ topY - wh, rightY - wh, botY - wh,leftY - wh }, 4);
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
        double cx  = isoScreenX(tx, ty, ox);
        double cy  = isoScreenY(tx, ty, oy);
        double hw  = TILE_W / 2.0 * 0.45;
        double hh  = TILE_H / 2.0 * 0.45;

        // Use a deterministic seed per tile so trees don't change position on redraw
        Random tileRng = new Random((long) tx * 10000 + ty);
        int numTrees = 1 + tileRng.nextInt(4); // 1 to 4
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
                    new double[]{ treeCx - hw, treeCx,       treeCx },
                    new double[]{ treeCy + hh, treeCy - treeH, treeCy + hh }, 3);
            gc.setFill(COL_FOREST_ROOF);
            gc.fillPolygon(
                    new double[]{ treeCx,  treeCx + hw, treeCx },
                    new double[]{ treeCy - treeH, treeCy + hh, treeCy + hh }, 3);

            gc.setStroke(Color.rgb(0, 0, 0, 0.20));
            gc.setLineWidth(0.3);
            gc.strokePolygon(
                    new double[]{ treeCx - hw, treeCx + hw, treeCx },
                    new double[]{ treeCy + hh, treeCy + hh, treeCy - treeH }, 3);
        }
    }

    private void drawLabel(GraphicsContext gc, int tx, int ty, String text,
                           int ox, int oy) {
        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy) - WALL_H - 14;
        double tw = text.length() * 6.5;
        gc.setFill(COL_LABEL);
        gc.fillRoundRect(cx - tw / 2 - 3, cy - 10, tw + 6, 14, 4, 4);
        gc.setFill(Color.WHITE);
        gc.fillText(text, cx, cy);
    }

    // ── Coordinate helpers ────────────────────────────────────────────────────

    private double isoScreenX(int tx, int ty, int ox) {
        return ox + (tx - ty) * (TILE_W / 2.0);
    }

    private double isoScreenY(int tx, int ty, int oy) {
        return oy + (tx + ty) * (TILE_H / 2.0);
    }

    /** Double-precision versions for smooth vehicle interpolation. */
    private double isoScreenXd(double tx, double ty, int ox) {
        return ox + (tx - ty) * (TILE_W / 2.0);
    }

    private double isoScreenYd(double tx, double ty, int oy) {
        return oy + (tx + ty) * (TILE_H / 2.0);
    }

    private double[] diamondXs(int tx, int ty, int ox) {
        double cx = isoScreenX(tx, ty, ox);
        return new double[]{ cx, cx + TILE_W / 2.0, cx, cx - TILE_W / 2.0 };
    }

    private double[] diamondYs(int tx, int ty, int oy) {
        double cy = isoScreenY(tx, ty, oy);
        return new double[]{ cy, cy + TILE_H / 2.0, cy + TILE_H, cy + TILE_H / 2.0 };
    }

    public int getTileW() { return TILE_W; }
    public int getTileH() { return TILE_H; }

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

    /**
     * Redraws only a single tile (ground + structure) to erase a hover overlay
     * without redrawing the entire map.
     */
    public void redrawTile(int tx, int ty) {
        if (currentGame == null || !currentGame.inBounds(tx, ty)) return;
        Tile tile = currentGame.getTile(tx, ty);
        GraphicsContext gc = getGraphicsContext2D();
        drawGround(gc, tile, tx, ty, storedOriginX, storedOriginY);
        drawStructure(gc, tile, tx, ty, storedOriginX, storedOriginY);
    }

    /**
     * Draws the route path the player is currently tracing as a yellow/orange
     * highlight on the overlay canvas.
     */
    public void drawRoutePathOverlay(java.util.List<model.Position> path) {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        if (path == null || path.isEmpty()) return;

        for (int i = 0; i < path.size(); i++) {
            model.Position p = path.get(i);
            double[] xs = diamondXs(p.getX(), p.getY(), storedOriginX);
            double[] ys = diamondYs(p.getX(), p.getY(), storedOriginY);

            // Highlight each tile orange-yellow
            gc.setFill(Color.rgb(255, 200, 0, 0.40));
            gc.fillPolygon(xs, ys, 4);
            gc.setStroke(Color.rgb(255, 160, 0, 0.90));
            gc.setLineWidth(2.0);
            gc.strokePolygon(xs, ys, 4);

            // Draw step number in the center
            double cx = isoScreenX(p.getX(), p.getY(), storedOriginX);
            double cy = isoScreenY(p.getX(), p.getY(), storedOriginY) + TILE_H * 0.25;
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Monospace", 10));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(String.valueOf(i + 1), cx, cy);

            // Draw arrow line to next tile
            if (i < path.size() - 1) {
                model.Position next = path.get(i + 1);
                double x1 = isoScreenX(p.getX(),    p.getY(),    storedOriginX);
                double y1 = isoScreenY(p.getX(),    p.getY(),    storedOriginY) + TILE_H * 0.5;
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

    /**
     * Draws a hover highlight on top of whatever is already on the overlay canvas
     * (used during route drawing to keep the path visible while hovering).
     */
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
