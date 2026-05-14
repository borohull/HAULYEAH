package view.panel;

import controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
 * Modal dialog for vehicle management, shown when the player clicks the Garage button.
 *
 * <p>Two tabs are presented:
 * <ul>
 *   <li><b>Purchase</b> — lets the player choose a {@link model.enums.VehicleType} and buy it
 *       via {@link controller.GameController#onBuyVehicleDirect}.</li>
 *   <li><b>Items</b> — lets the player assign an owned vehicle to a drawn route
 *       (with optional looping) or re-deploy a parked vehicle.</li>
 * </ul>
 */
public class GaragePanel {

    private static final String ROOT_STYLE =
            "-fx-background-color: linear-gradient(to right, #d9edf9, #f7dcc0);" +
                    "-fx-border-color: #d8c3a8;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 14;" +
                    "-fx-background-radius: 14;";

    private static final String HEADER_STYLE =
            "-fx-background-color: rgba(255,255,255,0.55);" +
                    "-fx-border-color: rgba(0,0,0,0.08);" +
                    "-fx-border-width: 0 0 1 0;" +
                    "-fx-background-radius: 14 14 0 0;";

    private static final String LEFT_PANEL_STYLE =
            "-fx-background-color: rgba(255,255,255,0.18);" +
                    "-fx-border-color: rgba(0,0,0,0.08);" +
                    "-fx-border-width: 0 1 0 0;";

    private static final String RIGHT_PANEL_STYLE =
            "-fx-background-color: rgba(255,255,255,0.12);";

    private static final String TAB_ACTIVE_STYLE =
            "-fx-background-color: #b83a3a;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 14 22;" +
                    "-fx-cursor: hand;";

    private static final String TAB_INACTIVE_STYLE =
            "-fx-background-color: #9aab64;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 14 22;" +
                    "-fx-cursor: hand;";

    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: #b83a3a;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 10 22;" +
                    "-fx-cursor: hand;";

    private static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: #9aab64;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 10 22;" +
                    "-fx-cursor: hand;";

    private static final String FIELD_STYLE =
            "-fx-font-size: 15px;" +
                    "-fx-background-color: rgba(255,255,255,0.90);" +
                    "-fx-border-color: rgba(0,0,0,0.10);" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;";

    private static final String CARD_STYLE =
            "-fx-background-color: rgba(255,255,255,0.55);" +
                    "-fx-border-color: rgba(0,0,0,0.08);" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 18;";

    private final GameController gameController;
    private final Game game;

    /**
     * Creates a GaragePanel bound to the given controller and world state.
     *
     * @param gameController controller used to buy vehicles and assign routes
     * @param game           current world, used to populate vehicle and route lists
     */
    public GaragePanel(GameController gameController, Game game) {
        this.gameController = gameController;
        this.game = game;
    }

    /**
     * Opens the Garage dialog as an application-modal window.
     *
     * @param owner the owning stage (used for modality and positioning)
     */
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Garage");

        List<Route> drawnRoutes = game.getRoutes().stream()
                .filter(Route::hasTilePath)
                .collect(Collectors.toList());

        Label title = new Label("Garage");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #2f2a24;");
        Label subtitle = new Label("Manage your vehicles");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        VBox header = new VBox(4, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(18, 20, 18, 20));
        header.setStyle(HEADER_STYLE);

        Button btnPurchase = new Button("Purchase");
        Button btnItems = new Button("Items");
        btnPurchase.setMaxWidth(Double.MAX_VALUE);
        btnItems.setMaxWidth(Double.MAX_VALUE);
        btnPurchase.setPrefHeight(56);
        btnItems.setPrefHeight(56);

        VBox leftPanel = new VBox(14, btnPurchase, btnItems);
        leftPanel.setPadding(new Insets(26));
        leftPanel.setPrefWidth(210);
        leftPanel.setStyle(LEFT_PANEL_STYLE);

        VBox purchaseView = buildPurchaseView(dialog);
        VBox itemsView = buildItemsView(dialog, drawnRoutes);

        itemsView.setVisible(false);
        itemsView.setManaged(false);


        Button closeButton = new Button("Close");
        closeButton.setStyle(SECONDARY_BUTTON_STYLE);
        closeButton.setOnAction(e -> dialog.close());

        VBox buttonRow = new VBox(closeButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.setPadding(new Insets(0, 0, 0, 0));

        VBox rightPanel = new VBox(20, purchaseView, itemsView, buttonRow);
        rightPanel.setPadding(new Insets(26));
        rightPanel.setStyle(RIGHT_PANEL_STYLE);


        btnPurchase.setStyle(TAB_ACTIVE_STYLE);
        btnItems.setStyle(TAB_INACTIVE_STYLE);

        btnPurchase.setOnAction(e -> {
            btnPurchase.setStyle(TAB_ACTIVE_STYLE);
            btnItems.setStyle(TAB_INACTIVE_STYLE);
            purchaseView.setVisible(true);
            purchaseView.setManaged(true);
            itemsView.setVisible(false);
            itemsView.setManaged(false);
        });

        btnItems.setOnAction(e -> {
            btnPurchase.setStyle(TAB_INACTIVE_STYLE);
            btnItems.setStyle(TAB_ACTIVE_STYLE);
            purchaseView.setVisible(false);
            purchaseView.setManaged(false);
            itemsView.setVisible(true);
            itemsView.setManaged(true);
        });

        HBox content = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);
        root.setStyle(ROOT_STYLE);

        Scene scene = new Scene(root, 760, 560);

        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setMinWidth(680);
        dialog.setMinHeight(520);
        dialog.showAndWait();
    }

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
            if (idx < 0) return;
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
            if (idx < 0) return;

            VehicleType selectedType = VehicleType.values()[idx];
            Vehicle vehicle = gameController.onBuyVehicleDirect(selectedType);

            if (vehicle != null) {
                System.out.println("[Garage] Bought " + selectedType);
                dialog.close();
            } else {
                System.out.println("[Garage] Failed to buy vehicle");
            }
        });

        VBox statsCard = new VBox(10, categoryLabel, speedLabel, capacityLabel, priceLabel);
        statsCard.setStyle(CARD_STYLE);

        return new VBox(14,
                sectionTitle,
                hint,
                vehicleLabel,
                vehicleTypeBox,
                statsCard,
                buyButton
        );
    }

    private VBox buildItemsView(Stage dialog, List<Route> drawnRoutes) {
        Label sectionTitle = new Label("Vehicles & Routes");
        sectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        // ── Vehicle assignment card ────────────────────────────────────────────
        Label ownedVehicleLabel = new Label("Owned vehicle");
        ownedVehicleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        ComboBox<String> ownedVehicleBox = new ComboBox<>();
        ownedVehicleBox.setPrefWidth(320);
        ownedVehicleBox.setStyle(FIELD_STYLE);

        List<Vehicle> ownedVehicles = game.getVehicles();
        if (ownedVehicles.isEmpty()) {
            ownedVehicleBox.getItems().add("No vehicles owned");
        } else {
            for (Vehicle vehicle : ownedVehicles) {
                String routeName = vehicle.getRoute() != null ? vehicle.getRoute().getName() : "Unassigned";
                String status    = vehicle.isActive() ? "Active" : "Parked";
                ownedVehicleBox.getItems().add(formatName(vehicle.getType()) + " - " + routeName + " (" + status + ")");
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
            for (Route route : drawnRoutes) {
                routeBox.getItems().add(route.getName());
            }
            routeBox.getSelectionModel().selectFirst();
        }

        CheckBox loopBox = new CheckBox("Loop route");

        Button assignButton = new Button("Assign Route");
        assignButton.setStyle(PRIMARY_BUTTON_STYLE);
        assignButton.setOnAction(e -> {
            int vehicleIdx = ownedVehicleBox.getSelectionModel().getSelectedIndex();
            int routeIdx   = routeBox.getSelectionModel().getSelectedIndex();

            if (ownedVehicles.isEmpty() || drawnRoutes.isEmpty()) return;
            if (vehicleIdx < 0 || vehicleIdx >= ownedVehicles.size()) return;
            if (routeIdx < 0 || routeIdx >= drawnRoutes.size()) return;

            Vehicle selectedVehicle = ownedVehicles.get(vehicleIdx);
            Route   selectedRoute   = drawnRoutes.get(routeIdx);
            gameController.onAssignVehicle(selectedVehicle, selectedRoute, loopBox.isSelected());

            System.out.println("[Garage] Assigned " + selectedVehicle.getId()
                    + " to route: " + selectedRoute.getName());
            dialog.close();
        });

        Button deployButton = new Button("Deploy");
        deployButton.setStyle(PRIMARY_BUTTON_STYLE);
        deployButton.setOnAction(e -> {
            int vehicleIdx = ownedVehicleBox.getSelectionModel().getSelectedIndex();
            if (ownedVehicles.isEmpty() || vehicleIdx < 0 || vehicleIdx >= ownedVehicles.size()) return;
            Vehicle selectedVehicle = ownedVehicles.get(vehicleIdx);
            gameController.onDeployVehicle(selectedVehicle);
            dialog.close();
        });

        VBox assignCard = new VBox(12, ownedVehicleLabel, ownedVehicleBox, routeLabel, routeBox, loopBox);
        assignCard.setStyle(CARD_STYLE);

        return new VBox(14,
                sectionTitle,
                assignCard,
                assignButton,
                deployButton
        );
    }

    private static String formatName(VehicleType t) {
        return t.name().replace('_', ' ');
    }
}
