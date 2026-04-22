package view;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;

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

    public static Alert createAlert(Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (buttons != null && buttons.length > 0) {
            alert.getButtonTypes().setAll(buttons);
        }
        styleAlert(alert);
        return alert;
    }

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
