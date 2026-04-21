package view.panel;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.Group;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import model.Game;
import model.Tile;
import model.enums.TileType;

/**
 * MinimapPanel — 2.5D isometric overview of the main map.
 *
 * Tiles are drawn as filled isometric diamonds using the same projection
 * formulas as MapPanel, scaled to fit within a dynamically-sized canvas.
 * The minimap size tracks the parent container so it shrinks and grows with
 * the window. A white rectangle shows the currently visible area; clicking
 * or dragging navigates the main ScrollPane.
 */
public class MinimapPanel extends Canvas {

    /** Fraction of the smaller parent dimension used for the minimap. */
    private static final double SIZE_FRACTION = 0.42;
    private static final int MIN_DIM = 300;
    private static final int MAX_DIM_CAP = 750;

    // Mirrors MapPanel constants
    private static final int MAIN_TW = 64;
    private static final int MAIN_TH = 32;
    private static final int MAIN_OY = 44; // WALL_H(24) + 20

    private final ScrollPane scroll;
    private final Group mapGroup;
    private final Canvas mainCanvas;

    private int maxDim = MAX_DIM_CAP; // current effective cap, updated by bindSize
    private double scale = 1.0;
    private int mmW = 1;
    private int mmH = 1;
    private Game currentGame;

    public MinimapPanel(ScrollPane scroll, Group mapGroup, Canvas mainCanvas) {
        super(1, 1);
        this.scroll = scroll;
        this.mapGroup = mapGroup;
        this.mainCanvas = mainCanvas;

        scroll.hvalueProperty().addListener((obs, o, n) -> redraw());
        scroll.vvalueProperty().addListener((obs, o, n) -> redraw());

        setOnMouseClicked(e -> navigate(e.getX(), e.getY()));
        setOnMouseDragged(e -> navigate(e.getX(), e.getY()));
    }

    /**
     * Bind the minimap size to a parent Region (e.g. the StackPane that
     * contains the scroll pane). The minimap is resized whenever the parent
     * resizes.
     */
    public void bindSize(Region parent) {
        parent.widthProperty().addListener((obs, o, n) -> onParentResized(n.doubleValue(), parent.getHeight()));
        parent.heightProperty().addListener((obs, o, n) -> onParentResized(parent.getWidth(), n.doubleValue()));
    }

    private void onParentResized(double parentW, double parentH) {
        int newMax = (int) Math.min(MAX_DIM_CAP,
                Math.max(MIN_DIM, Math.min(parentW, parentH) * SIZE_FRACTION));
        if (newMax != maxDim) {
            maxDim = newMax;
            if (currentGame != null)
                drawMinimap(currentGame);
        }
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /** Full redraw — call when game state changes. */
    public void drawMinimap(Game game) {
        this.currentGame = game;
        int cols = game.getWidth();
        int rows = game.getHeight();

        double fullW = (cols + rows) * (MAIN_TW / 2.0) + MAIN_TW;
        double fullH = (cols + rows) * (MAIN_TH / 2.0) + MAIN_OY + 60;
        scale = maxDim / Math.max(fullW, fullH);

        mmW = (int) Math.ceil(fullW * scale);
        mmH = (int) Math.ceil(fullH * scale);
        setWidth(mmW + 2);
        setHeight(mmH + 2);

        redraw();
    }

    /** Refresh viewport rect after a zoom change. */
    public void refreshViewport() {
        redraw();
    }

    // ── Core render ──────────────────────────────────────────────────────────

    private void redraw() {
        if (currentGame == null || mmW <= 1)
            return;

        int cols = currentGame.getWidth();
        int rows = currentGame.getHeight();

        double mTW = MAIN_TW * scale;
        double mTH = MAIN_TH * scale;
        double mOX = rows * (mTW / 2.0) + 1;
        double mOY = MAIN_OY * scale + 1;

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#87CEEB"));
        gc.fillRect(0, 0, mmW + 2, mmH + 2);

        // Isometric diamond tiles — same painter's order as MapPanel (y outer, x inner)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Tile t = currentGame.getTile(x, y);
                gc.setFill(tileColor(t != null ? t.getType() : TileType.EMPTY));

                double cx = mOX + (x - y) * (mTW / 2.0);
                double cy = mOY + (x + y) * (mTH / 2.0);

                double[] xs = { cx, cx + mTW / 2.0, cx, cx - mTW / 2.0 };
                double[] ys = { cy, cy + mTH / 2.0, cy + mTH, cy + mTH / 2.0 };
                gc.fillPolygon(xs, ys, 4);
            }
        }

