package view.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.TrafficLight;
import model.enums.Direction;

/**
 * TrafficLightPanel — modal dialog for configuring green durations per direction.
 * Shows the currently active (green) direction at the top so the player knows
 * which direction is which.
 */
public class TrafficLightPanel {

    private static final String ROOT_STYLE =
            "-fx-background-color:#2b2b2b; -fx-border-color:#555; " +
            "-fx-border-width:1; -fx-border-radius:10; -fx-background-radius:10;";

    private static final String TITLE_STYLE =
            "-fx-text-fill:white; -fx-font-size:15px; -fx-font-weight:bold;";

    private static final String LABEL_STYLE =
            "-fx-text-fill:#cccccc; -fx-font-size:13px;";

    private static final String BTN_CONFIRM =
            "-fx-background-color:#4a9c6d; -fx-text-fill:white; " +
            "-fx-font-size:13px; -fx-padding:7 22; -fx-background-radius:6; -fx-cursor:hand;";

    private static final String BTN_CANCEL =
            "-fx-background-color:#9c4a4a; -fx-text-fill:white; " +
            "-fx-font-size:13px; -fx-padding:7 22; -fx-background-radius:6; -fx-cursor:hand;";

    public static void show(Stage owner, TrafficLight tl, Runnable onChanged) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Traffic Light — " + tl.getId());
        dialog.setResizable(false);

        Label title = new Label("Green Duration per Direction (seconds)");
        title.setStyle(TITLE_STYLE);

        // ── Current green direction indicator ──────────────────────────────
        Direction curGreen = tl.getCurrentGreenDirection();
        String greenText = curGreen != null
                ? "🟢  Currently green: " + dirLabel(curGreen) + " " + dirArrow(curGreen)
                : "🟢  Currently green: —";
        Label greenLabel = new Label(greenText);
        greenLabel.setStyle("-fx-text-fill:#55dd77; -fx-font-size:13px; -fx-font-weight:bold;");

        // ── Duration spinners ──────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 0, 16, 0));

        Direction[] dirs = tl.getActiveDirections().toArray(new Direction[0]);
        @SuppressWarnings("unchecked")
        Spinner<Integer>[] spinners = new Spinner[dirs.length];

        for (int i = 0; i < dirs.length; i++) {
            Direction d = dirs[i];
            Label lbl = new Label(dirLabel(d) + " " + dirArrow(d) + ":");
            lbl.setStyle(LABEL_STYLE);

            int current = (int) Math.round(tl.getGreenDuration(d));
            Spinner<Integer> spinner = new Spinner<>();
            spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, current));
            spinner.setEditable(true);
            spinner.setPrefWidth(90);
            spinner.setStyle("-fx-background-color:#3c3c3c; -fx-text-fill:white;");

            grid.add(lbl,     0, i);
            grid.add(spinner, 1, i);
            spinners[i] = spinner;
        }

        Button btnConfirm = new Button("Apply");
        btnConfirm.setStyle(BTN_CONFIRM);
        btnConfirm.setOnAction(e -> {
            for (int i = 0; i < dirs.length; i++) {
                tl.setGreenDuration(dirs[i], spinners[i].getValue());
            }
            if (onChanged != null) onChanged.run();
            dialog.close();
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle(BTN_CANCEL);
        btnCancel.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(12, btnConfirm, btnCancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, title, greenLabel, grid, buttons);
        root.setPadding(new Insets(20));
        root.setStyle(ROOT_STYLE);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private static String dirLabel(Direction d) {
        return switch (d) {
            case NORTH -> "North";
            case SOUTH -> "South";
            case EAST  -> "East";
            case WEST  -> "West";
        };
    }

    /** Small arrow so players can see which physical direction each label means. */
    private static String dirArrow(Direction d) {
        return switch (d) {
            case NORTH -> "↑";
            case SOUTH -> "↓";
            case EAST  -> "→";
            case WEST  -> "←";
        };
    }
}
