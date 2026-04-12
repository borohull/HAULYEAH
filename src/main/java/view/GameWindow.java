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
import model.enums.TileType;
import model.Tile;
import view.panel.BuildToolbar;
import view.panel.GaragePanel;
import view.panel.HudPanel;
import view.panel.MapPanel;
import model.service.ConstructionService;

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
            topHud.setPaused(!topHud.isPaused());
        });

        topHud.getSpeed1xButton().setOnAction(e -> topHud.setActiveSpeedButton("1x"));
        topHud.getSpeed2xButton().setOnAction(e -> topHud.setActiveSpeedButton("2x"));
        topHud.getSpeed4xButton().setOnAction(e -> topHud.setActiveSpeedButton("4x"));
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

        Group mapGroup = new Group(mapPanel);
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
                double newScale = mapPanel.getScaleX() * factor;
                newScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newScale));
                mapPanel.setScaleX(newScale);
                mapPanel.setScaleY(newScale);
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
            bottomToolbar.selectSelectMode();
            gameController.setBuildMode(GameController.BuildMode.SELECT);
            mapPanel.drawGame(game);
        });

        bottomToolbar.getBuildButton().addEventHandler(javafx.event.ActionEvent.ACTION,
                e -> mapPanel.drawGame(game));

        bottomToolbar.getRemoveButton().setOnAction(e -> {
            gameController.setBuildMode(GameController.BuildMode.DEMOLISH);
            mapPanel.drawGame(game);
        });

        bottomToolbar.getSaveButton().setOnAction(e -> simController.saveGame(0));

        bottomToolbar.getExitButton().setOnAction(e -> handleExit());

        // Garage — opens the vehicle purchase modal
        bottomToolbar.getGarageButton().setOnAction(e ->
                new GaragePanel(gameController, game).show(stage));
        bottomToolbar.getFinanceButton().setOnAction(e ->
                gameController.onOpenFinanceDetails());
    }

    // ── Map mouse wiring ─────────────────────────────────────────────────────

    private void wireMapEvents(Game game) {
        // Hover overlay
        mapPanel.setOnMouseMoved(e -> {
            int[] tile = mapPanel.screenToTile(e.getX(), e.getY());
            int tx = tile[0], ty = tile[1];

            if (!bottomToolbar.isRemoveSelected()
                    && !bottomToolbar.isStopSelected()
                    && !bottomToolbar.isRoadSelected()) return;

            mapPanel.drawGame(game);
            if (game.inBounds(tx, ty)) {
                boolean valid;
                if (bottomToolbar.isRemoveSelected()) {
                    Position p = new Position(tx, ty);
                    valid = game.getRoadAt(p) != null || game.getStopAt(p) != null;
                } else if (bottomToolbar.isStopSelected()) {
                    valid = game.getTile(tx, ty).getType() == TileType.EMPTY
                            && new ConstructionService().isAdjacentToRoadCityOrFacility(game, new Position(tx, ty));
                } else {
                    TileType t = game.getTile(tx, ty).getType();
                    valid = t == TileType.EMPTY || t == TileType.FOREST;
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
            if (bottomToolbar.isRouteSelected()) {
                // Create a route from all placed stops with one click
                if (game.getStops().size() >= 2) {
                    Route r = gameController.onCreateRoute(game.getStops());
                    if (r != null) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                "Route \"" + r.getName() + "\" created with "
                                + r.getStops().size() + " stops!");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                            "Place at least 2 stops first, then click the map to create a route.");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
                bottomToolbar.clearSelection();
                return;
            }
            if (bottomToolbar.isSelectSelected()) {
                Tile selectedTile = game.getTile(tx, ty);
                String info = String.format("Pos: (%d,%d) Type: %s", tx, ty, selectedTile.getType());
                if (selectedTile.getEntityName() != null) {
                    info += " Entity: " + selectedTile.getEntityName();
                }
                topHud.setSelectedTile(info);
                return;
            }
            if (!bottomToolbar.isRoadSelected()) return;

            gameController.setBuildMode(GameController.BuildMode.ROAD);
            gameController.onTileClicked(p);
        });
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
