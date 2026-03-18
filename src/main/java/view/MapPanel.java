package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import model.Game;
import model.Tile;
import model.TileType;
import model.City;
import model.Facility;
import model.Position;

/**
 * 2.5D isometric MapPanel.
 *
 * Coordinate system:
 *   screenX = (tileX - tileY) * (TILE_W / 2)  +  originX
 *   screenY = (tileX + tileY) * (TILE_H / 2)  +  originY
 *
 * Draw order: row-major back-to-front (y outer, x inner)
 * so buildings never incorrectly overlap foreground tiles.
 *
 * Each tile is a diamond (rhombus). Structures draw a box
 * on top: left wall (dark), right wall (mid), roof (light).
 */
public class MapPanel extends Canvas {

    // --- Tile geometry ---
    private static final int TILE_W   = 64;   // full diamond width  (pixels)
    private static final int TILE_H   = 32;   // full diamond height (pixels)
    private static final int WALL_H   = 24;   // height of vertical walls on structures

    private static final int ORIGIN_X = 0;
    private static final int ORIGIN_Y = WALL_H + 20;

    // -----------------------------------------------------------------------
    // Colours
    // -----------------------------------------------------------------------
    private static final Color COL_EMPTY_TOP   = Color.rgb(140, 185, 110);
    private static final Color COL_ROAD_TOP    = Color.rgb( 90,  90,  90);

    private static final Color COL_CITY_TOP    = Color.rgb( 80, 130, 210);
    private static final Color COL_CITY_LEFT   = Color.rgb( 50,  90, 160);
    private static final Color COL_CITY_RIGHT  = Color.rgb( 60, 110, 190);
    private static final Color COL_CITY_ROOF   = Color.rgb(110, 165, 240);

    private static final Color COL_FAC_TOP     = Color.rgb(200, 145,  55);
    private static final Color COL_FAC_LEFT    = Color.rgb(140,  95,  30);
    private static final Color COL_FAC_RIGHT   = Color.rgb(170, 115,  40);
    private static final Color COL_FAC_ROOF    = Color.rgb(230, 175,  90);

    private static final Color COL_STOP_TOP    = Color.rgb(220,  80,  80);
    private static final Color COL_GRID        = Color.rgb(0, 0, 0, 0.08);
    private static final Color COL_LABEL       = Color.rgb(0, 0, 0, 0.55);

    // -----------------------------------------------------------------------

    public MapPanel() {
        super(0, 0);
    }

    /**
     * Call this whenever the model changes.
     * Re-sizes the canvas and redraws everything.
     */
    public void drawGame(Game game) {
        int cols = game.getWidth();
        int rows = game.getHeight();

        double canvasW = (cols + rows) * (TILE_W / 2.0) + TILE_W;
        double canvasH = (cols + rows) * (TILE_H / 2.0) + WALL_H + 60;
        setWidth(canvasW);
        setHeight(canvasH);

        // tile (0,0) top-corner sits at x = rows*(TILE_W/2)
        int originX = rows * (TILE_W / 2) + ORIGIN_X;
        int originY = ORIGIN_Y;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);

        // Pass 1 — ground diamonds (back to front)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                drawGround(gc, game.getTile(x, y), x, y, originX, originY);
            }
        }

        // Pass 2 — structures (overflow upward, must follow ground)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                drawStructure(gc, game.getTile(x, y), x, y, originX, originY);
            }
        }

        // Pass 3 — labels
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
    }

    // -----------------------------------------------------------------------
    // Ground diamond
    // -----------------------------------------------------------------------
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
            default       -> COL_EMPTY_TOP;
        };
    }

    // -----------------------------------------------------------------------
    // 3D box: left wall + right wall + roof
    // -----------------------------------------------------------------------
    private void drawStructure(GraphicsContext gc, Tile tile,
                               int tx, int ty, int ox, int oy) {
        if (tile.getType() != TileType.CITY && tile.getType() != TileType.FACILITY) return;

        boolean isCity = tile.getType() == TileType.CITY;
        Color colLeft  = isCity ? COL_CITY_LEFT  : COL_FAC_LEFT;
        Color colRight = isCity ? COL_CITY_RIGHT  : COL_FAC_RIGHT;
        Color colRoof  = isCity ? COL_CITY_ROOF   : COL_FAC_ROOF;

        double inset = 6;
        double hw  = TILE_W / 2.0 - inset;
        double hh  = TILE_H / 2.0 - inset / 2.0;

        double cx = isoScreenX(tx, ty, ox);
        double cy = isoScreenY(tx, ty, oy);

        // Diamond corners at ground level
        double topX   = cx,      topY   = cy;
        double rightX = cx + hw, rightY = cy + hh;
        double leftX  = cx - hw, leftY  = cy + hh;
        double botY   = cy + TILE_H - inset;

        double wh = WALL_H;

        // Left wall
        gc.setFill(colLeft);
        gc.fillPolygon(
                new double[]{ leftX,      leftX,          topX,           topX  },
                new double[]{ leftY,      leftY  - wh,    topY  - wh,     topY  },
                4
        );

        // Right wall
        gc.setFill(colRight);
        gc.fillPolygon(
                new double[]{ topX,       topX,           rightX,         rightX },
                new double[]{ topY,       topY  - wh,     rightY - wh,    rightY },
                4
        );

        // Roof
        gc.setFill(colRoof);
        gc.fillPolygon(
                new double[]{ topX,       rightX,         cx,             leftX  },
                new double[]{ topY - wh,  rightY - wh,    botY   - wh,    leftY  - wh },
                4
        );

        // Outlines
        gc.setStroke(Color.rgb(0, 0, 0, 0.18));
        gc.setLineWidth(0.6);
        gc.strokePolygon(
                new double[]{ leftX,  leftX,      topX,       topX  },
                new double[]{ leftY,  leftY - wh, topY - wh,  topY  }, 4);
        gc.strokePolygon(
                new double[]{ topX,   topX,       rightX,     rightX },
                new double[]{ topY,   topY - wh,  rightY - wh,rightY }, 4);
        gc.strokePolygon(
                new double[]{ topX,      rightX,      cx,       leftX  },
                new double[]{ topY - wh, rightY - wh, botY - wh,leftY - wh }, 4);
    }

    // -----------------------------------------------------------------------
    // Label above structure
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // Isometric coordinate helpers
    // -----------------------------------------------------------------------

    /** Top-corner X of the diamond for tile (tx, ty). */
    private double isoScreenX(int tx, int ty, int ox) {
        return ox + (tx - ty) * (TILE_W / 2.0);
    }

    /** Top-corner Y of the diamond for tile (tx, ty). */
    private double isoScreenY(int tx, int ty, int oy) {
        return oy + (tx + ty) * (TILE_H / 2.0);
    }

    /** All 4 X coords of the ground diamond: top, right, bottom, left. */
    private double[] diamondXs(int tx, int ty, int ox) {
        double cx = isoScreenX(tx, ty, ox);
        return new double[]{ cx, cx + TILE_W / 2.0, cx, cx - TILE_W / 2.0 };
    }

    /** All 4 Y coords of the ground diamond: top, right, bottom, left. */
    private double[] diamondYs(int tx, int ty, int oy) {
        double cy = isoScreenY(tx, ty, oy);
        return new double[]{ cy, cy + TILE_H / 2.0, cy + TILE_H, cy + TILE_H / 2.0 };
    }

    public int getTileW() { return TILE_W; }
    public int getTileH() { return TILE_H; }
}
