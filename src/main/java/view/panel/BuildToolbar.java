package view.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Road;

/**
 * Bottom toolbar shown during gameplay.
 *
 * <p>Top-level buttons: Select, Build, Remove, Garage, Finance, Save, Exit, Menu.
 * Clicking <b>Build</b> reveals a submenu (Road, Stop, Bridge, Route, Traffic Light).
 * Clicking <b>Bridge</b> within that submenu reveals a second submenu for bridge type.
 * During route drawing, all submenus are hidden and a dedicated route-draw bar is shown instead.
 *
 * <p>Selection state is tracked by the boolean fields ({@code roadSelected}, {@code stopSelected}, etc.)
 * and read by {@link controller.GameController} on each map click. Only one mode can be active at a time;
 * {@link #clearSelection()} resets all flags and restores button styles.
 *
 * <p>Renamed from {@code BottomToolbar} to {@code BuildToolbar} to match the UML diagram.
 */
public class BuildToolbar extends VBox {

    // ── Style constants ───────────────────────────────────────────────────────
    private static final String BAR_BG =
            "-fx-background-color: linear-gradient(to right, #99D2FB, #FBD3AC);" +
                    "-fx-border-color: #d0d0d0;" +
                    "-fx-border-width: 1 0 0 0;";

    private static final String PANEL_BOX =
            "-fx-background-color: rgba(255,255,255,0.70);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(0,0,0,0.08);" +
                    "-fx-border-radius: 12;";

    private static final String BTN_NORMAL =
            "-fx-background-color:#9AAB64; -fx-text-fill:white; " +
                    "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:8 18; " +
                    "-fx-background-radius:10; -fx-cursor:hand;";

    private static final String BTN_HIGHLIGHT =
            "-fx-background-color:#AA333C; -fx-text-fill:white; " +
                    "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:8 18; " +
                    "-fx-background-radius:10; -fx-cursor:hand;";

    private static final String SUB_BTN_NORMAL =
            "-fx-background-color:#9AAB64; -fx-text-fill:white; " +
                    "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; " +
                    "-fx-background-radius:8; -fx-cursor:hand;";

    private static final String SUB_BTN_ACTIVE =
            "-fx-background-color:#AA333C; -fx-text-fill:white; " +
                    "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; " +
                    "-fx-background-radius:8; -fx-cursor:hand;";


    // ── State ─────────────────────────────────────────────────────────────────
    private boolean roadSelected;
    private boolean stopSelected;
    private boolean bridgeSelected;
    private boolean removeSelected;
    private boolean routeSelected;
    private boolean selectSelected;

    // ── Main toolbar buttons ──────────────────────────────────────────────────
    private final Button btnSelect;
    private final Button btnBuild;
    private final Button btnRemove;
    private final Button btnGarage;
    private final Button btnFinance;
    private final Button btnSave;
    private final Button btnExit;
    private final Button btnMenu;

    // ── Build submenu buttons ─────────────────────────────────────────────────
    private final Button btnRoad;
    private final Button btnStop;
    private final Button btnBridge;
    private final Button btnRoute;
    private final Button btnTrafficLight;
    private final HBox   buildSubmenu;
    private boolean trafficLightSelected;

    // ── Bridge type buttons ─────────────────────────────────────────────────
    private final Button btnWoodenBridge;
    private final Button btnStoneBridge;
    private final Button btnSteelBridge;
    private HBox bridgeSubmenu;
    private String selectedBridgeType = "Wooden Bridge";

    // ── Route-draw toolbar (shown while player is drawing a route) ────────────
    private final Button btnDoneRoute;
    private final Button btnCancelRoute;
    private final HBox   routeDrawBar;

    /** Creates and wires up the full toolbar with all buttons, submenus, and toggle logic. */
    public BuildToolbar() {
        super();

        // Main toolbar
        btnSelect  = makeBtn("Select");
        btnBuild   = makeBtn("Build");
        btnRemove  = makeBtn("Remove");
        btnGarage  = makeBtn("Garage");
        btnFinance = makeBtn("Finance");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnSave = makeBtn("Save");
        btnExit = makeBtn("Exit");
        btnMenu = makeBtn("Menu");

        HBox toolbar = new HBox(8,
                btnSelect, btnBuild, btnRemove, btnGarage, btnFinance,
                spacer,
                btnSave, btnExit, btnMenu);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(BAR_BG);


        // Build submenu (Road + Stop + Route + Traffic Light)
        btnRoad         = new Button("Road");
        btnStop         = new Button("Stop");
        btnBridge       = new Button("Bridge");
        btnRoute        = new Button("+ Route");
        btnTrafficLight = new Button("Traffic Light");
        btnRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);
        btnBridge.setStyle(SUB_BTN_NORMAL);
        btnRoute.setStyle(SUB_BTN_NORMAL);
        btnTrafficLight.setStyle(SUB_BTN_NORMAL);

