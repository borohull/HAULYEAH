package view;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Centralized theme constants and factory helpers for all in-game popup dialogs.
 *
 * <p>All style strings are inline JavaFX CSS, applied via {@code setStyle()}. The
 * factory methods create pre-styled {@link Alert} instances that match the game's
 * warm gradient colour scheme. {@link #showAndWait(Alert, Window)} restores window
 * focus and maximized state after the dialog closes — needed on some platforms
 * where the owner window loses focus when a modal dialog is dismissed.
 */
public final class PopupTheme {

    public static final String ROOT_STYLE =
            "-fx-background-color: linear-gradient(to right, #d9edf9, #f7dcc0);" +
                    "-fx-border-color: #d8c3a8;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 16;" +
                    "-fx-background-radius: 16;";

    public static final String HEADER_STYLE =
            "-fx-background-color: rgba(255,255,255,0.55);" +
                    "-fx-border-color: rgba(0,0,0,0.08);" +
                    "-fx-border-width: 0 0 1 0;" +
                    "-fx-background-radius: 16 16 0 0;";

    public static final String TITLE_STYLE =
            "-fx-font-size: 24px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #2f2a24;";

    public static final String LABEL_STYLE =
            "-fx-font-size: 14px;" +
                    "-fx-text-fill: #4b3a2d;";

    public static final String SUBTEXT_STYLE =
            "-fx-font-size: 13px;" +
                    "-fx-text-fill: #7b6553;";

    public static final String CARD_STYLE =
            "-fx-background-color: rgba(255,255,255,0.58);" +
                    "-fx-border-color: rgba(0,0,0,0.08);" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 18;";

    public static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: #b83a3a;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 10 22;" +
                    "-fx-cursor: hand;";

    public static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: #9aab64;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 10 22;" +
                    "-fx-cursor: hand;";

    public static final String MUTED_BUTTON_STYLE =
            "-fx-background-color: #8f857a;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 10 22;" +
                    "-fx-cursor: hand;";

    public static final String FIELD_STYLE =
            "-fx-font-size: 14px;" +
                    "-fx-text-fill: #3d3026;" +
                    "-fx-background-color: rgba(255,255,255,0.92);" +
                    "-fx-border-color: rgba(0,0,0,0.10);" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 8 10;";

    private PopupTheme() {}

    /**
     * Creates a themed {@link Alert} without an owner window.
     *
     * @param type    alert type (INFORMATION, CONFIRMATION, etc.)
     * @param title   window title bar text
     * @param header  bold header text inside the dialog (may be {@code null})
     * @param content body text
     * @param buttons button types to show; if empty the alert's defaults are used
     * @return a styled, ready-to-show alert
     */
    public static Alert createAlert(Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        return createAlert(null, type, title, header, content, buttons);
    }

    /**
     * Creates a themed {@link Alert} with an optional owner window for modality.
     *
     * @param owner   owning window for {@link Modality#WINDOW_MODAL}, or {@code null}
     * @param type    alert type
     * @param title   window title bar text
     * @param header  bold header text inside the dialog (may be {@code null})
     * @param content body text
     * @param buttons button types to show; if empty the alert's defaults are used
     * @return a styled, ready-to-show alert
     */
    public static Alert createAlert(Window owner,
                                    Alert.AlertType type,
                                    String title,
                                    String header,
                                    String content,
                                    ButtonType... buttons) {
        Alert alert = new Alert(type);
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (buttons != null && buttons.length > 0) {
            alert.getButtonTypes().setAll(buttons);
        }
        styleAlert(alert);
        return alert;
    }

    /**
     * Shows the alert modally and restores the owner window's focus and maximized state afterward.
     *
     * @param alert the alert to display
     * @param owner the game window that should regain focus when the dialog closes
     * @return the button the user clicked, wrapped in an {@link Optional}
     */
    public static Optional<ButtonType> showAndWait(Alert alert, Window owner) {
        boolean wasMaximized = owner instanceof Stage stage && stage.isMaximized();
        Optional<ButtonType> result = alert.showAndWait();
        if (owner instanceof Stage stage && stage.isShowing()) {
            stage.toFront();
            stage.requestFocus();
            if (wasMaximized && !stage.isMaximized()) {
                stage.setMaximized(true);
            }
        }
        return result;
    }

    /**
     * Applies the game's colour scheme to an already-constructed {@link Alert}.
     * Called automatically by the {@code createAlert} factories; exposed for
     * alerts constructed directly.
     *
     * @param alert the alert to style
     */
    public static void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.setGraphic(null);
        pane.setStyle(ROOT_STYLE);

        alert.setOnShown(e -> {
            Node header = pane.lookup(".header-panel");
            if (header != null) header.setStyle(HEADER_STYLE);

            Node headerLabel = pane.lookup(".header-panel .label");
            if (headerLabel instanceof Label label) label.setStyle(TITLE_STYLE.replace("24px", "20px"));

            Node contentLabel = pane.lookup(".content.label");
            if (contentLabel instanceof Label label) label.setStyle(LABEL_STYLE);

            for (ButtonType bt : pane.getButtonTypes()) {
                Node node = pane.lookupButton(bt);
                if (node instanceof Button button) {
                    ButtonBar.ButtonData data = bt.getButtonData();
                    if (bt == ButtonType.CANCEL || data == ButtonBar.ButtonData.CANCEL_CLOSE) {
                        button.setStyle(MUTED_BUTTON_STYLE);
                    } else if (data == ButtonBar.ButtonData.OK_DONE || data == ButtonBar.ButtonData.YES || data == ButtonBar.ButtonData.APPLY) {
                        button.setStyle(SECONDARY_BUTTON_STYLE);
                    } else {
                        button.setStyle(PRIMARY_BUTTON_STYLE);
                    }
                }
            }
        });
    }
}
