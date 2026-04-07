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
    private static final String BTN_NORMAL =
        "-fx-background-color:#3c3c3c; -fx-text-fill:white; " +
        "-fx-font-size:13px; -fx-padding:8 18; " +
        "-fx-background-radius:6; -fx-cursor:hand;";

    private static final String BTN_HIGHLIGHT =
        "-fx-background-color:#5a8fd8; -fx-text-fill:white; " +
        "-fx-font-size:13px; -fx-padding:8 18; " +
        "-fx-background-radius:6; -fx-cursor:hand;";

    private static final String SUB_BTN_NORMAL =
        "-fx-background-color:#4a4a4a; -fx-text-fill:white; " +
        "-fx-font-size:12px; -fx-padding:6 14; " +
        "-fx-background-radius:5; -fx-cursor:hand;";

    private static final String SUB_BTN_ACTIVE =
        "-fx-background-color:#5a8fd8; -fx-text-fill:white; " +
        "-fx-font-size:12px; -fx-padding:6 14; " +
        "-fx-background-radius:5; -fx-cursor:hand;";

    // ── State ─────────────────────────────────────────────────────────────────
    private Road.RoadType selectedRoadType;
    private boolean       stopSelected;
    private boolean       removeSelected;

    // ── Main toolbar buttons ──────────────────────────────────────────────────
    private final Button btnSelect;
    private final Button btnBuild;
    private final Button btnRemove;
    private final Button btnGarage;
    private final Button btnFinance;
    private final Button btnSave;
    private final Button btnMenu;

    // ── Build submenu buttons ─────────────────────────────────────────────────
    private final Button btnHRoad;
    private final Button btnVRoad;
    private final Button btnStop;
    private final HBox   roadSubmenu;

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
        btnMenu = makeBtn("Menu");

        HBox toolbar = new HBox(8,
                btnSelect, btnBuild, btnRemove, btnGarage, btnFinance,
                spacer,
                btnSave, btnMenu);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color:#2b2b2b;");

        // Build submenu (Roads + Stops)
        btnHRoad = new Button("Horizontal");
        btnVRoad = new Button("Vertical");
        btnStop  = new Button("Stop");
        btnHRoad.setStyle(SUB_BTN_NORMAL);
        btnVRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);

        btnHRoad.setOnAction(e -> {
            selectedRoadType = Road.RoadType.HORIZONTAL;
            stopSelected = false;
            removeSelected = false;
            btnHRoad.setStyle(SUB_BTN_ACTIVE);
            btnVRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
        });
        btnVRoad.setOnAction(e -> {
            selectedRoadType = Road.RoadType.VERTICAL;
            stopSelected = false;
            removeSelected = false;
            btnVRoad.setStyle(SUB_BTN_ACTIVE);
            btnHRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
        });
        btnStop.setOnAction(e -> {
            stopSelected = true;
            selectedRoadType = null;
            removeSelected = false;
            btnStop.setStyle(SUB_BTN_ACTIVE);
            btnHRoad.setStyle(SUB_BTN_NORMAL);
            btnVRoad.setStyle(SUB_BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
        });

        Label lblRoads = new Label("Roads:");
        lblRoads.setStyle("-fx-text-fill:#cccccc; -fx-font-size:13px; -fx-padding:0 8 0 0;");
        Label lblSep = new Label("|");
        lblSep.setStyle("-fx-text-fill:#666666; -fx-font-size:14px; -fx-padding:0 4;");
        Label lblStops = new Label("Stops:");
        lblStops.setStyle("-fx-text-fill:#cccccc; -fx-font-size:13px; -fx-padding:0 8 0 0;");

        roadSubmenu = new HBox(10, lblRoads, btnHRoad, btnVRoad, lblSep, lblStops, btnStop);
        roadSubmenu.setPadding(new Insets(8, 12, 8, 12));
        roadSubmenu.setAlignment(Pos.CENTER_LEFT);
        roadSubmenu.setStyle("-fx-background-color:#3a3a3a;");
        roadSubmenu.setVisible(false);
        roadSubmenu.setManaged(false);

        // Toggle logic
        btnBuild.setOnAction(e -> {
            boolean show = !roadSubmenu.isVisible();
            removeSelected = false;
            roadSubmenu.setVisible(show);
            roadSubmenu.setManaged(show);
            btnBuild.setStyle(show ? BTN_HIGHLIGHT : BTN_NORMAL);
            btnRemove.setStyle(BTN_NORMAL);
            if (!show) clearSelection();
        });

        btnRemove.setOnAction(e -> {
            removeSelected = true;
            selectedRoadType = null;
            stopSelected = false;
            roadSubmenu.setVisible(false);
            roadSubmenu.setManaged(false);
            btnBuild.setStyle(BTN_NORMAL);
            btnRemove.setStyle(BTN_HIGHLIGHT);
            btnHRoad.setStyle(SUB_BTN_NORMAL);
            btnVRoad.setStyle(SUB_BTN_NORMAL);
            btnStop.setStyle(SUB_BTN_NORMAL);
        });

        btnSelect.setOnAction(e -> {
            roadSubmenu.setVisible(false);
            roadSubmenu.setManaged(false);
            btnBuild.setStyle(BTN_NORMAL);
            clearSelection();
        });

        // Submenu on top, toolbar on bottom
        getChildren().addAll(roadSubmenu, toolbar);
    }

    // ── State getters ─────────────────────────────────────────────────────────

    public Road.RoadType getSelectedRoadType() { return selectedRoadType; }
    public boolean isStopSelected()            { return stopSelected; }
    public boolean isRemoveSelected()          { return removeSelected; }

    public void clearSelection() {
        selectedRoadType = null;
        stopSelected = false;
        removeSelected = false;
        btnHRoad.setStyle(SUB_BTN_NORMAL);
        btnVRoad.setStyle(SUB_BTN_NORMAL);
        btnStop.setStyle(SUB_BTN_NORMAL);
        btnRemove.setStyle(BTN_NORMAL);
    }

    // ── Button accessors (for GameWindow wiring) ──────────────────────────────

    public Button getSelectButton()  { return btnSelect; }
    public Button getBuildButton()   { return btnBuild; }
    public Button getRemoveButton()  { return btnRemove; }
    public Button getGarageButton()  { return btnGarage; }
    public Button getFinanceButton() { return btnFinance; }
    public Button getSaveButton()    { return btnSave; }
    public Button getMenuButton()    { return btnMenu; }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static Button makeBtn(String text) {
        Button b = new Button(text);
        b.setStyle(BTN_NORMAL);
        return b;
    }
}