        drawViewportRect(gc, rows);

        gc.setStroke(Color.rgb(255, 255, 255, 0.35));
        gc.setLineWidth(1.0);
        gc.strokeRect(0.5, 0.5, mmW + 1, mmH + 1);
    }

    private void drawViewportRect(GraphicsContext gc, int rows) {
        double zoom    = mapGroup.getScaleX();
        double canvasW = mainCanvas.getWidth();
        double canvasH = mainCanvas.getHeight();
        double vpW     = scroll.getViewportBounds().getWidth()  / zoom;
        double vpH     = scroll.getViewportBounds().getHeight() / zoom;

        double hRange = scroll.getHmax() - scroll.getHmin();
        double vRange = scroll.getVmax() - scroll.getVmin();
        double hFrac  = (hRange > 0) ? (scroll.getHvalue() - scroll.getHmin()) / hRange : 0;
        double vFrac  = (vRange > 0) ? (scroll.getVvalue() - scroll.getVmin()) / vRange : 0;

        double offX = hFrac * Math.max(0, canvasW - vpW);
        double offY = vFrac * Math.max(0, canvasH - vpH);

        // The minimap is a scaled copy of the isometric canvas, so the viewport
        // rectangle in canvas-pixel space maps directly to minimap-pixel space.
        double rX = 1 + offX * scale;
        double rY = 1 + offY * scale;
        double rW = Math.max(2, vpW * scale);
        double rH = Math.max(2, vpH * scale);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeRect(rX, rY, rW, rH);
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private void navigate(double mx, double my) {
        if (currentGame == null || scale <= 0)
            return;

        int rows = currentGame.getHeight();

        double mTW = MAIN_TW * scale;
        double mTH = MAIN_TH * scale;
        double mOX = rows * (mTW / 2.0) + 1;
        double mOY = MAIN_OY * scale + 1;

        // Invert minimap pixel → tile coordinate
        double relX = mx - mOX;
        double relY = my - mOY;
        double tx = (relX / (mTW / 2.0) + relY / (mTH / 2.0)) / 2.0;
        double ty = (relY / (mTH / 2.0) - relX / (mTW / 2.0)) / 2.0;

        // Forward-project tile → isometric canvas pixel
        double originX = rows * (MAIN_TW / 2.0);
        double cx = originX + (tx - ty) * (MAIN_TW / 2.0);
        double cy = MAIN_OY + (tx + ty) * (MAIN_TH / 2.0);

        double zoom = mapGroup.getScaleX();
        double canvasW = mainCanvas.getWidth();
        double canvasH = mainCanvas.getHeight();
        double vpW = scroll.getViewportBounds().getWidth() / zoom;
        double vpH = scroll.getViewportBounds().getHeight() / zoom;

        double scrollX = cx - vpW / 2.0;
        double scrollY = cy - vpH / 2.0;

        double rangeX = Math.max(0, canvasW - vpW);
        double rangeY = Math.max(0, canvasH - vpH);

        double hFrac = (rangeX > 0) ? Math.max(0, Math.min(1, scrollX / rangeX)) : 0;
        double vFrac = (rangeY > 0) ? Math.max(0, Math.min(1, scrollY / rangeY)) : 0;

        scroll.setHvalue(scroll.getHmin() + hFrac * (scroll.getHmax() - scroll.getHmin()));
        scroll.setVvalue(scroll.getVmin() + vFrac * (scroll.getVmax() - scroll.getVmin()));
    }

    // ── Tile colors ──────────────────────────────────────────────────────────

    private static Color tileColor(TileType type) {
        return switch (type) {
            case ROAD, CITY_ROAD -> Color.rgb(120, 120, 120);
            case CITY -> Color.rgb(230, 150, 50);
            case CITY_EMPTY -> Color.rgb(200, 130, 40);
            case FACILITY -> Color.rgb(160, 60, 180);
            case WATER -> Color.rgb(60, 140, 210);
            case FOREST -> Color.rgb(40, 110, 40);
            case BRIDGE -> Color.rgb(140, 100, 60);
            case STOP, CITY_STOP -> Color.rgb(220, 50, 50);
            default -> Color.rgb(100, 170, 60);
        };
    }
}
