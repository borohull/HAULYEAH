package view;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Game;
import model.MapGenerator;
import model.Road;
import model.Stop;
import model.Tile;
import model.TileType;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MapDemo extends Application {

    private static final Path SAVE_DIR = Path.of(System.getProperty("user.home"), ".haulyea");
    private static final Path SAVE_FILE = SAVE_DIR.resolve("savegame.txt");

    @Override
    public void start(Stage stage) {
        showMenu(stage);
    }

    private void showMenu(Stage stage){
        javafx.scene.image.Image bgImage = new javafx.scene.image.Image(getClass().getResource("/images/menupic.jpg").toExternalForm());

        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);

        javafx.scene.layout.BackgroundSize backgroundSize =
                new javafx.scene.layout.BackgroundSize(
                        100, 100,
                        true, true,
                        false, true
                );

        javafx.scene.layout.BackgroundImage bg =  new javafx.scene.layout.BackgroundImage(
                bgImage,
                javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                javafx.scene.layout.BackgroundPosition.CENTER,
               backgroundSize
        );


        menu.setBackground(new javafx.scene.layout.Background(bg));
        menu.setPrefSize(900, 650);

        Label title = new Label("Haul Yea!");

        Font arcadeFont = Font.loadFont(
                getClass().getResourceAsStream("/fonts/arcadeclassic.TTF"),
                80
        );

        title.setFont(arcadeFont);

        title.setStyle("-fx-text-fill: #6eb892;" + "-fx-effect: dropshadow(gaussian, rgb(35,51,27), 13, 0.5, 0, 2);");
        title.setTranslateX(15);

        Button newGame = new Button("New Game");
        Button loadGame = new Button("Load Game");
        Button deleteGame = new Button("Delete Game");
        Button exitGame = new Button("Exit Game");

        newGame.setOnAction(e -> showNewGameScreen(stage));
        loadGame.setOnAction(e -> System.out.println("Load game clicked"));
        deleteGame.setOnAction(e -> showDeleteScreen(stage));
        exitGame.setOnAction(e -> stage.close());

        menu.getChildren().addAll(title, newGame, loadGame, deleteGame, exitGame);

        double buttonWidth = 160;

        Button[] buttons = {newGame, loadGame, deleteGame, exitGame};
        for(Button b : buttons) {
            b.setPrefWidth(buttonWidth);
        }


        String buttonStyle =  "-fx-background-color: rgba(30,30,30,0.7);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 20;" +
                "-fx-border-color: rgba(255,255,255,0.2);" +
                "-fx-border-radius: 10;";



        newGame.setStyle(buttonStyle);
        loadGame.setStyle(buttonStyle);
        deleteGame.setStyle(buttonStyle);
        exitGame.setStyle(buttonStyle);

        addHover(newGame);
        addHover(loadGame);
        addHover(deleteGame);
        addHover(exitGame);


        Scene scene = new Scene(menu, 900, 650);
        stage.setTitle("Haul Yea!");
        stage.setScene(scene);
        stage.show();
    }

    private void showNewGameScreen(Stage stage) {

        javafx.scene.image.Image bgImage =
                new javafx.scene.image.Image(getClass().getResource("/images/newGameBack.jpg").toExternalForm());

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(900, 650);
        root.setFillWidth(false);

        javafx.scene.layout.BackgroundSize backgroundSize =
                new javafx.scene.layout.BackgroundSize(
                        100, 100,
                        true, true,
                        false, true
                );

        javafx.scene.layout.BackgroundImage bg =
                new javafx.scene.layout.BackgroundImage(
                        bgImage,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        javafx.scene.layout.BackgroundPosition.CENTER,
                        backgroundSize
                );

        root.setBackground(new javafx.scene.layout.Background(bg));

        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(350);
        panel.setPrefHeight(300);
        panel.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

        panel.setStyle(
                "-fx-background-color: rgba(20,20,20,0.72);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(255,255,255,0.20);" +
                        "-fx-border-radius: 18;" +
                        "-fx-padding: 30;"
        );

        Label title = new Label("Start New Game");
        title.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;"
        );


        Label nameLabel = new Label("World Name");
        nameLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );


        javafx.scene.control.TextField worldInput = new javafx.scene.control.TextField();
        worldInput.setPromptText("Enter world name...");
        worldInput.setMaxWidth(300);
        worldInput.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8;"
        );


        Button menuBtn = new Button("Menu");
        Button startBtn = new Button("Start");

        double buttonWidth = 140;
        menuBtn.setPrefWidth(buttonWidth);
        startBtn.setPrefWidth(buttonWidth);

        String buttonStyle =
                "-fx-background-color: rgba(30,30,30,0.7);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: normal;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-border-color: rgba(255,255,255,0.2);" +
                        "-fx-border-radius: 10;";

        menuBtn.setStyle(buttonStyle);
        startBtn.setStyle(buttonStyle);

        addNewGameHover(menuBtn);
        addNewGameHover(startBtn);


        javafx.scene.layout.HBox buttonRow = new javafx.scene.layout.HBox(20);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(menuBtn, startBtn);


        menuBtn.setOnAction(e -> showMenu(stage));

        startBtn.setOnAction(e -> {
            String worldName = worldInput.getText().trim();

            if (worldName.isEmpty()) {
                worldInput.setStyle(
                        "-fx-font-size: 15px;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 8;" +
                                "-fx-border-color: #e78a8a;" +
                                "-fx-border-radius: 8;"
                );
                return;
            }

            worldInput.setStyle(
                    "-fx-font-size: 15px;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8;"
            );

            showGame(stage, worldName);
        });

        panel.getChildren().addAll(title, nameLabel, worldInput, buttonRow);
        root.getChildren().add(panel);

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Haul Yea!");
        stage.setScene(scene);
        stage.show();
    }



    private void showDeleteScreen(Stage stage){

        javafx.scene.image.Image bgImage =
                new javafx.scene.image.Image(getClass().getResource("/images/menupic.jpg").toExternalForm());

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(900, 650);
        root.setFillWidth(false);

        javafx.scene.layout.BackgroundSize backgroundSize =
                new javafx.scene.layout.BackgroundSize(
                        100, 100,
                        true, true,
                        false, true
                );

        javafx.scene.layout.BackgroundImage bg =
                new javafx.scene.layout.BackgroundImage(
                        bgImage,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        javafx.scene.layout.BackgroundPosition.CENTER,
                        backgroundSize
                );

        root.setBackground(new javafx.scene.layout.Background(bg));

        VBox panel = new VBox(25);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(360);
        panel.setPrefHeight(220);
        panel.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

        panel.setStyle(
                "-fx-background-color: rgba(20,20,20,0.72);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(255,255,255,0.20);" +
                        "-fx-border-radius: 18;" +
                        "-fx-padding: 30;"
        );

        Label question = new Label("Are you sure you want\nto delete progress?");
        question.setWrapText(true);
        question.setMaxWidth(300);
        question.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        question.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 26px;" +
                        "-fx-font-weight: bold;"
        );

        Button noBtn = new Button("NO");
        Button yesBtn = new Button("YES");

        double buttonWidth = 120;
        noBtn.setPrefWidth(buttonWidth);
        yesBtn.setPrefWidth(buttonWidth);

        String buttonStyle =
                "-fx-background-color: rgba(30,30,30,0.7);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 20;" +
                        "-fx-border-color: rgba(255,255,255,0.2);" +
                        "-fx-border-radius: 10;";

        noBtn.setStyle(buttonStyle);
        yesBtn.setStyle(buttonStyle);

        addDeleteHover(noBtn);
        addDeleteHover(yesBtn);

        javafx.scene.layout.HBox buttonRow = new javafx.scene.layout.HBox(30);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(noBtn, yesBtn);

        noBtn.setOnAction(e -> showMenu(stage));

        yesBtn.setOnAction(e -> {
            System.out.println("Delete confirmed");
            showMenu(stage);
        });

        panel.getChildren().addAll(question, buttonRow);
        root.getChildren().add(panel);

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Haul Yea!");
        stage.setScene(scene);
        stage.show();
    }

    private void addHover(Button button) {
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.85);" +
                                "-fx-text-fill: black;" +
                                "-fx-font-size: 14px;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 8 20;" +
                                "-fx-border-color: rgba(255,255,255,0.2);" +
                                "-fx-border-radius: 10;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: rgba(30,30,30,0.7);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 8 20;" +
                                "-fx-border-color: rgba(255,255,255,0.2);" +
                                "-fx-border-radius: 10;"
                )
        );
    }

    private void addDeleteHover(Button button) {
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.9);" +
                                "-fx-text-fill: black;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 20;" +
                                "-fx-border-radius: 10;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: rgba(30,30,30,0.7);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 20;" +
                                "-fx-border-color: rgba(255,255,255,0.2);" +
                                "-fx-border-radius: 10;"
                )
        );
    }

    private void addNewGameHover(Button button) {
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.90);" +
                                "-fx-text-fill: black;" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: normal;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 20;" +
                                "-fx-border-color: rgba(255,255,255,0.2);" +
                                "-fx-border-radius: 10;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: rgba(30,30,30,0.7);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: normal;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 20;" +
                                "-fx-border-color: rgba(255,255,255,0.2);" +
                                "-fx-border-radius: 10;"
                )
        );
    }

    private void showGame(Stage stage, String worldName){
        MapGenerator generator = new MapGenerator();
        Game game = generator.generate(55,55,4,5);
        game.setWorldName(worldName);

        MapPanel mapPanel = new MapPanel();
        mapPanel.drawGame(game);

        // Wrap in Group so ScrollPane respects the scaled size
        Group mapGroup = new Group(mapPanel);

        ScrollPane scroll = new ScrollPane(mapGroup);
        scroll.setPannable(true);

        javafx.application.Platform.runLater(() -> {
            scroll.setHvalue(0.5);
            scroll.setVvalue(0.5);
        });

        scroll.setStyle(
                "-fx-background: #87CEEB;" +
                        "-fx-background-color: #87CEEB;"
        );

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
        TopHud topHud = new TopHud(game.getWorldName());
        BottomToolbar bottomToolbar = new BottomToolbar();

        bottomToolbar.getSelectButton().addEventHandler(javafx.event.ActionEvent.ACTION,
                e -> mapPanel.drawGame(game));
        bottomToolbar.getBuildButton().addEventHandler(javafx.event.ActionEvent.ACTION,
                e -> mapPanel.drawGame(game));
        bottomToolbar.getRemoveButton().addEventHandler(javafx.event.ActionEvent.ACTION,
                e -> mapPanel.drawGame(game));
        bottomToolbar.getSaveButton().setOnAction(e -> saveGame(game));

        //Mouse handlers for road/stop placement and removal
        final int[] roadIdCounter = { 0 };
        final int[] stopIdCounter = { 0 };

        mapPanel.setOnMouseMoved(e -> {
            int[] tile = mapPanel.screenToTile(e.getX(), e.getY());
            int tx = tile[0], ty = tile[1];

            if (!bottomToolbar.isRemoveSelected()
                    && !bottomToolbar.isStopSelected()
                    && bottomToolbar.getSelectedRoadType() == null) return;

            mapPanel.drawGame(game);
            if (game.inBounds(tx, ty)) {
                boolean valid;
                if (bottomToolbar.isRemoveSelected()) {
                    model.Position p = new model.Position(tx, ty);
                    valid = game.getRoadAt(p) != null || game.getStopAt(p) != null;
                } else if (bottomToolbar.isStopSelected()) {
                    model.Tile t = game.getTile(tx, ty);
                    valid = t.getType() == model.TileType.EMPTY
                            && game.isAdjacentToRoadCityOrFacility(new model.Position(tx, ty));
                } else {
                    model.Tile t = game.getTile(tx, ty);
                    valid = t.getType() == model.TileType.EMPTY
                            || t.getType() == model.TileType.FOREST;
                }
                mapPanel.drawHoverOverlay(tx, ty, valid);
            }
        });

        mapPanel.setOnMouseClicked(e -> {
            int[] tile = mapPanel.screenToTile(e.getX(), e.getY());
            int tx = tile[0], ty = tile[1];
            if (game.inBounds(tx, ty)) {
                model.Position p = new model.Position(tx, ty);
                if (bottomToolbar.isRemoveSelected()) {
                    if (game.removeRoad(p) || game.removeStop(p)) {
                        mapPanel.drawGame(game);
                    }
                    return;
                }

                if (bottomToolbar.isStopSelected()) {
                    Stop stop = new Stop("stop-" + (++stopIdCounter[0]), tx, ty,
                            "Stop " + stopIdCounter[0]);
                    if (game.addStop(stop)) {
                        mapPanel.drawGame(game);
                    }
                    return;
                }

                if (bottomToolbar.getSelectedRoadType() == null) return;

                // Use user's selected road type directly - no auto-detection
                Road.RoadType roadType = bottomToolbar.getSelectedRoadType();

                Road road = new Road("road-" + (++roadIdCounter[0]),
                        tx, ty, roadType);
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

    private void saveGame(Game game) {
        StringBuilder content = new StringBuilder();
        content.append("# Haul Yea Save\n");
        content.append("savedAt=")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append('\n');
        content.append("worldName=").append(game.getWorldName()).append('\n');
        content.append("width=").append(game.getWidth()).append('\n');
        content.append("height=").append(game.getHeight()).append('\n');
        content.append("roads=").append(game.getRoads().size()).append('\n');

        content.append("[roadList]\n");
        for (Road road : game.getRoads()) {
            content.append(road.getId()).append(',')
                    .append(road.getPosition().getX()).append(',')
                    .append(road.getPosition().getY()).append(',')
                    .append(road.getType())
                    .append('\n');
        }

        content.append("stops=").append(game.getStops().size()).append('\n');
        content.append("[stopList]\n");
        for (Stop stop : game.getStops()) {
            content.append(stop.getId()).append(',')
                    .append(stop.getPosition().getX()).append(',')
                    .append(stop.getPosition().getY()).append(',')
                    .append(stop.getName())
                    .append('\n');
        }

        content.append("[tileTypes]\n");
        for (int y = 0; y < game.getHeight(); y++) {
            for (int x = 0; x < game.getWidth(); x++) {
                Tile tile = game.getTile(x, y);
                TileType type = tile == null ? TileType.EMPTY : tile.getType();
                content.append(type.name());
                if (x < game.getWidth() - 1) {
                    content.append(',');
                }
            }
            content.append('\n');
        }

        try {
            Files.createDirectories(SAVE_DIR);
            Files.writeString(SAVE_FILE, content);
            System.out.println("Game saved: " + SAVE_FILE);
        } catch (IOException ex) {
            System.err.println("Failed to save game: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