        btnRoad.setOnAction(e -> {
            roadSelected         = true;
            stopSelected         = false;
            bridgeSelected       = false;
            routeSelected        = false;
            trafficLightSelected = false;
            removeSelected       = false;
            selectSelected       = false;
            bridgeSubmenu.setVisible(false);
            bridgeSubmenu.setManaged(false);
            btnRoad.setStyle(SUB_BTN_ACTIVE);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnTrafficLight.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });
        btnStop.setOnAction(e -> {
            stopSelected         = true;
            roadSelected         = false;
            bridgeSelected       = false;
            routeSelected        = false;
            trafficLightSelected = false;
            removeSelected       = false;
            selectSelected       = false;
            bridgeSubmenu.setVisible(false);
            bridgeSubmenu.setManaged(false);
            btnStop.setStyle(SUB_BTN_ACTIVE);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnTrafficLight.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });
        btnRoute.setOnAction(e -> {
            routeSelected        = true;
            roadSelected         = false;
            stopSelected         = false;
            bridgeSelected       = false;
            trafficLightSelected = false;
            removeSelected       = false;
            selectSelected       = false;
            bridgeSubmenu.setVisible(false);
            bridgeSubmenu.setManaged(false);
            btnRoute.setStyle(SUB_BTN_ACTIVE);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
            btnTrafficLight.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });

        btnTrafficLight.setOnAction(e -> {
            trafficLightSelected = true;
            roadSelected         = false;
            stopSelected         = false;
            bridgeSelected       = false;
            routeSelected        = false;
            removeSelected       = false;
            selectSelected       = false;
            bridgeSubmenu.setVisible(false);
            bridgeSubmenu.setManaged(false);
            btnTrafficLight.setStyle(SUB_BTN_ACTIVE);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });

        Label lblBuild = new Label("Build:");
        lblBuild.setStyle("-fx-text-fill:#666666; -fx-font-size:13px; -fx-font-weight:bold; -fx-padding:0 8 0 0;");


        buildSubmenu = new HBox(10, lblBuild, btnRoad, btnStop, btnBridge, btnRoute, btnTrafficLight);
        buildSubmenu.setPadding(new Insets(8, 12, 8, 12));
        buildSubmenu.setAlignment(Pos.CENTER_LEFT);
        buildSubmenu.setStyle(PANEL_BOX);

        buildSubmenu.setVisible(false);
        buildSubmenu.setManaged(false);

        // Bridge submenu (Wooden + Stone + Steel)
        btnWoodenBridge = new Button("Wooden Bridge");
        btnStoneBridge = new Button("Stone Bridge");
        btnSteelBridge = new Button("Steel Bridge");
        btnWoodenBridge.setStyle(SUB_BTN_NORMAL);
        btnStoneBridge.setStyle(SUB_BTN_NORMAL);
        btnSteelBridge.setStyle(SUB_BTN_NORMAL);

        btnWoodenBridge.setOnAction(e -> {
            bridgeSelected = true;
            selectedBridgeType = "Wooden Bridge";
            btnWoodenBridge.setStyle(SUB_BTN_ACTIVE);
            btnStoneBridge.setStyle(SUB_BTN_NORMAL);
            btnSteelBridge.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
        });
        btnStoneBridge.setOnAction(e -> {
            bridgeSelected = true;
            selectedBridgeType = "Stone Bridge";
            btnWoodenBridge.setStyle(SUB_BTN_NORMAL);
            btnStoneBridge.setStyle(SUB_BTN_ACTIVE);
            btnSteelBridge.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
        });
        btnSteelBridge.setOnAction(e -> {
            bridgeSelected = true;
            selectedBridgeType = "Steel Bridge";
            btnWoodenBridge.setStyle(SUB_BTN_NORMAL);
            btnStoneBridge.setStyle(SUB_BTN_NORMAL);
            btnSteelBridge.setStyle(SUB_BTN_ACTIVE);
            btnBridge.setStyle(SUB_BTN_NORMAL);
        });

        bridgeSubmenu = new HBox(10, btnWoodenBridge, btnStoneBridge, btnSteelBridge);
        bridgeSubmenu.setPadding(new Insets(8, 12, 8, 12));
        bridgeSubmenu.setAlignment(Pos.CENTER_LEFT);
        bridgeSubmenu.setStyle(PANEL_BOX);
        bridgeSubmenu.setVisible(false);
        bridgeSubmenu.setManaged(false);

