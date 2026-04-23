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
import model.Stop;
import model.Tile;
import model.TrafficLight;
import model.Vehicle;
import model.enums.CargoType;
import model.enums.Direction;
import model.enums.TileType;

import model.enums.VehicleType;

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

    private final Image grassImage       = loadTileImage("/images/grass.png");
    private final Image waterImage       = loadTileImage("/images/water.png");
    private final Image fieldImage       = loadTileImage("/images/field.png");
    private final Image plantationImage  = loadTileImage("/images/plantation.png");
    private final Image stopImage        = loadTileImage("/images/stop.png");
    private final Image roadHorImage     = loadTileImage("/images/roadHor.png");
    private final Image roadVertImage    = loadTileImage("/images/roadVert.png");
    // ── Vehicle sprites — one per VehicleType (supports animation arrays) ────

    private final Image[] foodTruckImages   = loadVehicleAnim("/images/vehicles/food_truck");

    private final Image cityBusEastImage = loadTileImage("/images/vehicles/city_east.png");
    private final Image cityBusWestImage = loadTileImage("/images/vehicles/city_west.png");
    private final Image cityBusNorthImage = loadTileImage("/images/vehicles/city_north.png");
    private final Image cityBusSouthImage = loadTileImage("/images/vehicles/city_south.png");

    private final Image expressBusEastImage = loadTileImage("/images/vehicles/express_east.png");
    private final Image expressBusWestImage = loadTileImage("/images/vehicles/express_west.png");
    private final Image expressBusNorthImage = loadTileImage("/images/vehicles/express_north.png");
    private final Image expressBusSouthImage = loadTileImage("/images/vehicles/express_south.png");

    private final Image flatbedEastImage = loadTileImage("/images/vehicles/flatbed_east.png");
    private final Image flatbedWestImage = loadTileImage("/images/vehicles/flatbed_west.png");
    private final Image flatbedNorthImage = loadTileImage("/images/vehicles/flatbed_north.png");
    private final Image flatbedSouthImage = loadTileImage("/images/vehicles/flatbed_south.png");

    private final Image logEastImage = loadTileImage("/images/vehicles/log_east.png");
    private final Image logWestImage = loadTileImage("/images/vehicles/log_west.png");
    private final Image logNorthImage = loadTileImage("/images/vehicles/log_north.png");
    private final Image logSouthImage = loadTileImage("/images/vehicles/log_south.png");

    private final Image goodsEastImage = loadTileImage("/images/vehicles/goods_east.png");
    private final Image goodsWestImage = loadTileImage("/images/vehicles/goods_west.png");
    private final Image goodsNorthImage = loadTileImage("/images/vehicles/goods_north.png");
    private final Image goodsSouthImage = loadTileImage("/images/vehicles/goods_south.png");


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
    
    private final java.util.Random rng = new java.util.Random();
    
    private java.util.List<model.Position> activeRoutePath = null;
    private int hoverX = -1, hoverY = -1;
    private boolean hoverValid, hoverGrey;

    private final javafx.scene.canvas.Canvas overlayCanvas = new javafx.scene.canvas.Canvas();

    public javafx.scene.canvas.Canvas getOverlayCanvas() {
        return overlayCanvas;
    }

    private boolean staticNeedsRedraw = true;

    public void forceStaticRedraw() {
        this.staticNeedsRedraw = true;
    }

    private Image loadTileImage(String path) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is == null) return null;
            return new Image(is);
        } catch (Exception e) {
            System.out.println("Could not load image: " + path);
            return null;
        }
    }

    private Image[] loadVehicleAnim(String pathBase) {
        java.util.List<Image> frames = new java.util.ArrayList<>();
        int i = 1;
        while (true) {
            Image img = loadTileImage(pathBase + "_" + i + ".png");
            if (img == null) break;
            frames.add(img);
            i++;
        }
        if (frames.isEmpty()) {
            Image fallback = loadTileImage(pathBase + ".png");
            if (fallback != null) {
                frames.add(fallback);
            }
        }
        return frames.toArray(new Image[0]);
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

        if (staticNeedsRedraw) {
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
            if (entity instanceof Bridge) continue;
            Position c = entity.getCenter();
            drawLabel(gc, c.getX(), c.getY(), entity.getName(), originX, originY);
        }
        for (Stop stop : game.getStops()) {
            Position c = stop.getPosition();
            drawLabel(gc, c.getX(), c.getY(), stop.getName(), originX, originY);
        }
        
        staticNeedsRedraw = false;
        } // end of staticNeedsRedraw

        drawDynamic(game);
    }
    
    public void drawDynamic(Game game) {
        if (currentGame == null || game == null) return;
        
        GraphicsContext dgc = overlayCanvas.getGraphicsContext2D();
        dgc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

        int originX = storedOriginX;
        int originY = storedOriginY;

        // Draw Route first so vehicles and hover are above it
        if (activeRoutePath != null && !activeRoutePath.isEmpty()) {
            drawRoutePathDirectly(dgc, activeRoutePath);
        }

        // Hover
        if (hoverX != -1 && hoverY != -1) {
            drawHoverTile(dgc, hoverX, hoverY, hoverValid, hoverGrey);
        }

        for (TrafficLight tl : game.getTrafficLights().values()) {
            drawTrafficLight(dgc, tl, originX, originY);
        }

        drawDemandBadges(dgc, game, originX, originY);

        for (Vehicle vehicle : game.getVehicles()) {
            drawVehicle(dgc, vehicle, originX, originY);
        }
    }

    private void drawTrafficLight(GraphicsContext gc, TrafficLight tl, int ox, int oy) {
        int tx = tl.getPosition().getX();
        int ty = tl.getPosition().getY();

        double isoX = isoScreenX(tx, ty, ox);
        double isoY = isoScreenY(tx, ty, oy);

        Direction curGreen = tl.getCurrentGreenDirection();
        List<Direction> active = tl.getActiveDirections();

        gc.setFill(Color.rgb(55, 55, 55, 0.95));
        gc.fillRoundRect(isoX - 2, isoY - 6, 4, 34, 2, 2);



        gc.setFill(Color.rgb(90, 90, 90, 0.85));
        gc.fillOval(isoX - 4, isoY + 26, 8, 4);




        for (Direction dir : active) {
            double cx;
            double cy;

            switch (dir) {
                case NORTH -> {
                    cx = isoX + TILE_W * 0.18;
                    cy = isoY + TILE_H * 0.18 - 8;
                }
                case EAST -> {
                    cx = isoX + TILE_W * 0.25;
                    cy = isoY + TILE_H * 0.52 - 8;
                }
                case SOUTH -> {
                    cx = isoX - TILE_W * 0.18;
                    cy = isoY + TILE_H * 0.58 - 8;
                }
                case WEST -> {
                    cx = isoX - TILE_W * 0.25;
                    cy = isoY + TILE_H * 0.24 - 8;
                }

                default -> {
                    cx = isoX;
                    cy = isoY + TILE_H * 0.4;
                }
            }

            boolean isGreen = dir == curGreen;
            drawSignalHead(gc, cx, cy, isGreen);
        }
    }

    private void drawSignalHead(GraphicsContext gc, double cx, double cy, boolean isGreen) {
        double boxW = 16;
        double boxH = 10;

        // housing
        gc.setFill(Color.rgb(28, 28, 28, 0.95));
        gc.fillRoundRect(cx - boxW / 2, cy - boxH / 2, boxW, boxH, 4, 4);

        gc.setStroke(Color.rgb(80, 80, 80, 0.9));
        gc.setLineWidth(0.7);
        gc.strokeRoundRect(cx - boxW / 2, cy - boxH / 2, boxW, boxH, 4, 4);

        double r = 3.2;
        double leftX = cx - 4.2;
        double rightX = cx + 4.2;
        double lampY = cy;

        Color redOn = Color.rgb(255, 70, 70);
        Color redOff = Color.rgb(70, 20, 20);
        Color greenOn = Color.rgb(50, 230, 90);
        Color greenOff = Color.rgb(20, 60, 25);

        gc.setFill(isGreen ? redOff : redOn);
        gc.fillOval(leftX - r, lampY - r, r * 2, r * 2);

        gc.setFill(isGreen ? greenOn : greenOff);
        gc.fillOval(rightX - r, lampY - r, r * 2, r * 2);

        // subtle glow only for active lamp
        gc.setGlobalAlpha(0.22);
        if (isGreen) {
            gc.setFill(greenOn);
            gc.fillOval(rightX - 5.5, lampY - 5.5, 11, 11);
        } else {
            gc.setFill(redOn);
            gc.fillOval(leftX - 5.5, lampY - 5.5, 11, 11);
        }
        gc.setGlobalAlpha(1.0);
    }


    private void drawVehicle(GraphicsContext gc, Vehicle vehicle, int ox, int oy) {
        double cx = isoScreenXd(vehicle.getSmoothX(), vehicle.getSmoothY(), ox);
        double cy = isoScreenYd(vehicle.getSmoothX(), vehicle.getSmoothY(), oy);

        Direction travelDir = vehicle.getTravelDirection();
        Image img = null;
        boolean flipX = false;

        if (vehicle.getType() == VehicleType.CITY_BUS) {
            img = switch (travelDir) {
                case EAST -> cityBusEastImage;
                case WEST -> cityBusWestImage;
                case NORTH -> cityBusNorthImage;
                case SOUTH -> cityBusSouthImage;
            };
        } else if (vehicle.getType() == VehicleType.EXPRESS_BUS) {
            img = switch (travelDir) {
                case EAST -> expressBusEastImage;
                case WEST -> expressBusWestImage;
                case NORTH -> expressBusNorthImage;
                case SOUTH -> expressBusSouthImage;
            };
        } else if (vehicle.getType() == VehicleType.ORE_TRUCK || vehicle.getType() == VehicleType.IRON_HAULER) {
            img = switch (travelDir) {
                case EAST -> flatbedEastImage;
                case WEST -> flatbedWestImage;
                case NORTH -> flatbedNorthImage;
                case SOUTH -> flatbedSouthImage;
            };
        } else if (vehicle.getType() == VehicleType.LOG_TRUCK || vehicle.getType() == VehicleType.TIMBER_LORRY) {
            img = switch (travelDir) {
                case EAST -> logEastImage;
                case WEST -> logWestImage;
                case NORTH -> logNorthImage;
                case SOUTH -> logSouthImage;
            };
        } else if (vehicle.getType() == VehicleType.COAL_CART || vehicle.getType() == VehicleType.COAL_TRUCK) {
            img = switch (travelDir) {
                case EAST -> goodsEastImage;
                case WEST -> goodsWestImage;
                case NORTH -> goodsNorthImage;
                case SOUTH -> goodsSouthImage;
            };
        } else {
            Image[] imgs = switch (vehicle.getType()) {
                case OIL_TANKER, FUEL_TRUCK -> foodTruckImages;
                default                     -> null;
            };


            double dist = vehicle.getSmoothX() + vehicle.getSmoothY();

            if (imgs != null && imgs.length > 0) {
                int frameIdx = (int) (dist * 4.0) % imgs.length;
                if (frameIdx < 0) frameIdx += imgs.length;
                img = imgs[frameIdx];
            }

            flipX = (travelDir == Direction.WEST || travelDir == Direction.SOUTH);
        }

        double imgW = TILE_W * 1.30;
        double imgH = img != null && img.getWidth() > 0
                ? imgW * (img.getHeight() / img.getWidth())
                : imgW;

        double pivotX = cx;
        double pivotY = cy - TILE_H * 0.15;

        switch (travelDir) {
            case EAST -> {
                pivotX -= 8;
                pivotY += 2;
            }
            case WEST -> {
                pivotX += 8;
                pivotY -= 2;
            }
            case NORTH -> {
                pivotX += 6;
                pivotY += 4;
            }
            case SOUTH -> {
                pivotX -= 6;
                pivotY -= 4;
            }
        }


        if (img != null) {
            gc.save();
            gc.translate(pivotX, pivotY);
            gc.scale(flipX ? -1 : 1, 1);
            gc.drawImage(img, -imgW / 2.0, -imgH / 2.0, imgW, imgH);
            gc.restore();
        } else {
            boolean isPassenger = vehicle.getType().isPassenger();
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
            case CITY     -> drawCityBuilding(gc, tile, tx, ty, ox, oy);
            case FACILITY -> { } // Facilities are drawn separately in drawGame
            case WATER    -> drawWaterSurface(gc, tx, ty, ox, oy);
            case FOREST   -> drawForestTree(gc, tx, ty, ox, oy);
            case BRIDGE   -> drawBridge(gc, tx, ty, ox, oy);
            case ROAD, CITY_ROAD -> { }
            case STOP, CITY_STOP -> { }
            default -> { }
        }
    }

    private void drawBridge(GraphicsContext gc, int tx, int ty, int ox, int oy) {
        Bridge bridge = currentGame != null ? currentGame.getBridgeAt(new Position(tx, ty)) : null;
        if (bridge == null || bridge.getBridgeType() == null) {
            drawBox(gc, tx, ty, ox, oy, COL_BRIDGE_LEFT, COL_BRIDGE_RIGHT, COL_BRIDGE_ROOF);
            return;
        }

        switch (bridge.getBridgeType()) {
            case WOODEN -> drawBox(gc, tx, ty, ox, oy,
                    Color.rgb(120, 105, 75),
                    Color.rgb(150, 130, 95),
                    Color.rgb(210, 195, 155));
            case STEEL -> drawBox(gc, tx, ty, ox, oy,
                    Color.rgb(88, 98, 110),
                    Color.rgb(118, 130, 145),
                    Color.rgb(160, 176, 194));
            case SUSPENSION -> drawBox(gc, tx, ty, ox, oy,
                    Color.rgb(108, 86, 62),
                    Color.rgb(138, 112, 82),
                    Color.rgb(186, 150, 110));
            default -> drawBox(gc, tx, ty, ox, oy, COL_BRIDGE_LEFT, COL_BRIDGE_RIGHT, COL_BRIDGE_ROOF);
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
        gc.setTextAlign(TextAlignment.CENTER);

        // City demand: primary signal (larger + red)
        for (City city : game.getCities()) {
            CargoType demand = city.getCurrentDemand();
            if (demand == null) continue;
            Position center = city.getCenter();
            double cx = isoScreenX(center.getX(), center.getY(), ox);
            double cy = isoScreenY(center.getX(), center.getY(), oy) - WALL_H - 88;
            String text = "DEMAND: " + demand.displayName();

            gc.setFont(Font.font("System", 16));
            double tw = text.length() * 9.2;
            gc.setFill(Color.rgb(130, 0, 0, 0.88));
            gc.fillRoundRect(cx - tw / 2 - 8, cy - 16, tw + 16, 24, 8, 8);
            gc.setStroke(Color.rgb(255, 200, 200, 0.95));
            gc.setLineWidth(1.8);
            gc.strokeRoundRect(cx - tw / 2 - 8, cy - 16, tw + 16, 24, 8, 8);
            gc.setFill(Color.rgb(255, 70, 70));
            gc.fillText(text, cx, cy + 1);
        }

        // Facility production: secondary signal (smaller)
        for (Facility facility : game.getFacilities()) {
            CargoType production = facility.getPrimaryProduction();
            if (production == null) continue;
            Position center = facility.getCenter();
            double cx = isoScreenX(center.getX(), center.getY(), ox);
            double cy = isoScreenY(center.getX(), center.getY(), oy) - WALL_H - 38;
            String text = "Produces: " + production.displayName();

            gc.setFont(Font.font("Monospace", 12));
            double tw = text.length() * 7.1;
            gc.setFill(Color.rgb(0, 0, 0, 0.58));
            gc.fillRoundRect(cx - tw / 2 - 6, cy - 13, tw + 12, 20, 6, 6);
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
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
        this.activeRoutePath = path;
        drawDynamic(currentGame);
    }

    private void drawRoutePathDirectly(GraphicsContext gc, java.util.List<model.Position> path) {
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
        this.hoverX = -1;
        this.hoverY = -1;
        this.activeRoutePath = null;
        drawDynamic(currentGame);
    }

    public void drawHoverOverlay(int tx, int ty, boolean valid) {
        drawHoverOverlay(tx, ty, valid, false);
    }

    public void drawHoverOverlay(int tx, int ty, boolean valid, boolean grey) {
        this.hoverX = tx;
        this.hoverY = ty;
        this.hoverValid = valid;
        this.hoverGrey = grey;
        drawDynamic(currentGame);
    }

    public void drawHoverOnTopOfRoute(int tx, int ty, boolean valid) {
        drawHoverOverlay(tx, ty, valid, false);
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

