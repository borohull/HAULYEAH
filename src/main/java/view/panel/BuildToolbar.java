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
 * BuildToolbar — the bottom toolbar shown during gameplay.
 *
 * Contains: Select, Build (with road/stop submenu), Remove, Garage, Finance, Save, Menu.
 * Renamed from BottomToolbar → BuildToolbar to match the UML diagram.
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
    private final Button btnRoute;
    private final Button btnTrafficLight;
    private final HBox   buildSubmenu;
    private boolean trafficLightSelected;

    // ── Route-draw toolbar (shown while player is drawing a route) ────────────
    private final Button btnDoneRoute;
    private final Button btnCancelRoute;
    private final HBox   routeDrawBar;

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
        btnRoute        = new Button("+ Route");
        btnTrafficLight = new Button("Traffic Light");
        btnRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);
        btnRoute.setStyle(SUB_BTN_NORMAL);
        btnTrafficLight.setStyle(SUB_BTN_NORMAL);

        btnRoad.setOnAction(e -> {
            roadSelected   = true;
            stopSelected   = false;
            routeSelected  = false;
            removeSelected = false;
            selectSelected = false;
            btnRoad.setStyle(SUB_BTN_ACTIVE);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });
        btnStop.setOnAction(e -> {
            stopSelected   = true;
            roadSelected   = false;
            routeSelected  = false;
            removeSelected = false;
            selectSelected = false;
            btnStop.setStyle(SUB_BTN_ACTIVE);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });
        btnRoute.setOnAction(e -> {
            routeSelected        = true;
            roadSelected         = false;
            stopSelected         = false;
            removeSelected       = false;
            selectSelected       = false;
            trafficLightSelected = false;
            btnRoute.setStyle(SUB_BTN_ACTIVE);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnTrafficLight.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });

        btnTrafficLight.setOnAction(e -> {
            trafficLightSelected = true;
            roadSelected         = false;
            stopSelected         = false;
            routeSelected        = false;
            removeSelected       = false;
            selectSelected       = false;
            btnTrafficLight.setStyle(SUB_BTN_ACTIVE);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });

        Label lblBuild = new Label("Build:");
        lblBuild.setStyle("-fx-text-fill:#666666; -fx-font-size:13px; -fx-font-weight:bold; -fx-padding:0 8 0 0;");


        buildSubmenu = new HBox(10, lblBuild, btnRoad, btnStop, btnRoute, btnTrafficLight);
        buildSubmenu.setPadding(new Insets(8, 12, 8, 12));
        buildSubmenu.setAlignment(Pos.CENTER_LEFT);
        buildSubmenu.setStyle(PANEL_BOX);

        buildSubmenu.setVisible(false);
        buildSubmenu.setManaged(false);

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
            if (!show) clearSelection();
        });

        btnRemove.setOnAction(e -> {
            removeSelected = true;
            roadSelected   = false;
            stopSelected   = false;
            routeSelected  = false;
            selectSelected = false;
            buildSubmenu.setVisible(false);
            buildSubmenu.setManaged(false);
            btnBuild.setStyle(BTN_NORMAL);
            btnRemove.setStyle(BTN_HIGHLIGHT);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
            btnSelect.setStyle(BTN_NORMAL);
        });

        btnSelect.setOnAction(e -> {
            selectSelected = true;
            roadSelected   = false;
            stopSelected   = false;
            routeSelected  = false;
            removeSelected = false;
            buildSubmenu.setVisible(false);
            buildSubmenu.setManaged(false);
            btnBuild.setStyle(BTN_NORMAL);
            btnSelect.setStyle(BTN_HIGHLIGHT);
            btnRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnRoute.setStyle(SUB_BTN_NORMAL);
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

        // Submenu on top, route-draw bar next, toolbar on bottom
        getChildren().addAll(buildSubmenu, routeDrawBar, toolbar);
    }

    // ── State getters ─────────────────────────────────────────────────────────

    public boolean isRoadSelected()         { return roadSelected; }
    public boolean isStopSelected()         { return stopSelected; }
    public boolean isRemoveSelected()       { return removeSelected; }
    public boolean isRouteSelected()        { return routeSelected; }
    public boolean isSelectSelected()       { return selectSelected; }
    public boolean isTrafficLightSelected() { return trafficLightSelected; }

    public void clearSelection() {
        roadSelected         = false;
        stopSelected         = false;
        removeSelected       = false;
        routeSelected        = false;
        selectSelected       = false;
        trafficLightSelected = false;
        btnRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);
        btnRoute.setStyle(SUB_BTN_NORMAL);
        btnTrafficLight.setStyle(SUB_BTN_NORMAL);
        btnRemove.setStyle(BTN_NORMAL);
        btnSelect.setStyle(BTN_NORMAL);
        btnBuild.setStyle(BTN_NORMAL);

    }

    // ── Button accessors (for GameWindow wiring) ──────────────────────────────

    public Button getSelectButton()  { return btnSelect; }
    public Button getBuildButton()   { return btnBuild; }
    public Button getRemoveButton()  { return btnRemove; }
    public Button getGarageButton()  { return btnGarage; }
    public Button getFinanceButton() { return btnFinance; }
    public Button getSaveButton()    { return btnSave; }
    public Button getExitButton()    { return btnExit; }
    public Button getMenuButton()    { return btnMenu; }
    public Button getRouteButton()        { return btnRoute; }
    public Button getTrafficLightButton() { return btnTrafficLight; }
    public Button getDoneRouteButton()  { return btnDoneRoute; }
    public Button getCancelRouteButton(){ return btnCancelRoute; }

    /** Show the route-draw instruction bar (hides build submenu). */
    public void showRouteDrawBar() {
        buildSubmenu.setVisible(false);
        buildSubmenu.setManaged(false);
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

    public void selectSelectMode() {
        selectSelected = true;
        roadSelected   = false;
        stopSelected   = false;
        routeSelected  = false;
        removeSelected = false;
        trafficLightSelected = false;

        buildSubmenu.setVisible(false);
        buildSubmenu.setManaged(false);
        btnBuild.setStyle(BTN_NORMAL);
        btnSelect.setStyle(BTN_HIGHLIGHT);
        btnRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);
        btnRoute.setStyle(SUB_BTN_NORMAL);
        btnTrafficLight.setStyle(SUB_BTN_NORMAL);

        btnRemove.setStyle(BTN_NORMAL);
    }
}