        btnBridge.setOnAction(e -> {
            boolean show = !bridgeSubmenu.isVisible();
            bridgeSelected       = show;
            roadSelected         = false;
            stopSelected         = false;
            routeSelected        = false;
            trafficLightSelected = false;
            removeSelected       = false;
            selectSelected       = false;
            bridgeSubmenu.setVisible(show);
            bridgeSubmenu.setManaged(show);
            btnBridge.setStyle(show ? SUB_BTN_ACTIVE : SUB_BTN_NORMAL);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnTrafficLight.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });

        // Toggle logic
        btnBuild.setOnAction(e -> {
            boolean show = !buildSubmenu.isVisible();
            removeSelected = false;
            selectSelected = false;
            buildSubmenu.setVisible(show);
            buildSubmenu.setManaged(show);
            btnBuild.setStyle(show ? BTN_HIGHLIGHT : BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
            if (!show) {
                bridgeSubmenu.setVisible(false);
                bridgeSubmenu.setManaged(false);
                clearSelection();
            }
        });

        btnRemove.setOnAction(e -> {
            removeSelected = true;
            roadSelected   = false;
            stopSelected   = false;
            bridgeSelected = false;
            routeSelected  = false;
            trafficLightSelected = false;
            selectSelected = false;
            buildSubmenu.setVisible(false);
            buildSubmenu.setManaged(false);
            bridgeSubmenu.setVisible(false);
            bridgeSubmenu.setManaged(false);
            btnBuild.setStyle(BTN_NORMAL);
            btnRemove.setStyle(BTN_HIGHLIGHT);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnTrafficLight.setStyle(SUB_BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });

        btnSelect.setOnAction(e -> {
            selectSelected = true;
            roadSelected   = false;
            stopSelected   = false;
            bridgeSelected = false;
            routeSelected  = false;
            trafficLightSelected = false;
            removeSelected = false;
            buildSubmenu.setVisible(false);
            buildSubmenu.setManaged(false);
            bridgeSubmenu.setVisible(false);
            bridgeSubmenu.setManaged(false);
            btnBuild.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_HIGHLIGHT);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnBridge.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnTrafficLight.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
        });

        // ── Route-draw bar (visible only while drawing a route) ───────────────
        btnDoneRoute   = new Button("✔  Done Route");
        btnCancelRoute = new Button("✕  Cancel");
        btnDoneRoute.setStyle(
                "-fx-background-color:#9AAB64; -fx-text-fill:white;" +
                        "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 16; -fx-background-radius:8; -fx-cursor:hand;");
        btnCancelRoute.setStyle(
                "-fx-background-color:#AA333C; -fx-text-fill:white;" +
                        "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 16; -fx-background-radius:8; -fx-cursor:hand;");


        Label lblDrawing = new Label("🖊  Drawing route — click road/stop tiles to trace the path:");
        lblDrawing.setStyle("-fx-text-fill:#666666; -fx-font-size:12px; -fx-font-weight:bold;");


        routeDrawBar = new HBox(12, lblDrawing, btnDoneRoute, btnCancelRoute);
        routeDrawBar.setPadding(new Insets(8, 12, 8, 12));
        routeDrawBar.setAlignment(Pos.CENTER_LEFT);
        routeDrawBar.setStyle(PANEL_BOX);

        routeDrawBar.setVisible(false);
        routeDrawBar.setManaged(false);

        // Bridge type submenu appears above the build submenu row.
        getChildren().addAll(bridgeSubmenu, buildSubmenu, routeDrawBar, toolbar);
    }

    // ── State getters ─────────────────────────────────────────────────────────

    /** Returns {@code true} if Road build mode is active. */
    public boolean isRoadSelected()         { return roadSelected; }
    /** Returns {@code true} if Stop build mode is active. */
    public boolean isStopSelected()         { return stopSelected; }
    /** Returns {@code true} if Bridge build mode is active. */
    public boolean isBridgeSelected()       { return bridgeSelected; }
    /** Returns {@code true} if Demolish mode is active. */
    public boolean isRemoveSelected()       { return removeSelected; }
    /** Returns {@code true} if Route-draw mode is active. */
    public boolean isRouteSelected()        { return routeSelected; }
    /** Returns {@code true} if Select (inspect) mode is active. */
    public boolean isSelectSelected()       { return selectSelected; }
    /** Returns {@code true} if Traffic Light placement mode is active. */
    public boolean isTrafficLightSelected() { return trafficLightSelected; }
    /** Returns the display name of the currently selected bridge type (e.g. {@code "Steel Bridge"}). */
    public String getSelectedBridgeType()   { return selectedBridgeType; }

