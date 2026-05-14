package view.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * HudPanel — the top status bar shown during gameplay.
 *
 * Displays:
 *   - Pause / Continue button
 *   - Speed controls (1x, 2x, 4x)
 *   - Money
 *   - World name
 */
public class HudPanel extends HBox {

    private static final String PANEL_STYLE =
            "-fx-background-color: linear-gradient(to right, #99D2FB, #FBD3AC);" +
                    "-fx-border-color: #d0d0d0;" +
                    "-fx-border-width: 0 0 1 0;";

    private static final String ROUND_BUTTON_STYLE =
            "-fx-background-color: #AA333C;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 14;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 5 10;" +
                    "-fx-cursor: hand;";

    private static final String SPEED_BUTTON_STYLE =
            "-fx-background-color: #9AAB64;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 4 10;";

    private static final String SPEED_BUTTON_ACTIVE_STYLE =
            "-fx-background-color: #3C5227;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 4 10;";

    private static final String INFO_BOX_STYLE =
            "-fx-background-color: rgba(255,255,255,0.7);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: rgba(0,0,0,0.08);" +
                    "-fx-border-radius: 12;" +
                    "-fx-padding: 5 10;";

    private final Button btnPause;
    private final Button btnSpeed1x;
    private final Button btnSpeed2x;
    private final Button btnSpeed4x;

    private final Label lblMoney;
    private final Label lblWorldName;
    private final Label lblSelectedTile;
    private final Label lblTileTitle;

    private boolean paused = true;

