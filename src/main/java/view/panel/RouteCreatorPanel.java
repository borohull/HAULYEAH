package view.panel;

import controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Game;
import model.Route;
import model.Stop;

import java.util.ArrayList;
import java.util.List;
import view.PopupTheme;
/**
 * RouteCreatorPanel — modal dialog for building a circular route.
 *
 * Left side : all placed stops (click to add to route)
 * Right side: current route order (click to remove)
 * Bottom    : name field + Create / Cancel buttons
 *
 * The route loops back automatically (A → B → C → A).
 */
public class RouteCreatorPanel {

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final String DARK_BG  = "#1e1e2e";
    private static final String CARD_BG  = "#2a2a3c";
    private static final String ACCENT   = "#5a8fd8";
    private static final String GREEN    = "#4a9c6d";
    private static final String RED      = "#9c4a4a";
    private static final String TEXT     = "#e0e0f0";
    private static final String SUBTEXT  = "#9090b0";

    private final GameController gameController;
    private final Game           game;

    /** Callback fired after a route is successfully created. */
    private Runnable onRouteCreated;

    public RouteCreatorPanel(GameController gameController, Game game) {
        this.gameController = gameController;
        this.game           = game;
    }

    public void setOnRouteCreated(Runnable callback) {
        this.onRouteCreated = callback;
    }

    // ── Public entry point ────────────────────────────────────────────────────

    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Create Route");

        // ── Title ─────────────────────────────────────────────────────────────
        Label title = styledLabel("🗺  Create Route", TEXT, 18);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        // ── Left panel: available stops ───────────────────────────────────────
        Label lblAvail = styledLabel("Available Stops", SUBTEXT, 12);
        lblAvail.setFont(Font.font("System", FontWeight.BOLD, 12));

        ListView<String> availableList = new ListView<>();
        availableList.setStyle(listStyle());
        availableList.setPrefHeight(220);
        availableList.setPrefWidth(200);

        List<Stop> allStops = game.getStops();
        for (Stop s : allStops) {
            availableList.getItems().add(stopLabel(s));
        }

        Label hintAdd = styledLabel("Click a stop to add →", SUBTEXT, 11);

        VBox leftBox = new VBox(8, lblAvail, availableList, hintAdd);
        leftBox.setAlignment(Pos.TOP_LEFT);

        // ── Middle arrows ─────────────────────────────────────────────────────
        Label arrowRight = styledLabel("→", ACCENT, 24);
        Label arrowLeft  = styledLabel("←", RED,   24);
        VBox arrowBox = new VBox(12, arrowRight, arrowLeft);
        arrowBox.setAlignment(Pos.CENTER);
        arrowBox.setPadding(new Insets(0, 8, 0, 8));

        // ── Right panel: selected route order ─────────────────────────────────
        Label lblRoute = styledLabel("Route Order  (A → B → … → A)", SUBTEXT, 12);
        lblRoute.setFont(Font.font("System", FontWeight.BOLD, 12));

        ListView<String> routeList = new ListView<>();
        routeList.setStyle(listStyle());
        routeList.setPrefHeight(220);
        routeList.setPrefWidth(200);

        Label hintRemove = styledLabel("Click a stop to remove ←", SUBTEXT, 11);

        VBox rightBox = new VBox(8, lblRoute, routeList, hintRemove);
        rightBox.setAlignment(Pos.TOP_LEFT);

        // Backing list of Stop objects in the chosen order
        List<Stop> selectedStops = new ArrayList<>();

        // ── Add stop on click ─────────────────────────────────────────────────
        availableList.setOnMouseClicked(e -> {
            int idx = availableList.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            Stop chosen = allStops.get(idx);
            selectedStops.add(chosen);
            refreshRouteList(routeList, selectedStops);
            availableList.getSelectionModel().clearSelection();
        });

