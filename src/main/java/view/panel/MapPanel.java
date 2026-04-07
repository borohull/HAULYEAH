package view.panel;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import model.Bridge;
import model.City;
import model.Facility;
import model.Forest;
import model.Game;
import model.Position;
import model.Stop;
import model.Tile;
import model.TileType;
import model.Road;
import model.WaterBody;
import javafx.scene.image.Image;


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
    private final Image roadHorImage     = loadTileImage("/images/roadHor.png");
    private final Image roadVertImage    = loadTileImage("/images/roadVert.png");
    private final Image stopImage        = loadTileImage("/images/stop.png");
    private final Image crossroadImage   = loadTileImage("/images/crossroad.webp");

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

        for (City city : game.getCities()) {
            Position c = city.getCenter();
            drawLabel(gc, c.getX(), c.getY(), city.getName(), originX, originY);
        }
        for (Facility fac : game.getFacilities()) {
            Position c = fac.getCenter();
            drawLabel(gc, c.getX(), c.getY(), fac.getName(), originX, originY);
        }
        for (WaterBody water : game.getWaterBodies()) {
            Position c = water.getCenter();
            drawLabel(gc, c.getX(), c.getY(), water.getName(), originX, originY);
        }
        for (Forest forest : game.getForests()) {
            Position c = forest.getCenter();
            drawLabel(gc, c.getX(), c.getY(), forest.getName(), originX, originY);
        }
        for (Bridge bridge : game.getBridges()) {
            Position c = bridge.getCenter();
            drawLabel(gc, c.getX(), c.getY(), bridge.getName(), originX, originY);
        }
        for (Stop stop : game.getStops()) {
            Position c = stop.getPosition();
            drawLabel(gc, c.getX(), c.getY(), stop.getName(), originX, originY);
        }
    }

    private Image getGroundImage(Tile tile) {
        return switch (tile.getType()) {
            case WATER     -> waterImage;
            case FOREST    -> grassImage;
            case FACILITY  -> fieldImage;
            case CITY      -> null;
            case CITY_ROAD -> getCityRoadImage(tile.getPosition());
            case ROAD      -> getRoadNetworkImage(tile.getPosition());
            case STOP, CITY_STOP -> stopImage != null ? stopImage : plantationImage;
            case BRIDGE    -> waterImage;
            case EMPTY     -> grassImage;
        };
    }

    private Image getCityRoadImage(Position p) {
        if (currentGame == null) return roadVertImage;
        for (City city : currentGame.getCities()) {
            if (!city.containsPosition(p.getX(), p.getY())) continue;
            if (city.isCrossroad(p))     return crossroadImage != null ? crossroadImage : roadVertImage;
            if (city.isVerticalRoad(p))  return roadHorImage;
            return roadVertImage;
        }
        return roadVertImage;
    }

    private Image getRoadNetworkImage(Position p) {
        Road road = currentGame.getRoadAt(p);
        if (road != null && road.getType() == Road.RoadType.VERTICAL) return roadHorImage;
        return roadVertImage;
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

        gc.setFill(groundColor(tile.getType()));
        gc.fillPolygon(xs, ys, 4);

        Image tileImage = getGroundImage(tile);
        if (tileImage != null) {
            gc.drawImage(tileImage, cx - TILE_W / 2.0, cy, TILE_W, TILE_H + WALL_H);
        }

        if (tile.getType() == TileType.CITY_ROAD) {
            gc.setStroke(Color.rgb(60, 60, 60));
            gc.setLineWidth(1.0);
            gc.strokePolygon(xs, ys, 4);
        }
    }

    private Color groundColor(TileType type) {
        return switch (type) {
            case ROAD, CITY_ROAD     -> COL_ROAD_TOP;
            case STOP, CITY_STOP     -> COL_STOP_TOP;
            case CITY                -> COL_CITY_TOP;
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

        gc.setFill(Color.rgb(80, 50, 20));
        gc.fillRect(cx - 3, cy + hh - 2, 6, WALL_H * 0.3);

        double treeH = WALL_H * 1.1;
        gc.setFill(COL_FOREST_LEFT);
        gc.fillPolygon(
                new double[]{ cx - hw, cx,       cx },
                new double[]{ cy + hh, cy - treeH, cy + hh }, 3);
        gc.setFill(COL_FOREST_ROOF);
        gc.fillPolygon(
                new double[]{ cx,  cx + hw, cx },
                new double[]{ cy - treeH, cy + hh, cy + hh }, 3);

        gc.setStroke(Color.rgb(0, 0, 0, 0.20));
        gc.setLineWidth(0.5);
        gc.strokePolygon(
                new double[]{ cx - hw, cx + hw, cx },
                new double[]{ cy + hh, cy + hh, cy - treeH }, 3);
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
        double dy = screenY - storedOriginY;
        double txRaw = (dx / (TILE_W / 2.0) + dy / (TILE_H / 2.0)) / 2.0;
        double tyRaw = (dy / (TILE_H / 2.0) - dx / (TILE_W / 2.0)) / 2.0;
        return new int[]{ (int) Math.floor(txRaw), (int) Math.floor(tyRaw) };
    }

    public void drawHoverOverlay(int tx, int ty, boolean valid) {
        GraphicsContext gc = getGraphicsContext2D();
        double[] xs = diamondXs(tx, ty, storedOriginX);
        double[] ys = diamondYs(tx, ty, storedOriginY);
        gc.setFill(valid ? Color.rgb(0, 255, 0, 0.25) : Color.rgb(255, 0, 0, 0.25));
        gc.fillPolygon(xs, ys, 4);
        gc.setStroke(valid ? Color.LIMEGREEN : Color.RED);
        gc.setLineWidth(1.5);
        gc.strokePolygon(xs, ys, 4);
    }
}
