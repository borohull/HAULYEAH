package view.panel;

import controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Game;
import model.Route;
import model.enums.VehicleType;

import java.util.List;

/**
 * GaragePanel — modal window for buying vehicles and assigning them to routes.
 *
 * Shows all 6 vehicle types with their stats.
 * Player picks a route then clicks "Buy" on any vehicle type.
 */
public class GaragePanel {

    private static final String DARK_BG  = "#1e1e2e";
    private static final String CARD_BG  = "#2a2a3c";
    private static final String ACCENT   = "#5a8fd8";
    private static final String TEXT     = "#e0e0f0";
    private static final String SUBTEXT  = "#9090b0";

    private final GameController gameController;
    private final Game           game;

    public GaragePanel(GameController gameController, Game game) {
        this.gameController = gameController;
        this.game           = game;
    }

    /** Opens the garage as a modal window on top of the game. */
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Garage");

        // ── Route selector ────────────────────────────────────────────────────
        List<Route> routes = game.getRoutes();

        Label routeLabel = styledLabel("Assign to route:", SUBTEXT, 12);
        ComboBox<String> routeBox = new ComboBox<>();
        routeBox.setStyle(
            "-fx-background-color:#3a3a50; -fx-text-fill:" + TEXT + ";" +
            "-fx-prompt-text-fill:" + SUBTEXT + "; -fx-font-size:13px;");
        routeBox.setPrefWidth(220);

        if (routes.isEmpty()) {
            routeBox.getItems().add("— No routes yet —");
            routeBox.getSelectionModel().selectFirst();
        } else {
            for (Route r : routes) routeBox.getItems().add(r.getName());
            routeBox.getSelectionModel().selectFirst();
        }

        HBox routeRow = new HBox(10, routeLabel, routeBox);

        // Add an Auto-route button if there are no routes but there are stops
        if (routes.isEmpty() && game.getStops().size() >= 2) {
            Button autoRouteBtn = new Button("+ Auto Route");
            autoRouteBtn.setStyle("-fx-background-color:#4a9c6d; -fx-text-fill:white; -fx-cursor:hand;");
            autoRouteBtn.setOnAction(e -> {
                gameController.onCreateRoute(game.getStops());
                dialog.close();
                new GaragePanel(gameController, game).show(owner); // Reload panel
            });
            routeRow.getChildren().add(autoRouteBtn);
        }

        routeRow.setAlignment(Pos.CENTER_LEFT);
        routeRow.setPadding(new Insets(0, 0, 12, 0));

        // ── Vehicle cards ─────────────────────────────────────────────────────
        Label title = styledLabel("🚗  Garage", TEXT, 20);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        VehicleType[] types = VehicleType.values();
        for (int i = 0; i < types.length; i++) {
            VehicleType t = types[i];
            VBox card = buildCard(t, routeBox, routes, dialog);
            grid.add(card, i % 3, i / 3);
        }

        // ── Close button ──────────────────────────────────────────────────────
        Button closeBtn = new Button("✕  Close");
        closeBtn.setStyle(
            "-fx-background-color:#444460; -fx-text-fill:" + TEXT + ";" +
            "-fx-font-size:13px; -fx-padding:7 20; -fx-background-radius:6; -fx-cursor:hand;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        closeRow.setPadding(new Insets(12, 0, 0, 0));

        // ── Layout ────────────────────────────────────────────────────────────
        VBox root = new VBox(16, title, routeRow, grid, closeRow);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:" + DARK_BG + "; -fx-background-radius:12;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    // ── Card builder ──────────────────────────────────────────────────────────

    private VBox buildCard(VehicleType type, ComboBox<String> routeBox,
                           List<Route> routes, Stage dialog) {

        String emoji = emojiFor(type);
        String colorHex = colorFor(type);

        Label nameLabel = styledLabel(emoji + "  " + formatName(type), TEXT, 13);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label catLabel  = styledLabel("Category: " + type.getCategory(), SUBTEXT, 11);
        Label spdLabel  = styledLabel("Speed:     " + type.getSpeed(), SUBTEXT, 11);
        Label capLabel  = styledLabel("Capacity:  " + type.getCapacity(), SUBTEXT, 11);
        Label costLabel = styledLabel("Maint/min: " + type.getMaintenanceCost(), SUBTEXT, 11);

        Button buyBtn = new Button("Buy");
        buyBtn.setStyle(
            "-fx-background-color:" + colorHex + "; -fx-text-fill:white;" +
            "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 18;" +
            "-fx-background-radius:5; -fx-cursor:hand;");

        buyBtn.setOnAction(e -> {
            // Find the selected route
            int idx = routeBox.getSelectionModel().getSelectedIndex();
            if (!routes.isEmpty() && idx >= 0 && idx < routes.size()) {
                Route route = routes.get(idx);
                // Spawn vehicle at first stop and assign to chosen route
                if (!game.getStops().isEmpty()) {
                    model.Vehicle v = gameController.spawnAndAssign(type, route);
                    System.out.println("[Garage] Bought " + type + " on " + route.getName());
                }
            } else {
                System.out.println("[Garage] No route selected — create stops and use Garage first");
            }
            dialog.close();
        });

        VBox card = new VBox(6, nameLabel, catLabel, spdLabel, capLabel, costLabel, buyBtn);
        card.setPadding(new Insets(14));
        card.setPrefWidth(170);
        card.setStyle(
            "-fx-background-color:" + CARD_BG + ";" +
            "-fx-background-radius:8;" +
            "-fx-border-color:" + colorHex + ";" +
            "-fx-border-width:1.5;" +
            "-fx-border-radius:8;");
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Label styledLabel(String text, String colorHex, double size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + colorHex + "; -fx-font-size:" + size + "px;");
        return l;
    }

    private static String formatName(VehicleType t) {
        return t.name().replace('_', ' ');
    }

    private static String emojiFor(VehicleType t) {
        return switch (t) {
            case CITY_BUS, EXPRESS_BUS   -> "🚌";
            case LOG_TRUCK, FLATBED_TRUCK -> "🚛";
            case FOOD_TRUCK, GOODS_TRUCK  -> "🚚";
        };
    }

    private static String colorFor(VehicleType t) {
        return switch (t.getCategory()) {
            case "Passenger"    -> "#3a7bd5";
            case "Raw material" -> "#c97820";
            default/*Product*/  -> "#2e9e6e";
        };
    }
}