    /**
     * Clears all selection flags and resets all button styles to their default (inactive) appearance.
     * Does not close submenus — callers that want to hide submenus should call this after hiding them.
     */
    public void clearSelection() {
        roadSelected         = false;
        stopSelected         = false;
        bridgeSelected       = false;
        removeSelected       = false;
        routeSelected        = false;
        selectSelected       = false;
        trafficLightSelected = false;
        btnRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);
        btnBridge.setStyle(SUB_BTN_NORMAL);
        btnRoute.setStyle(SUB_BTN_NORMAL);
        btnTrafficLight.setStyle(SUB_BTN_NORMAL);
        btnRemove.setStyle(BTN_NORMAL);
        btnSelect.setStyle(BTN_NORMAL);
        btnBuild.setStyle(BTN_NORMAL);
        bridgeSubmenu.setVisible(false);
        bridgeSubmenu.setManaged(false);

    }

    // ── Button accessors (for GameWindow wiring) ──────────────────────────────

    /** Returns the Select button for action-listener wiring in {@link view.GameWindow}. */
    public Button getSelectButton()  { return btnSelect; }
    /** Returns the Build button for action-listener wiring in {@link view.GameWindow}. */
    public Button getBuildButton()   { return btnBuild; }
    /** Returns the Remove button for action-listener wiring in {@link view.GameWindow}. */
    public Button getRemoveButton()  { return btnRemove; }
    /** Returns the Garage button for action-listener wiring in {@link view.GameWindow}. */
    public Button getGarageButton()  { return btnGarage; }
    /** Returns the Finance button for action-listener wiring in {@link view.GameWindow}. */
    public Button getFinanceButton() { return btnFinance; }
    /** Returns the Save button for action-listener wiring in {@link view.GameWindow}. */
    public Button getSaveButton()    { return btnSave; }
    /** Returns the Exit button for action-listener wiring in {@link view.GameWindow}. */
    public Button getExitButton()    { return btnExit; }
    /** Returns the Menu button for action-listener wiring in {@link view.GameWindow}. */
    public Button getMenuButton()    { return btnMenu; }
    /** Returns the Route submenu button for action-listener wiring in {@link view.GameWindow}. */
    public Button getRouteButton()        { return btnRoute; }
    /** Returns the Traffic Light submenu button for action-listener wiring. */
    public Button getTrafficLightButton() { return btnTrafficLight; }
    /** Returns the "Done Route" button shown in the route-draw bar. */
    public Button getDoneRouteButton()  { return btnDoneRoute; }
    /** Returns the "Cancel" button shown in the route-draw bar. */
    public Button getCancelRouteButton(){ return btnCancelRoute; }

    /** Show the route-draw instruction bar (hides build submenu). */
    public void showRouteDrawBar() {
        buildSubmenu.setVisible(false);
        buildSubmenu.setManaged(false);
        bridgeSubmenu.setVisible(false);
        bridgeSubmenu.setManaged(false);
        btnBuild.setStyle(BTN_NORMAL);
        routeDrawBar.setVisible(true);
        routeDrawBar.setManaged(true);
    }

    /** Hide the route-draw bar and go back to normal state. */
    public void hideRouteDrawBar() {
        routeDrawBar.setVisible(false);
        routeDrawBar.setManaged(false);
        clearSelection();
    }

    // kept for backwards compat — always returns true when road is selected
    /** @deprecated use {@link #isRoadSelected()} */
    @Deprecated
    public Road.RoadType getSelectedRoadType() {
        return roadSelected ? Road.RoadType.HORIZONTAL : null;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static Button makeBtn(String text) {
        Button b = new Button(text);
        b.setStyle(BTN_NORMAL);
        return b;
    }

    /**
     * Programmatically activates Select mode (same effect as clicking the Select button).
     * Used by {@link controller.GameController} to restore the default mode after certain actions.
     */
    public void selectSelectMode() {
        selectSelected = true;
        roadSelected   = false;
        stopSelected   = false;
        bridgeSelected = false;
        routeSelected  = false;
        removeSelected = false;
        trafficLightSelected = false;

        buildSubmenu.setVisible(false);
        buildSubmenu.setManaged(false);
        btnBuild.setStyle(BTN_NORMAL);
        btnSelect.setStyle(BTN_HIGHLIGHT);
        btnRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);
        btnBridge.setStyle(SUB_BTN_NORMAL);
        btnRoute.setStyle(SUB_BTN_NORMAL);
        btnTrafficLight.setStyle(SUB_BTN_NORMAL);

        btnRemove.setStyle(BTN_NORMAL);
    }
}