    /** Creates a HudPanel with the world name set to "Unnamed World". */
    public HudPanel() {
        this("Unnamed World");
        lblMoney.setStyle(
                "-fx-text-fill: #3C5227;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        lblWorldName.setStyle(
                "-fx-text-fill: #3C5227;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );
    }

    /**
     * Creates a HudPanel displaying the given world name.
     *
     * @param worldName the world name shown in the top-right info box
     */
    public HudPanel(String worldName) {
        super(8);

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(6, 10, 6, 10));
        setStyle(PANEL_STYLE);

        // Pause button
        btnPause = new Button("Continue");
        btnPause.setStyle(ROUND_BUTTON_STYLE);
        btnPause.setMinHeight(38);
        btnPause.setMinWidth(90);
        btnPause.setPrefWidth(90);
        btnPause.setMaxWidth(90);

        // Speed controls
        btnSpeed1x = new Button("1x");
        btnSpeed2x = new Button("2x");
        btnSpeed4x = new Button("4x");

        btnSpeed1x.setStyle(SPEED_BUTTON_STYLE);
        btnSpeed2x.setStyle(SPEED_BUTTON_STYLE);
        btnSpeed4x.setStyle(SPEED_BUTTON_STYLE);


        HBox speedBox = new HBox(8, btnSpeed1x, btnSpeed2x, btnSpeed4x);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        speedBox.setStyle(INFO_BOX_STYLE);

        // Money display
        Label moneyTitle = new Label("Amount");
        moneyTitle.setStyle(
                "-fx-text-fill: #666666;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        lblMoney = new Label("$10,000");
        lblMoney.setStyle(
                "-fx-text-fill: #202020;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        HBox moneyBox = new HBox();
        moneyBox.setAlignment(Pos.CENTER_LEFT);
        moneyBox.setSpacing(8);
        moneyBox.setStyle(INFO_BOX_STYLE);
        moneyBox.getChildren().addAll(moneyTitle, lblMoney);

        // Selected tile display
        lblTileTitle = new Label("Hover");
        lblTileTitle.setStyle(
                "-fx-text-fill: #666666;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        lblSelectedTile = new Label("None");
        lblSelectedTile.setStyle(
                "-fx-text-fill: #202020;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        HBox selectedBox = new HBox(8, lblTileTitle, lblSelectedTile);
        selectedBox.setAlignment(Pos.CENTER_LEFT);
        selectedBox.setStyle(INFO_BOX_STYLE);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // World name display
        Label worldTitle = new Label("World");
        worldTitle.setStyle(
                "-fx-text-fill: #666666;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;"
        );

        lblWorldName = new Label();
        lblWorldName.setStyle(
                "-fx-text-fill: #202020;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );
        setWorldName(worldName);

        HBox worldBox = new HBox(8, worldTitle, lblWorldName);
        worldBox.setAlignment(Pos.CENTER_RIGHT);
        worldBox.setStyle(INFO_BOX_STYLE);

        getChildren().addAll(
                btnPause,
                speedBox,
                moneyBox,
                selectedBox,
                spacer,
                worldBox
        );
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Updates the world name displayed in the top-right box.
     * Falls back to "Unnamed World" if {@code worldName} is null or blank.
     *
     * @param worldName new world name to display
     */
    public void setWorldName(String worldName) {
        String displayName = (worldName == null || worldName.trim().isEmpty())
                ? "Unnamed World"
                : worldName.trim();
        lblWorldName.setText(displayName);
    }

    /**
     * Updates the money display to show {@code capital} formatted with thousands separators.
     *
     * @param capital current player balance in game currency
     */
    public void updateMoney(int capital) {
        lblMoney.setText(String.format("$%,d", capital));
    }

    /**
     * Toggles the pause state and updates the pause button label.
     *
     * @param paused {@code true} to show "Continue", {@code false} to show "Pause"
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
        btnPause.setText(paused ? "Continue" : "Pause");
    }

    /** Returns whether the simulation is currently paused. */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Highlights the speed button matching {@code speed} and resets the others.
     *
     * @param speed one of {@code "1x"}, {@code "2x"}, or {@code "4x"}
     */
    public void setActiveSpeedButton(String speed) {
        btnSpeed1x.setStyle(SPEED_BUTTON_STYLE);
        btnSpeed2x.setStyle(SPEED_BUTTON_STYLE);
        btnSpeed4x.setStyle(SPEED_BUTTON_STYLE);

        switch (speed) {
            case "1x" -> btnSpeed1x.setStyle(SPEED_BUTTON_ACTIVE_STYLE);
            case "2x" -> btnSpeed2x.setStyle(SPEED_BUTTON_ACTIVE_STYLE);
            case "4x" -> btnSpeed4x.setStyle(SPEED_BUTTON_ACTIVE_STYLE);
        }
    }



    /**
     * Shows tile info with a "Hover" prefix label (mouse is hovering over the tile).
     *
     * @param info description of the hovered tile
     */
    public void setHoverTile(String info) {
        lblTileTitle.setText("Hover");
        lblSelectedTile.setText(info);
    }

    /**
     * Shows tile info with a "Selected" prefix label (player clicked the tile).
     *
     * @param info description of the selected tile
     */
    public void setSelectedTile(String info) {
        lblTileTitle.setText("Selected");
        lblSelectedTile.setText(info);
    }

    /** Returns the pause/continue button for external wiring by {@link view.GameWindow}. */
    public Button getPauseButton() {
        return btnPause;
    }

    /** Returns the 1× speed button for external wiring. */
    public Button getSpeed1xButton() {
        return btnSpeed1x;
    }

    /** Returns the 2× speed button for external wiring. */
    public Button getSpeed2xButton() {
        return btnSpeed2x;
    }

    /** Returns the 4× speed button for external wiring. */
    public Button getSpeed4xButton() {
        return btnSpeed4x;
    }

    /** Returns the money label (used by {@link view.GameWindow} for live-binding). */
    public Label getMoneyLabel() {
        return lblMoney;
    }

    /** Returns the world-name label (used by {@link view.GameWindow} for live-binding). */
    public Label getWorldNameLabel() {
        return lblWorldName;
    }

    /** Returns the tile-info label (used by {@link view.GameWindow} for live-binding). */
    public Label getSelectedTileLabel() {
        return lblSelectedTile;
    }
}
