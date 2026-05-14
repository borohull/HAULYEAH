package view.panel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.FinanceLedger;
import model.Transaction;
import model.enums.TransactionType;

import java.util.List;
import java.util.Map;

/**
 * Modal dialog for reviewing financial history, shown when the player clicks Finance.
 *
 * <p>Four tabs are available:
 * <ul>
 *   <li><b>All</b> — every non-maintenance transaction, newest first.</li>
 *   <li><b>Income</b> — only positive transactions.</li>
 *   <li><b>Expenditure</b> — only negative transactions.</li>
 *   <li><b>Maintenance</b> — running totals aggregated by vehicle key.</li>
 * </ul>
 *
 * <p>A {@link javafx.animation.Timeline} refreshes the balance label and transaction
 * lists every second while the dialog is open.
 */
public class FinancePanel {

    // ── Styles (mirrors GaragePanel exactly) ─────────────────────────────────
    private static final String ROOT_STYLE =
            "-fx-background-color: linear-gradient(to right, #d9edf9, #f7dcc0);" +
            "-fx-border-color: #d8c3a8; -fx-border-width: 1;" +
            "-fx-border-radius: 14; -fx-background-radius: 14;";

    private static final String HEADER_STYLE =
            "-fx-background-color: rgba(255,255,255,0.55);" +
            "-fx-border-color: rgba(0,0,0,0.08); -fx-border-width: 0 0 1 0;" +
            "-fx-background-radius: 14 14 0 0;";

    private static final String LEFT_PANEL_STYLE =
            "-fx-background-color: rgba(255,255,255,0.18);" +
            "-fx-border-color: rgba(0,0,0,0.08); -fx-border-width: 0 1 0 0;";

    private static final String RIGHT_PANEL_STYLE =
            "-fx-background-color: rgba(255,255,255,0.12);";

    private static final String TAB_ACTIVE_STYLE =
            "-fx-background-color: #b83a3a; -fx-text-fill: white;" +
            "-fx-font-size: 16px; -fx-font-weight: bold;" +
            "-fx-background-radius: 12; -fx-padding: 14 22; -fx-cursor: hand;";

    private static final String TAB_INACTIVE_STYLE =
            "-fx-background-color: #9aab64; -fx-text-fill: white;" +
            "-fx-font-size: 16px; -fx-font-weight: bold;" +
            "-fx-background-radius: 12; -fx-padding: 14 22; -fx-cursor: hand;";

    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: #b83a3a; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-background-radius: 10; -fx-padding: 10 22; -fx-cursor: hand;";

    private static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: #9aab64; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-background-radius: 10; -fx-padding: 10 22; -fx-cursor: hand;";

    private static final String CARD_STYLE =
            "-fx-background-color: rgba(255,255,255,0.55);" +
            "-fx-border-color: rgba(0,0,0,0.08); -fx-border-radius: 12;" +
            "-fx-background-radius: 12; -fx-padding: 18;";

    // ── Filter state ──────────────────────────────────────────────────────────
    private enum Filter { ALL, INCOME, EXPENDITURE }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Opens the Finance dialog as an application-modal window.
     *
     * @param owner  the owning stage (used for modality and positioning)
     * @param ledger the ledger whose data is displayed and auto-refreshed
     */
    public static void show(Stage owner, FinanceLedger ledger) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Finance");

        Filter[] currentFilter = {Filter.ALL};

        // ── Header ────────────────────────────────────────────────────────────
        Label title = new Label("Finance");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #2f2a24;");

        Label balanceLabel = new Label();
        balanceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        VBox header = new VBox(4, title, balanceLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(18, 20, 18, 20));
        header.setStyle(HEADER_STYLE);

        // ── Left tab panel ────────────────────────────────────────────────────
        Button btnAll  = new Button("All");
        Button btnIn   = new Button("Income");
        Button btnOut  = new Button("Expenditure");
        Button btnMaint = new Button("Maintenance");

