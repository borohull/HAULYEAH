package view;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Game;
import model.MapGenerator;
import model.Road;

public class MapDemo extends Application {

    @Override
    public void start(Stage stage) {
        MapGenerator generator = new MapGenerator();
        Game game = generator.generate(30, 30, 4, 5);

        MapPanel mapPanel = new MapPanel();
        mapPanel.drawGame(game);

        // Wrap in Group so ScrollPane respects the scaled size
        Group mapGroup = new Group(mapPanel);

        ScrollPane scroll = new ScrollPane(mapGroup);
        scroll.setPannable(true);

        //Zoom
        final double MIN_ZOOM = 0.3;
        final double MAX_ZOOM = 3.0;
        scroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                e.consume();
                double factor = e.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
                double newScale = mapPanel.getScaleX() * factor;
                newScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newScale));
                mapPanel.setScaleX(newScale);
                mapPanel.setScaleY(newScale);
            }
        });

        //UI components
        TopHud topHud = new TopHud();
        BottomToolbar bottomToolbar = new BottomToolbar();

        //Mouse handlers for road placement
        final int[] roadIdCounter = { 0 };

        mapPanel.setOnMouseMoved(e -> {
            if (bottomToolbar.getSelectedRoadType() == null) return;
            int[] tile = mapPanel.screenToTile(e.getX(), e.getY());
            int tx = tile[0], ty = tile[1];
            mapPanel.drawGame(game);
            if (game.inBounds(tx, ty)) {
                model.Tile t = game.getTile(tx, ty);
                boolean valid = t.getType() == model.TileType.EMPTY
                             || t.getType() == model.TileType.FOREST;
                mapPanel.drawHoverOverlay(tx, ty, valid);
            }
        });

        mapPanel.setOnMouseClicked(e -> {
            if (bottomToolbar.getSelectedRoadType() == null) return;
            int[] tile = mapPanel.screenToTile(e.getX(), e.getY());
            int tx = tile[0], ty = tile[1];
            if (game.inBounds(tx, ty)) {
                Road road = new Road("road-" + (++roadIdCounter[0]),
                                     tx, ty, bottomToolbar.getSelectedRoadType());
                if (game.addRoad(road)) {
                    mapPanel.drawGame(game);
                }
            }
        });

        //Root Layout
        BorderPane root = new BorderPane();
        root.setTop(topHud);
        root.setCenter(scroll);
        root.setBottom(bottomToolbar);

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Haul Yea!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
