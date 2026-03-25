package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TopHud extends HBox {

    private final Button btnPause;
    private final Label  lblSpeed;
    private final Label  lblMoney;

    public TopHud() {
        super(10);

        btnPause = new Button("\u23f8");
        btnPause.setStyle(
            "-fx-background-color:#444444; -fx-text-fill:white; " +
            "-fx-background-radius:20; -fx-font-size:14px; -fx-padding:4 10;");

        lblSpeed = new Label("1x | 2x | 4x");
        lblSpeed.setStyle(
            "-fx-text-fill:black; -fx-background-color:white; " +
            "-fx-border-color:#888888; -fx-border-radius:4; " +
            "-fx-padding:4 10; -fx-font-size:12px;");

        lblMoney = new Label("amount: 10000$");
        lblMoney.setStyle("-fx-text-fill:#222222; -fx-font-size:13px; -fx-padding:4 12;");

        getChildren().addAll(btnPause, lblSpeed, lblMoney);
        setPadding(new Insets(6, 12, 6, 12));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color:#d8d8d8;");
    }

    public Button getPauseButton() { return btnPause; }
    public Label  getSpeedLabel()  { return lblSpeed; }
    public Label  getMoneyLabel()  { return lblMoney; }
}
