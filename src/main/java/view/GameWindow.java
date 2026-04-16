package view;

import controller.GameController;
import controller.SimulationController;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Game;
import model.Position;
import model.Route;
import model.Tile;
import view.panel.BuildToolbar;
import view.panel.GaragePanel;
import view.panel.HudPanel;
import view.panel.MapPanel;

/**
 * GameWindow — builds and shows the in-game scene.
 *
 * Responsibilities (View only):
 *   - Assemble the BorderPane: MapPanel (center), TopHud (top), BottomToolbar (bottom)
 *   - Set up zoom on the ScrollPane
 *   - Register map hover/click handlers that call GameController
 *   - Register toolbar button handlers that call GameController / SimulationController
 *
 * Must NOT contain any game logic — all decisions go through the controllers.
 */
public class GameWindow {

    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 3.0;

    private final Stage                stage;
    private final GameController       gameController;
    private final SimulationController simController;

    private MapPanel      mapPanel;
    private HudPanel      topHud;
    private BuildToolbar  bottomToolbar;

    public GameWindow(Stage stage,
                      GameController gameController,
                      SimulationController simController) {
        this.stage          = stage;
        this.gameController = gameController;
        this.simController  = simController;
    }

    private void wireHud() {
        topHud.getPauseButton().setOnAction(e -> {
            if (simController.isRunning()) {
                simController.pause();
                topHud.setPaused(true);
            } else {
                simController.play();
                topHud.setPaused(false);
            }
        });

        topHud.getSpeed1xButton().setOnAction(e -> {
            simController.setSpeed(SimulationController.Speed.X1);
            topHud.setActiveSpeedButton("1x");
            topHud.setPaused(false);
        });
        topHud.getSpeed2xButton().setOnAction(e -> {
            simController.setSpeed(SimulationController.Speed.X2);
            topHud.setActiveSpeedButton("2x");
            topHud.setPaused(false);
        });
        topHud.getSpeed4xButton().setOnAction(e -> {
            simController.setSpeed(SimulationController.Speed.X4);
            topHud.setActiveSpeedButton("4x");
            topHud.setPaused(false);
        });
    }

