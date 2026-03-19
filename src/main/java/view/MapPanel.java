package view;

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
import model.Tile;
import model.TileType;
import model.WaterBody;


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
    private static final Color COL_GRID     = Color.rgb(0, 0, 0, 0.08);
    private static final Color COL_LABEL    = Color.rgb(0, 0, 0, 0.55);

    

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
    }

    
    
    
    private void drawGround(GraphicsContext gc, Tile tile,
                            int tx, int ty, int ox, int oy) {
        double[] xs = diamondXs(tx, ty, ox);
        double[] ys = diamondYs(tx, ty, oy);

        gc.setFill(groundColor(tile.getType()));
        gc.fillPolygon(xs, ys, 4);

        gc.setStroke(COL_GRID);
        gc.setLineWidth(0.5);
        gc.strokePolygon(xs, ys, 4);
    }

    private Color groundColor(TileType type) {
        return switch (type) {
            case ROAD     -> COL_ROAD_TOP;
            case STOP     -> COL_STOP_TOP;
            case CITY     -> COL_CITY_TOP;
            case FACILITY -> COL_FAC_TOP;
            case WATER    -> COL_WATER_TOP;
            case FOREST   -> COL_FOREST_TOP;
            case BRIDGE   -> COL_BRIDGE_TOP;
            default       -> COL_EMPTY_TOP;
        };
    }

    
    
    
    private void drawStructure(GraphicsContext gc, Tile tile,
                               int tx, int ty, int ox, int oy) {
        switch (tile.getType()) {
            case CITY     -> drawBox(gc, tx, ty, ox, oy, COL_CITY_LEFT,   COL_CITY_RIGHT,   COL_CITY_ROOF);
            case FACILITY -> drawBox(gc, tx, ty, ox, oy, COL_FAC_LEFT,    COL_FAC_RIGHT,    COL_FAC_ROOF);
            case WATER    -> drawWaterSurface(gc, tx, ty, ox, oy);
            case FOREST   -> drawForestTree(gc, tx, ty, ox, oy);
            case BRIDGE   -> drawBox(gc, tx, ty, ox, oy, COL_BRIDGE_LEFT, COL_BRIDGE_RIGHT, COL_BRIDGE_ROOF);
            default       -> {  }
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
        
        gc.strokeLine(cx - hw * 0.5, cy + hh * 0.5,
                      cx + hw * 0.5, cy + hh * 0.5);
        gc.strokeLine(cx - hw * 0.3, cy + hh * 0.8,
                      cx + hw * 0.3, cy + hh * 0.8);

        
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
}
