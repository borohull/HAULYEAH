package view;

import controller.MainMenuController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * MainMenuView — builds all main-menu screens (pure UI, no game logic).
 *
 * Screens:
 *   show()              — the main menu (New Game / Load / Delete / Exit)
 *   showNewGame()       — world-name input before starting
 *   showDeleteConfirm() — are-you-sure confirmation before deleting a save
 *
 * All button actions delegate to MainMenuController.
 */
public class MainMenuView {

    // ── shared button style constants ────────────────────────────────────────
    private static final String BTN_DARK =
            "-fx-background-color: rgba(30,30,30,0.7);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 10;";

    private static final String BTN_HOVER =
            "-fx-background-color: rgba(255,255,255,0.85);" +
            "-fx-text-fill: black;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 10;";

    // ────────────────────────────────────────────────────────────────────────

    private final Stage                stage;
    private final MainMenuController   controller;

    public MainMenuView(Stage stage, MainMenuController controller) {
        this.stage      = stage;
        this.controller = controller;
    }

    // ── Main menu ────────────────────────────────────────────────────────────

    /**
     * Builds and shows the main menu screen.
     */
    public void show() {
        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);
        menu.setBackground(loadBg("/images/menupic.jpg"));
        menu.setPrefSize(900, 650);

        // Title
        Label title = new Label("Haul Yea!");
        Font arcadeFont = Font.loadFont(
                getClass().getResourceAsStream("/fonts/arcadeclassic.TTF"), 80);
        title.setFont(arcadeFont);
        title.setStyle(
                "-fx-text-fill: #6eb892;" +
                "-fx-effect: dropshadow(gaussian, rgb(35,51,27), 13, 0.5, 0, 2);");
        title.setTranslateX(15);

        // Buttons
        Button btnNew    = makeBtn("New Game",    "14px");
        Button btnLoad   = makeBtn("Load Game",   "14px");
        Button btnDelete = makeBtn("Delete Game", "14px");
        Button btnExit   = makeBtn("Exit Game",   "14px");

        for (Button b : new Button[]{btnNew, btnLoad, btnDelete, btnExit}) {
            b.setPrefWidth(160);
        }

        btnNew.setOnAction(e    -> controller.newGame());
        btnLoad.setOnAction(e   -> controller.loadGame(0));
        btnDelete.setOnAction(e -> controller.deleteSave(0));
        btnExit.setOnAction(e   -> controller.exit());