    /**
     * Builds the game scene and shows it on the stage.
     */
    public void show() {
        System.out.println("GameWindow.show() called");
        Game game = gameController.getState().getMap();
        System.out.println("Game loaded, size: " + game.getWidth() + "x" + game.getHeight());

        // Set the simulation controller in game controller for tracking changes
        gameController.setSimulationController(simController);

        // ── Map ──────────────────────────────────────────────────────────────
        mapPanel = new MapPanel();
        System.out.println("MapPanel created");
        mapPanel.drawGame(game);
        System.out.println("Map drawn");

        mapPanel.getOverlayCanvas().setMouseTransparent(true);
        Group mapGroup = new Group(mapPanel, mapPanel.getOverlayCanvas());
        ScrollPane scroll = new ScrollPane(mapGroup);
        scroll.setPannable(true);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setStyle("-fx-background: #87CEEB; -fx-background-color: #87CEEB;");

        // Centre the scroll after layout
        javafx.application.Platform.runLater(() -> {
            scroll.setHvalue(0.8);
            scroll.setVvalue(0.2);
        });

        // Ctrl+scroll to zoom
        scroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                e.consume();
                double factor   = e.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
                double newScale = mapGroup.getScaleX() * factor;
                newScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newScale));
                mapGroup.setScaleX(newScale);
                mapGroup.setScaleY(newScale);
            }
        });


        // ── HUD components ───────────────────────────────────────────────────
        topHud       = new HudPanel(game.getWorldName());
        bottomToolbar = new BuildToolbar();
        topHud.setActiveSpeedButton("1x");

        wireHud();
        // ── Wire toolbar → GameController / SimulationController ─────────────
        wireToolbar(game);

        // ── Wire map mouse events → GameController ───────────────────────────
        wireMapEvents(game);

        // Tell GameController to call mapPanel.drawGame() on any state change
        gameController.setOnStateChanged(() -> mapPanel.drawGame(game));

        // Tell SimulationController to redraw map on every simulation tick (vehicles move)
        simController.setOnStateChanged(() -> mapPanel.drawGame(game));


        // ── Assemble layout ──────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(topHud);
        root.setCenter(scroll);
        root.setBottom(bottomToolbar);

        stage.setTitle("Haul Yea!");
        stage.setScene(new Scene(root, 900, 650));

        // Handle window close (X button)
        stage.setOnCloseRequest(e -> {
            e.consume(); // Don't close yet
            handleExit();
        });

        System.out.println("About to call stage.show()");
        stage.show();
        System.out.println("Stage shown");
    }

    // ── Toolbar wiring ───────────────────────────────────────────────────────

    private void wireToolbar(Game game) {
        bottomToolbar.getSelectButton().setOnAction(e -> {
            if (bottomToolbar.isSelectSelected()) {
                // toggle off — back to normal mode
                bottomToolbar.clearSelection();
                gameController.setBuildMode(GameController.BuildMode.SELECT);
                mapPanel.clearHoverOverlay();
            } else {
                bottomToolbar.selectSelectMode();
                gameController.setBuildMode(GameController.BuildMode.SELECT);
            }
        });

        bottomToolbar.getRemoveButton().setOnAction(e ->
                gameController.setBuildMode(GameController.BuildMode.DEMOLISH));

        bottomToolbar.getSaveButton().setOnAction(e -> simController.saveGame(0));

        bottomToolbar.getExitButton().setOnAction(e -> handleExit());

        // Garage — opens the vehicle purchase modal
        bottomToolbar.getGarageButton().setOnAction(e ->
                new GaragePanel(gameController, game).show(stage));
        bottomToolbar.getFinanceButton().setOnAction(e ->
                gameController.onOpenFinanceDetails());

        // Route — enter tile-by-tile route drawing mode
        bottomToolbar.getRouteButton().setOnAction(e -> {
            bottomToolbar.showRouteDrawBar();
            gameController.startRouteDraw("Route " + (game.getRoutes().size() + 1));
        });

        // Done — finish drawing the route
        bottomToolbar.getDoneRouteButton().setOnAction(e -> {
            Route r = gameController.finishRouteDraw();
            bottomToolbar.hideRouteDrawBar();
            mapPanel.clearHoverOverlay();
            if (r != null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        "Route \"" + r.getName() + "\" created!\n"
                        + r.getTilePath().size() + " tiles, "
                        + r.getStops().size() + " stops.");
                info.setHeaderText(null);
                info.showAndWait();
            }
        });

        // Cancel — discard the route being drawn
        bottomToolbar.getCancelRouteButton().setOnAction(e -> {
            gameController.cancelRouteDraw();
            bottomToolbar.hideRouteDrawBar();
            mapPanel.clearHoverOverlay();
        });
    }

    // ── Map mouse wiring ─────────────────────────────────────────────────────

    private void wireMapEvents(Game game) {
        // Hover — show tile info in HUD + overlay in build mode
        int[] lastHover = {-1, -1};
        mapPanel.setOnMouseMoved(e -> {
            int[] tile = mapPanel.screenToTile(e.getX(), e.getY());
            int tx = tile[0], ty = tile[1];

            // Update HUD label
            if (game.inBounds(tx, ty)) {
                model.Tile t = game.getTile(tx, ty);
                String info = "(" + tx + ", " + ty + ")  " + t.getType();
                if (t.getEntityName() != null) info += "  " + t.getEntityName();
                topHud.setHoverTile(info);
            } else {
                topHud.setHoverTile("—");
            }

            // ── Route-draw mode: redraw full path + hover tile together ──────
            if (gameController.getBuildMode() == GameController.BuildMode.ROUTE_DRAW) {
                if (tx != lastHover[0] || ty != lastHover[1]) {
                    lastHover[0] = tx;
                    lastHover[1] = ty;
                    // Redraw the accumulated path first, then add hover highlight on top
                    mapPanel.drawRoutePathOverlay(gameController.getCurrentRoutePath());
                    if (game.inBounds(tx, ty)) {
                        model.enums.TileType ttype = game.getTile(tx, ty).getType();
                        boolean walkable = ttype == model.enums.TileType.ROAD
                                || ttype == model.enums.TileType.STOP
                                || ttype == model.enums.TileType.CITY_ROAD
                                || ttype == model.enums.TileType.CITY_STOP
                                || ttype == model.enums.TileType.BRIDGE;
                        mapPanel.drawHoverOnTopOfRoute(tx, ty, walkable);
                    }
                }
                return;
            }

            // Draw tile highlight in build mode and select mode
            boolean showOverlay = bottomToolbar.isRoadSelected()
                    || bottomToolbar.isStopSelected()
                    || bottomToolbar.isRemoveSelected()
                    || bottomToolbar.isSelectSelected();
            if (!showOverlay) {
                mapPanel.clearHoverOverlay();
                lastHover[0] = -1; lastHover[1] = -1;
                return;
            }

            if (tx == lastHover[0] && ty == lastHover[1]) return;
            lastHover[0] = tx;
            lastHover[1] = ty;

            if (game.inBounds(tx, ty)) {
                boolean valid;
                if (bottomToolbar.isSelectSelected()) {
                    mapPanel.drawHoverOverlay(tx, ty, true, true);
                    return;
                } else if (bottomToolbar.isRemoveSelected()) {
                    Position p = new Position(tx, ty);
                    valid = game.getRoadAt(p) != null || game.getStopAt(p) != null;
                } else if (bottomToolbar.isStopSelected()) {
                    valid = game.getTile(tx, ty).getType() == model.enums.TileType.EMPTY
                            && new model.service.ConstructionService()
                                .isAdjacentToRoadCityOrFacility(game, new Position(tx, ty));
                } else {
                    model.enums.TileType type = game.getTile(tx, ty).getType();
                    valid = type == model.enums.TileType.EMPTY || type == model.enums.TileType.FOREST;
                }
                mapPanel.drawHoverOverlay(tx, ty, valid);
            }
        });

        // Click — delegate everything to GameController
        mapPanel.setOnMouseClicked(e -> {
            int[] tile = mapPanel.screenToTile(e.getX(), e.getY());
            int tx = tile[0], ty = tile[1];
            if (!game.inBounds(tx, ty)) return;

            Position p = new Position(tx, ty);

            // Route-draw mode: every click adds a tile to the route path
            if (gameController.getBuildMode() == GameController.BuildMode.ROUTE_DRAW) {
                gameController.onTileClicked(p);
                // Redraw full path + hover tile so the new tile immediately shows
                mapPanel.drawRoutePathOverlay(gameController.getCurrentRoutePath());
                lastHover[0] = -1; lastHover[1] = -1; // force hover redraw on next move
                return;
            }

            if (bottomToolbar.isRemoveSelected()) {
                gameController.setBuildMode(GameController.BuildMode.DEMOLISH);
                gameController.onTileClicked(p);
                return;
            }
            if (bottomToolbar.isStopSelected()) {
                gameController.setBuildMode(GameController.BuildMode.STOP);
                gameController.onTileClicked(p);
                return;
            }
            if (bottomToolbar.isSelectSelected()) {
                Tile selectedTile = game.getTile(tx, ty);
                String info = buildTileInfo(game, selectedTile, tx, ty);
                topHud.setSelectedTile(info);
                return;
            }
            if (!bottomToolbar.isRoadSelected()) return;

            gameController.setBuildMode(GameController.BuildMode.ROAD);
            gameController.onTileClicked(p);
        });
    }

    /**
     * Builds a rich info string for the HUD when the player clicks a tile in SELECT mode.
     * - STOP / CITY_STOP: show stop name + which routes pass through it
     * - Tiles occupied by a vehicle: show vehicle type + assigned route
     * - Everything else: tile type + entity name
     */
    private String buildTileInfo(model.Game game, Tile tile, int tx, int ty) {
        model.enums.TileType type = tile.getType();

        // ── Stop tile ────────────────────────────────────────────────────────
        if (type == model.enums.TileType.STOP || type == model.enums.TileType.CITY_STOP) {
            model.Stop stop = game.getStopAt(new Position(tx, ty));
            if (stop != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Stop: ").append(stop.getName());
                // List routes that include this stop
                java.util.List<String> routeNames = new java.util.ArrayList<>();
                for (model.Route r : game.getRoutes()) {
                    for (model.Stop s : r.getStops()) {
                        if (s.getId().equals(stop.getId())) {
                            routeNames.add(r.getName());
                            break;
                        }
                    }
                }
                if (!routeNames.isEmpty()) {
                    sb.append("  |  Routes: ").append(String.join(", ", routeNames));
                } else {
                    sb.append("  |  No routes assigned");
                }
                return sb.toString();
            }
        }

        // ── Vehicle on tile ──────────────────────────────────────────────────
        for (model.Vehicle v : game.getVehicles()) {
            if (v.getPosition().getX() == tx && v.getPosition().getY() == ty) {
                String routeInfo = (v.getRoute() != null)
                        ? v.getRoute().getName()
                        : "No route";
                return "Vehicle: " + v.getType().name().replace('_', ' ')
                        + "  |  Route: " + routeInfo
                        + "  |  Speed: " + v.getSpeed();
            }
        }

        // ── Default ──────────────────────────────────────────────────────────
        String info = String.format("(%d,%d)  %s", tx, ty, type);
        if (tile.getEntityName() != null) info += "  " + tile.getEntityName();
        return info;
    }

    /**
     * Handles exit with warning if there are unsaved changes.
     */
    private void handleExit() {
        if (simController.hasUnsavedChanges()) {
            // Show warning about unsaved changes
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes!");
            alert.setContentText("Would you like to save before exiting?");

            ButtonType btnSave = new ButtonType("Save & Exit");
            ButtonType btnExit = new ButtonType("Exit Without Saving");
            ButtonType btnCancel = ButtonType.CANCEL;

            alert.getButtonTypes().setAll(btnSave, btnExit, btnCancel);

            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == btnSave) {
                    simController.saveGame(0);
                    stage.close();
                } else if (result.get() == btnExit) {
                    stage.close();
                }
            }
        } else {
            // No unsaved changes, ask for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Game");
            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("");

            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                stage.close();
            }
        }
    }
}
