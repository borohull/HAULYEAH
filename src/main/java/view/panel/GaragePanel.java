package view.panel;

import controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Game;
import model.Route;
import model.Vehicle;
import model.enums.VehicleType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Modal dialog for vehicle and route management.
 *
 * Three tabs:
 * Purchase — buy a new vehicle
 * Items — assign a vehicle to a route
 * Routes — rename or delete existing routes
 */
public class GaragePanel {

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final String ROOT_STYLE = "-fx-background-color: linear-gradient(to right, #d9edf9, #f7dcc0);" +
            "-fx-border-color: #d8c3a8; -fx-border-width: 1;" +
            "-fx-border-radius: 14; -fx-background-radius: 14;";

    private static final String HEADER_STYLE = "-fx-background-color: rgba(255,255,255,0.55);" +
            "-fx-border-color: rgba(0,0,0,0.08); -fx-border-width: 0 0 1 0;" +
            "-fx-background-radius: 14 14 0 0;";

    private static final String LEFT_PANEL_STYLE = "-fx-background-color: rgba(255,255,255,0.18);" +
            "-fx-border-color: rgba(0,0,0,0.08); -fx-border-width: 0 1 0 0;";

    private static final String RIGHT_PANEL_STYLE = "-fx-background-color: rgba(255,255,255,0.12);";

    private static final String TAB_ACTIVE_STYLE = "-fx-background-color: #b83a3a; -fx-text-fill: white;" +
            "-fx-font-size: 16px; -fx-font-weight: bold;" +
            "-fx-background-radius: 12; -fx-padding: 14 22; -fx-cursor: hand;";

    private static final String TAB_INACTIVE_STYLE = "-fx-background-color: #9aab64; -fx-text-fill: white;" +
            "-fx-font-size: 16px; -fx-font-weight: bold;" +
            "-fx-background-radius: 12; -fx-padding: 14 22; -fx-cursor: hand;";

    private static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: #b83a3a; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-background-radius: 10; -fx-padding: 10 22; -fx-cursor: hand;";

    private static final String SECONDARY_BUTTON_STYLE = "-fx-background-color: #9aab64; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-background-radius: 10; -fx-padding: 10 22; -fx-cursor: hand;";

    private static final String DANGER_BUTTON_STYLE = "-fx-background-color: #b83a3a; -fx-text-fill: white;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;";

    private static final String RENAME_BUTTON_STYLE = "-fx-background-color: #9aab64; -fx-text-fill: white;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;";

    private static final String FIELD_STYLE = "-fx-font-size: 15px;" +
            "-fx-background-color: rgba(255,255,255,0.90);" +
            "-fx-border-color: rgba(0,0,0,0.10);" +
            "-fx-border-radius: 10; -fx-background-radius: 10;";

    private static final String CARD_STYLE = "-fx-background-color: rgba(255,255,255,0.55);" +
            "-fx-border-color: rgba(0,0,0,0.08);" +
            "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12;";

    // ── State ─────────────────────────────────────────────────────────────────
    private final GameController gameController;
    private final Game game;

    public GaragePanel(GameController gameController, Game game) {
        this.gameController = gameController;
        this.game = game;
    }

    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Garage");

        List<Route> drawnRoutes = game.getRoutes().stream()
                .filter(Route::hasTilePath)
                .collect(Collectors.toList());

        // ── Header ────────────────────────────────────────────────────────────
        Label title = new Label("Garage");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #2f2a24;");
        Label subtitle = new Label("Manage your vehicles and routes");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        VBox header = new VBox(4, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(18, 20, 18, 20));
        header.setStyle(HEADER_STYLE);

        // ── Left tab buttons ──────────────────────────────────────────────────
        Button btnPurchase = new Button("Purchase");
        Button btnItems = new Button("Items");
        Button btnRoutes = new Button("Routes");