        menu.getChildren().addAll(title, btnNew, btnLoad, btnDelete, btnExit);
        setScene(menu);
    }

    // ── New game screen ──────────────────────────────────────────────────────

    /**
     * Builds and shows the new-game setup screen (world-name input).
     */
    public void showNewGame() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(900, 650);
        root.setFillWidth(false);
        root.setBackground(loadBg("/images/newGameBack.jpg"));

        // Panel
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefSize(350, 300);
        panel.setMaxWidth(Region.USE_PREF_SIZE);
        panel.setStyle(
                "-fx-background-color: rgba(20,20,20,0.72);" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.20);" +
                "-fx-border-radius: 18;" +
                "-fx-padding: 30;");

        Label titleLbl = new Label("Start New Game");
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label nameLbl = new Label("World Name");
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        TextField worldInput = new TextField();
        worldInput.setPromptText("Enter world name...");
        worldInput.setMaxWidth(300);
        worldInput.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8;");

        Button btnMenu  = makeBtn("Menu",  "15px");
        Button btnStart = makeBtn("Start", "15px");
        btnMenu.setPrefWidth(140);
        btnStart.setPrefWidth(140);

        btnMenu.setOnAction(e -> show()); // back to main menu

        btnStart.setOnAction(e -> {
            String name = worldInput.getText().trim();
            if (name.isEmpty()) {
                worldInput.setStyle(
                        "-fx-font-size: 15px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8;" +
                        "-fx-border-color: #e78a8a;" +
                        "-fx-border-radius: 8;");
                return;
            }
            worldInput.setStyle(
                    "-fx-font-size: 15px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 8;");
            controller.startGame(name);   // controller handles the navigation
        });

        HBox buttonRow = new HBox(20, btnMenu, btnStart);
        buttonRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(titleLbl, nameLbl, worldInput, buttonRow);
        root.getChildren().add(panel);
        setScene(root);
    }

    // ── Delete confirmation ──────────────────────────────────────────────────

    /**
     * Builds and shows the delete-save confirmation screen.
     *
     * @param slot the save-slot the player wants to delete
     */
    public void showDeleteConfirm(int slot) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(900, 650);
        root.setFillWidth(false);
        root.setBackground(loadBg("/images/menupic.jpg"));

        VBox panel = new VBox(25);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefSize(360, 220);
        panel.setMaxWidth(Region.USE_PREF_SIZE);
        panel.setStyle(
                "-fx-background-color: rgba(20,20,20,0.72);" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.20);" +
                "-fx-border-radius: 18;" +
                "-fx-padding: 30;");

        Label question = new Label("Are you sure you want\nto delete progress?");
        question.setWrapText(true);
        question.setMaxWidth(300);
        question.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        question.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;");

        Button btnNo  = makeBtn("NO",  "18px");
        Button btnYes = makeBtn("YES", "18px");
        btnNo.setStyle(btnNo.getStyle()   + "-fx-font-weight: bold;");
        btnYes.setStyle(btnYes.getStyle() + "-fx-font-weight: bold;");
        btnNo.setPrefWidth(120);
        btnYes.setPrefWidth(120);

        btnNo.setOnAction(e  -> show());                                // back to menu
        btnYes.setOnAction(e -> controller.confirmDeleteSave(slot));    // controller deletes + navigates

        HBox buttonRow = new HBox(30, btnNo, btnYes);
        buttonRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(question, buttonRow);
        root.getChildren().add(panel);
        setScene(root);
    }

    // ── Error screen ────────────────────────────────────────────────────────

    /**
     * Builds and shows an error screen when loading fails.
     */
    public void showLoadError() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(900, 650);
        root.setFillWidth(false);
        root.setBackground(loadBg("/images/menupic.jpg"));

        VBox panel = new VBox(25);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefSize(360, 220);
        panel.setMaxWidth(Region.USE_PREF_SIZE);
        panel.setStyle(
                "-fx-background-color: rgba(20,20,20,0.72);" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.20);" +
                "-fx-border-radius: 18;" +
                "-fx-padding: 30;");

        Label message = new Label("Failed to load game.\nNo save file found or corrupted.");
        message.setWrapText(true);
        message.setMaxWidth(300);
        message.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        message.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;");

        Button btnOk = makeBtn("OK", "18px");
        btnOk.setPrefWidth(120);
        btnOk.setOnAction(e -> show()); // back to menu

        panel.getChildren().addAll(message, btnOk);
        root.getChildren().add(panel);
        setScene(root);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Button makeBtn(String text, String fontSize) {
        Button b = new Button(text);
        String style = BTN_DARK + "-fx-font-size: " + fontSize + "; -fx-padding: 8 20;";
        b.setStyle(style);
        b.setOnMouseEntered(e -> b.setStyle(BTN_HOVER + "-fx-font-size: " + fontSize + "; -fx-padding: 8 20;"));
        b.setOnMouseExited(e  -> b.setStyle(style));
        return b;
    }

    private Background loadBg(String resourcePath) {
        javafx.scene.image.Image img =
                new javafx.scene.image.Image(getClass().getResource(resourcePath).toExternalForm());
        BackgroundSize bgSize = new BackgroundSize(100, 100, true, true, false, true);
        return new Background(new BackgroundImage(img,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, bgSize));
    }

    private void setScene(javafx.scene.Parent root) {
        stage.setTitle("Haul Yea!");
        stage.setScene(new Scene(root, 900, 650));
        stage.show();
    }
}