        // ── Remove stop on click ──────────────────────────────────────────────
        routeList.setOnMouseClicked(e -> {
            int idx = routeList.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            selectedStops.remove(idx);
            refreshRouteList(routeList, selectedStops);
            routeList.getSelectionModel().clearSelection();
        });

        // ── Route name field ──────────────────────────────────────────────────
        Label lblName = styledLabel("Route name:", SUBTEXT, 12);
        TextField nameField = new TextField("Route " + (game.getRoutes().size() + 1));
        nameField.setStyle(
            "-fx-background-color:#3a3a50; -fx-text-fill:" + TEXT + ";" +
            "-fx-font-size:13px; -fx-background-radius:5; -fx-padding:5 8;");
        nameField.setPrefWidth(180);

        HBox nameRow = new HBox(10, lblName, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        // ── Buttons ───────────────────────────────────────────────────────────
        Button btnCreate = makeButton("✔  Create Route", GREEN);
        Button btnClear  = makeButton("↺  Clear",        "#665522");
        Button btnCancel = makeButton("✕  Cancel",       "#444460");

        btnClear.setOnAction(e -> {
            selectedStops.clear();
            refreshRouteList(routeList, selectedStops);
        });

        btnCancel.setOnAction(e -> dialog.close());

        btnCreate.setOnAction(e -> {
            if (selectedStops.size() < 2) {
                Alert warn = new Alert(Alert.AlertType.WARNING,
                        "A route needs at least 2 stops.");
                warn.setHeaderText(null);
                warn.showAndWait();
                return;
            }
            String name = nameField.getText().trim();
            if (name.isEmpty()) name = "Route " + (game.getRoutes().size() + 1);

            Route created = gameController.onCreateRoute(selectedStops, name);
            if (created != null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        "\"" + created.getName() + "\" created with "
                        + created.getStops().size() + " stops!\n"
                        + "Route loops: " + routeSummary(created));
                info.setHeaderText(null);
                info.showAndWait();
                if (onRouteCreated != null) onRouteCreated.run();
            }
            dialog.close();
        });

        HBox btnRow = new HBox(10, btnCreate, btnClear, btnCancel);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(12, 0, 0, 0));

        // ── Assemble ──────────────────────────────────────────────────────────
        HBox listsRow = new HBox(0, leftBox, arrowBox, rightBox);
        listsRow.setAlignment(Pos.TOP_CENTER);

        VBox root = new VBox(16, title, listsRow, nameRow, btnRow);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:" + DARK_BG + "; -fx-background-radius:12;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Re-renders the right-hand ListView from selectedStops. */
    private void refreshRouteList(ListView<String> list, List<Stop> stops) {
        list.getItems().clear();
        for (int i = 0; i < stops.size(); i++) {
            String arrow = (i < stops.size() - 1) ? "  →  " : "  →  (back to start)";
            list.getItems().add((i + 1) + ".  " + stopLabel(stops.get(i)) + arrow);
        }
    }

    private String stopLabel(Stop s) {
        return s.getName() + "  (" + s.getPosition().getX()
                + ", " + s.getPosition().getY() + ")";
    }

    private String routeSummary(Route r) {
        StringBuilder sb = new StringBuilder();
        for (Stop s : r.getStops()) {
            if (sb.length() > 0) sb.append(" → ");
            sb.append(s.getName());
        }
        sb.append(" → ").append(r.getStops().get(0).getName());
        return sb.toString();
    }

    private static Label styledLabel(String text, String colorHex, double size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + colorHex + "; -fx-font-size:" + size + "px;");
        return l;
    }

    private static Button makeButton(String text, String bg) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color:" + bg + "; -fx-text-fill:white;" +
            "-fx-font-size:12px; -fx-padding:7 18; -fx-background-radius:6; -fx-cursor:hand;");
        return b;
    }

    private static String listStyle() {
        return "-fx-background-color:#2a2a3c; -fx-control-inner-background:#2a2a3c;" +
               "-fx-text-fill:#e0e0f0; -fx-font-size:12px;";
    }
}