        for (Button b : List.of(btnPurchase, btnItems, btnRoutes)) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefHeight(56);
        }

        VBox leftPanel = new VBox(14, btnPurchase, btnItems, btnRoutes);
        leftPanel.setPadding(new Insets(26));
        leftPanel.setPrefWidth(210);
        leftPanel.setStyle(LEFT_PANEL_STYLE);

        // ── Tab content views ─────────────────────────────────────────────────
        VBox purchaseView = buildPurchaseView(dialog);
        VBox itemsView = buildItemsView(dialog, drawnRoutes);
        VBox routesView = buildRoutesView(dialog);

        itemsView.setVisible(false);
        itemsView.setManaged(false);
        routesView.setVisible(false);
        routesView.setManaged(false);

        Button closeButton = new Button("Close");
        closeButton.setStyle(SECONDARY_BUTTON_STYLE);
        closeButton.setOnAction(e -> dialog.close());

        VBox rightPanel = new VBox(20, purchaseView, itemsView, routesView, closeButton);
        rightPanel.setPadding(new Insets(26));
        rightPanel.setStyle(RIGHT_PANEL_STYLE);

        // ── Tab switching ─────────────────────────────────────────────────────
        btnPurchase.setStyle(TAB_ACTIVE_STYLE);
        btnItems.setStyle(TAB_INACTIVE_STYLE);
        btnRoutes.setStyle(TAB_INACTIVE_STYLE);

        btnPurchase.setOnAction(e -> {
            setVisible(purchaseView, true);
            setVisible(itemsView, false);
            setVisible(routesView, false);
            btnPurchase.setStyle(TAB_ACTIVE_STYLE);
            btnItems.setStyle(TAB_INACTIVE_STYLE);
            btnRoutes.setStyle(TAB_INACTIVE_STYLE);
        });
        btnItems.setOnAction(e -> {
            setVisible(purchaseView, false);
            setVisible(itemsView, true);
            setVisible(routesView, false);
            btnPurchase.setStyle(TAB_INACTIVE_STYLE);
            btnItems.setStyle(TAB_ACTIVE_STYLE);
            btnRoutes.setStyle(TAB_INACTIVE_STYLE);
        });
        btnRoutes.setOnAction(e -> {
            setVisible(purchaseView, false);
            setVisible(itemsView, false);
            setVisible(routesView, true);
            btnPurchase.setStyle(TAB_INACTIVE_STYLE);
            btnItems.setStyle(TAB_INACTIVE_STYLE);
            btnRoutes.setStyle(TAB_ACTIVE_STYLE);
        });

        // ── Layout ────────────────────────────────────────────────────────────
        HBox content = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);
        root.setStyle(ROOT_STYLE);

        dialog.setScene(new Scene(root, 760, 560));
        dialog.setResizable(true);
        dialog.setMinWidth(680);
        dialog.setMinHeight(520);
        dialog.showAndWait();
    }

    // ── Purchase tab ──────────────────────────────────────────────────────────

    private VBox buildPurchaseView(Stage dialog) {
        Label sectionTitle = new Label("Buy New Vehicle");
        sectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        Label hint = new Label("Choose a vehicle type to add to your fleet.");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #7b6553;");

        Label vehicleLabel = new Label("Vehicle type");
        vehicleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        ComboBox<String> vehicleTypeBox = new ComboBox<>();
        vehicleTypeBox.setPrefWidth(280);
        vehicleTypeBox.setStyle(FIELD_STYLE);
        for (VehicleType type : VehicleType.values()) {
            vehicleTypeBox.getItems().add(formatName(type));
        }
        vehicleTypeBox.getSelectionModel().selectFirst();

        Label categoryLabel = new Label();
        Label speedLabel = new Label();
        Label capacityLabel = new Label();
        Label priceLabel = new Label();
        String statStyle = "-fx-font-size: 14px; -fx-text-fill: #5d4b3d;";
        categoryLabel.setStyle(statStyle);
        speedLabel.setStyle(statStyle);
        capacityLabel.setStyle(statStyle);
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3d3026;");

        Runnable updateStats = () -> {
            int idx = vehicleTypeBox.getSelectionModel().getSelectedIndex();
            if (idx < 0)
                return;
            VehicleType selected = VehicleType.values()[idx];
            categoryLabel.setText("Category: " + selected.getCategory());
            speedLabel.setText("Speed: " + selected.getSpeed());
            capacityLabel.setText("Capacity: " + selected.getCapacity());
            priceLabel.setText("Price: $" + selected.getPurchasePrice());
        };
        updateStats.run();
        vehicleTypeBox.setOnAction(e -> updateStats.run());

        Button buyButton = new Button("Buy Vehicle");
        buyButton.setStyle(PRIMARY_BUTTON_STYLE);
        buyButton.setOnAction(e -> {
            int idx = vehicleTypeBox.getSelectionModel().getSelectedIndex();
            if (idx < 0)
                return;
            Vehicle vehicle = gameController.onBuyVehicleDirect(VehicleType.values()[idx]);
            if (vehicle != null)
                dialog.close();
        });

        VBox statsCard = new VBox(10, categoryLabel, speedLabel, capacityLabel, priceLabel);
        statsCard.setStyle(CARD_STYLE);

        return new VBox(14, sectionTitle, hint, vehicleLabel, vehicleTypeBox, statsCard, buyButton);
    }

    // ── Items tab ─────────────────────────────────────────────────────────────

    private VBox buildItemsView(Stage dialog, List<Route> drawnRoutes) {
        Label sectionTitle = new Label("Vehicles & Routes");
        sectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        Label ownedVehicleLabel = new Label("Owned vehicle");
        ownedVehicleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        ComboBox<String> ownedVehicleBox = new ComboBox<>();
        ownedVehicleBox.setPrefWidth(320);
        ownedVehicleBox.setStyle(FIELD_STYLE);

        List<Vehicle> ownedVehicles = game.getVehicles();
        if (ownedVehicles.isEmpty()) {
            ownedVehicleBox.getItems().add("No vehicles owned");
        } else {
            for (Vehicle v : ownedVehicles) {
                String routeName = v.getRoute() != null ? v.getRoute().getName() : "Unassigned";
                String status = v.isActive() ? "Active" : "Parked";
                ownedVehicleBox.getItems().add(formatName(v.getType()) + " - " + routeName + " (" + status + ")");
            }
            ownedVehicleBox.getSelectionModel().selectFirst();
        }

        Label routeLabel = new Label("Assign to route");
        routeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        ComboBox<String> routeBox = new ComboBox<>();
        routeBox.setPrefWidth(320);
        routeBox.setStyle(FIELD_STYLE);
        if (drawnRoutes.isEmpty()) {
            routeBox.getItems().add("Draw a route first");
        } else {
            for (Route r : drawnRoutes) {
                routeBox.getItems().add(r.getName());
            }
            routeBox.getSelectionModel().selectFirst();
        }

        CheckBox loopBox = new CheckBox("Loop route");

        Button assignButton = new Button("Assign Route");
        assignButton.setStyle(PRIMARY_BUTTON_STYLE);
        assignButton.setOnAction(e -> {
            int vi = ownedVehicleBox.getSelectionModel().getSelectedIndex();
            int ri = routeBox.getSelectionModel().getSelectedIndex();
            if (ownedVehicles.isEmpty() || drawnRoutes.isEmpty())
                return;
            if (vi < 0 || vi >= ownedVehicles.size())
                return;
            if (ri < 0 || ri >= drawnRoutes.size())
                return;
            gameController.onAssignVehicle(ownedVehicles.get(vi), drawnRoutes.get(ri), loopBox.isSelected());
            dialog.close();
        });

        Button deployButton = new Button("Deploy");
        deployButton.setStyle(PRIMARY_BUTTON_STYLE);
        deployButton.setOnAction(e -> {
            int vi = ownedVehicleBox.getSelectionModel().getSelectedIndex();
            if (ownedVehicles.isEmpty() || vi < 0 || vi >= ownedVehicles.size())
                return;
            gameController.onDeployVehicle(ownedVehicles.get(vi));
            dialog.close();
        });

        VBox assignCard = new VBox(12, ownedVehicleLabel, ownedVehicleBox,
                routeLabel, routeBox, loopBox);
        assignCard.setStyle(CARD_STYLE);

        return new VBox(14, sectionTitle, assignCard, assignButton, deployButton);
    }

    // ── Routes tab ────────────────────────────────────────────────────────────

    /**
     * Builds the Routes tab — shows every route with a Rename and Delete button.
     * The list rebuilds itself after each action so the UI stays in sync.
     */
    private VBox buildRoutesView(Stage dialog) {
        Label sectionTitle = new Label("Manage Routes");
        sectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        Label hint = new Label("Rename or delete routes you have drawn.");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #7b6553;");

        // This VBox holds the list of route rows and is rebuilt on every change
        VBox routeList = new VBox(10);
        ScrollPane scroll = new ScrollPane(routeList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(320);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Build / rebuild the list of route rows
        Runnable refreshList = () -> {
            routeList.getChildren().clear();
            List<Route> routes = game.getRoutes().stream()
                    .filter(Route::hasTilePath)
                    .collect(Collectors.toList());

            if (routes.isEmpty()) {
                Label empty = new Label("No routes yet — draw one on the map first.");
                empty.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-padding: 8 0;");
                routeList.getChildren().add(empty);
                return;
            }

            for (Route route : routes) {
                routeList.getChildren().add(buildRouteRow(route, dialog, () -> {
                    // Refresh the list after any rename or delete
                    javafx.application.Platform.runLater(() -> buildRoutesView_refresh(routeList, dialog));
                }));
            }
        };

        refreshList.run();

        // Store refreshList so the row callbacks can trigger it
        routeList.setUserData(refreshList);

        return new VBox(14, sectionTitle, hint, scroll);
    }

    /**
     * Rebuilds the route rows inside an existing routeList VBox.
     * Called after rename or delete so the list stays up to date
     * without closing and reopening the dialog.
     */
    private void buildRoutesView_refresh(VBox routeList, Stage dialog) {
        routeList.getChildren().clear();
        List<Route> routes = game.getRoutes().stream()
                .filter(Route::hasTilePath)
                .collect(Collectors.toList());

        if (routes.isEmpty()) {
            Label empty = new Label("No routes yet — draw one on the map first.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-padding: 8 0;");
            routeList.getChildren().add(empty);
            return;
        }

        for (Route route : routes) {
            routeList.getChildren().add(buildRouteRow(route, dialog,
                    () -> javafx.application.Platform.runLater(
                            () -> buildRoutesView_refresh(routeList, dialog))));
        }
    }

    /**
     * Builds a single row for one route in the Routes tab.
     *
     * Layout: [route name label] [rename text field] [Rename btn] [Delete btn]
     *
     * Rename: player types a new name in the text field and clicks Rename.
     * Delete: player clicks Delete; a confirmation is shown before deletion.
     */
    private HBox buildRouteRow(Route route, Stage dialog, Runnable onChanged) {
        // Route name label
        Label nameLabel = new Label(route.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d3026;");
        nameLabel.setMinWidth(140);

        // Stop count info
        Label infoLabel = new Label(route.getTilePath().size() + " tiles · "
                + route.getStops().size() + " stops");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7b6553;");

        VBox nameBox = new VBox(2, nameLabel, infoLabel);
        nameBox.setMinWidth(160);

        // Rename text field
        TextField renameField = new TextField(route.getName());
        renameField.setPrefWidth(160);
        renameField.setStyle(FIELD_STYLE);

        // Rename button
        Button renameBtn = new Button("Rename");
        renameBtn.setStyle(RENAME_BUTTON_STYLE);
        renameBtn.setOnAction(e -> {
            String newName = renameField.getText().trim();
            if (newName.isEmpty()) {
                renameField.setStyle(FIELD_STYLE +
                        "-fx-border-color: #b83a3a; -fx-border-width: 2;");
                return;
            }
            gameController.renameRoute(route, newName);
            onChanged.run();
        });

        // Delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(DANGER_BUTTON_STYLE);
        deleteBtn.setOnAction(e -> {
            // Confirm before deleting
            javafx.scene.control.Alert confirm = view.PopupTheme.createAlert(
                    dialog,
                    javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "Delete Route",
                    "Delete \"" + route.getName() + "\"?",
                    "Any vehicles assigned to this route will be unassigned.");
            java.util.Optional<javafx.scene.control.ButtonType> result = view.PopupTheme.showAndWait(confirm, dialog);
            if (result.isPresent()
                    && result.get() == javafx.scene.control.ButtonType.OK) {
                gameController.deleteRoute(route);
                onChanged.run();
            }
        });

        HBox row = new HBox(12, nameBox, renameField, renameBtn, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(CARD_STYLE);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        return row;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void setVisible(VBox node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static String formatName(VehicleType t) {
        return t.name().replace('_', ' ');
    }
}