        for (Button b : List.of(btnAll, btnIn, btnOut, btnMaint)) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefHeight(56);
        }

        VBox leftPanel = new VBox(14, btnAll, btnIn, btnOut, btnMaint);
        leftPanel.setPadding(new Insets(26));
        leftPanel.setPrefWidth(210);
        leftPanel.setStyle(LEFT_PANEL_STYLE);

        // ── Right content views ───────────────────────────────────────────────
        VBox txView    = buildTxView(ledger, currentFilter);
        VBox maintView = buildMaintView(ledger);

        maintView.setVisible(false);
        maintView.setManaged(false);

        Button closeButton = new Button("Close");
        closeButton.setStyle(SECONDARY_BUTTON_STYLE);
        closeButton.setOnAction(e -> dialog.close());

        VBox buttonRow = new VBox(closeButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        VBox rightPanel = new VBox(20, txView, maintView, buttonRow);
        rightPanel.setPadding(new Insets(26));
        rightPanel.setStyle(RIGHT_PANEL_STYLE);
        VBox.setVgrow(txView, Priority.ALWAYS);
        VBox.setVgrow(maintView, Priority.ALWAYS);

        // ── Tab switching ─────────────────────────────────────────────────────
        btnAll.setStyle(TAB_ACTIVE_STYLE);
        btnIn.setStyle(TAB_INACTIVE_STYLE);
        btnOut.setStyle(TAB_INACTIVE_STYLE);
        btnMaint.setStyle(TAB_INACTIVE_STYLE);

        Runnable showTx = () -> {
            txView.setVisible(true);   txView.setManaged(true);
            maintView.setVisible(false); maintView.setManaged(false);
        };
        Runnable showMaint = () -> {
            txView.setVisible(false);  txView.setManaged(false);
            maintView.setVisible(true); maintView.setManaged(true);
        };

        btnAll.setOnAction(e -> {
            currentFilter[0] = Filter.ALL;
            btnAll.setStyle(TAB_ACTIVE_STYLE); btnIn.setStyle(TAB_INACTIVE_STYLE);
            btnOut.setStyle(TAB_INACTIVE_STYLE); btnMaint.setStyle(TAB_INACTIVE_STYLE);
            showTx.run();
        });
        btnIn.setOnAction(e -> {
            currentFilter[0] = Filter.INCOME;
            btnIn.setStyle(TAB_ACTIVE_STYLE); btnAll.setStyle(TAB_INACTIVE_STYLE);
            btnOut.setStyle(TAB_INACTIVE_STYLE); btnMaint.setStyle(TAB_INACTIVE_STYLE);
            showTx.run();
        });
        btnOut.setOnAction(e -> {
            currentFilter[0] = Filter.EXPENDITURE;
            btnOut.setStyle(TAB_ACTIVE_STYLE); btnAll.setStyle(TAB_INACTIVE_STYLE);
            btnIn.setStyle(TAB_INACTIVE_STYLE); btnMaint.setStyle(TAB_INACTIVE_STYLE);
            showTx.run();
        });
        btnMaint.setOnAction(e -> {
            btnMaint.setStyle(TAB_ACTIVE_STYLE); btnAll.setStyle(TAB_INACTIVE_STYLE);
            btnIn.setStyle(TAB_INACTIVE_STYLE); btnOut.setStyle(TAB_INACTIVE_STYLE);
            showMaint.run();
        });

        // ── Layout ────────────────────────────────────────────────────────────
        HBox content = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);
        root.setStyle(ROOT_STYLE);

        // ── Live refresh ──────────────────────────────────────────────────────
        // Extract the scrollable lists so we can repopulate them
        ScrollPane txScroll   = (ScrollPane) txView.getChildren().get(1);
        VBox       txList     = (VBox) txScroll.getContent();
        ScrollPane maintScroll = (ScrollPane) maintView.getChildren().get(1);
        VBox       maintList  = (VBox) maintScroll.getContent();

        Runnable refresh = () -> {
            double capital = ledger.getCurrentCapital();
            balanceLabel.setText(String.format("Balance: $%,.0f", capital));

            // Transactions
            txList.getChildren().clear();
            List<Transaction> all = ledger.getTransactions();
            for (int i = all.size() - 1; i >= 0; i--) {
                Transaction tx = all.get(i);
                if (tx.getType() == TransactionType.MAINTENANCE) continue;
                boolean isIncome = tx.getAmount() > 0;
                if (currentFilter[0] == Filter.INCOME && !isIncome) continue;
                if (currentFilter[0] == Filter.EXPENDITURE && isIncome) continue;
                txList.getChildren().add(buildTxRow(tx));
            }
            if (txList.getChildren().isEmpty()) {
                Label empty = new Label("No transactions yet.");
                empty.setStyle("-fx-text-fill: #888; -fx-padding: 8 0;");
                txList.getChildren().add(empty);
            }

            // Maintenance
            maintList.getChildren().clear();
            Map<String, Double> totals = ledger.getMaintenanceTotals();
            if (totals.isEmpty()) {
                Label empty = new Label("No maintenance charges yet.");
                empty.setStyle("-fx-text-fill: #888; -fx-padding: 8 0;");
                maintList.getChildren().add(empty);
            } else {
                for (Map.Entry<String, Double> entry : totals.entrySet()) {
                    maintList.getChildren().add(buildMaintRow(entry.getKey(), entry.getValue()));
                }
            }
        };

        refresh.run();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> refresh.run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        dialog.setOnHidden(e -> timeline.stop());

        dialog.setScene(new Scene(root, 760, 560));
        dialog.setResizable(true);
        dialog.setMinWidth(680);
        dialog.setMinHeight(520);
        dialog.showAndWait();
    }

    // ── View builders ─────────────────────────────────────────────────────────

    private static VBox buildTxView(FinanceLedger ledger, Filter[] filter) {
        Label sectionTitle = new Label("Transaction History");
        sectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        VBox txList = new VBox(8);
        txList.setPadding(new Insets(4));

        ScrollPane scroll = new ScrollPane(txList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox view = new VBox(12, sectionTitle, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return view;
    }

    private static VBox buildMaintView(FinanceLedger ledger) {
        Label sectionTitle = new Label("Maintenance (running total)");
        sectionTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4b3a2d;");

        VBox maintList = new VBox(8);
        maintList.setPadding(new Insets(4));

        ScrollPane scroll = new ScrollPane(maintList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox view = new VBox(12, sectionTitle, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return view;
    }

    // ── Row builders ──────────────────────────────────────────────────────────

    private static HBox buildTxRow(Transaction tx) {
        boolean isIncome = tx.getAmount() > 0;
        String amountText = String.format("%s$%,.0f", isIncome ? "+" : "", tx.getAmount());
        String noteText = tx.getNote().isEmpty() ? tx.getType().name() : tx.getNote();

        Label amountLbl = new Label(amountText);
        amountLbl.setMinWidth(100);
        amountLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                + (isIncome ? "#2a7a2a" : "#aa2222") + ";");

        Label noteLbl = new Label(noteText);
        noteLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b3a2d;");
        noteLbl.setWrapText(true);

        HBox row = new HBox(16, amountLbl, noteLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(CARD_STYLE);
        HBox.setHgrow(noteLbl, Priority.ALWAYS);
        return row;
    }

    private static HBox buildMaintRow(String key, double total) {
        Label amountLbl = new Label(String.format("-$%,.0f", total));
        amountLbl.setMinWidth(100);
        amountLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #aa2222;");

        Label keyLbl = new Label("Maintenance: " + key);
        keyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b3a2d;");
        keyLbl.setWrapText(true);

        HBox row = new HBox(16, amountLbl, keyLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(CARD_STYLE);
        HBox.setHgrow(keyLbl, Priority.ALWAYS);
        return row;
    }
}
