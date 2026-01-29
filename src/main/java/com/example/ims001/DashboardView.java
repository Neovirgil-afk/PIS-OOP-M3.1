package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DashboardView {

    private final MainApp mainApp;
    private final String username;
    private final BorderPane root = new BorderPane();

    public DashboardView(MainApp mainApp, String username) {
        this.mainApp = mainApp;
        this.username = username;
        buildUI();
    }

    private void buildUI() {
        Label lblTitle = new Label("Dashboard");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label lblUser = new Label("Logged in as: " + username);
        lblUser.setStyle("-fx-font-size: 14px;");

        // ✅ STOCK SUMMARY
        StockSummary s = ProductDAO.getStockSummary();

        Label lblSummary = new Label(
                "Total: " + s.getTotal() +
                        " | In Stock: " + s.getInStock() +
                        " | Low: " + s.getLowStock() +
                        " | Out: " + s.getOutStock()
        );
        lblSummary.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblWarn = new Label();
        if (s.getOutStock() > 0 || s.getLowStock() > 0) {
            lblWarn.setText("⚠ Warning: Some items are LOW/OUT OF STOCK");
            lblWarn.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            lblWarn.setText("All stock healthy ✅");
            lblWarn.setStyle("-fx-text-fill: #03DE82; -fx-font-weight: bold;");
        }

        VBox header = new VBox(6, lblTitle, lblUser, lblSummary, lblWarn);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnInventory = new Button("Inventory");
        btnInventory.setPrefWidth(200);
        btnInventory.setOnAction(e -> mainApp.showInventoryView());

        Button btnHistory = new Button("History");
        btnHistory.setPrefWidth(200);
        btnHistory.setOnAction(e -> mainApp.showHistoryView());

        Button btnLogout = new Button("Logout");
        btnLogout.setPrefWidth(200);
        btnLogout.setOnAction(e -> mainApp.showLoginView());

        VBox menu = new VBox(15, btnInventory, btnHistory, btnLogout);
        menu.setPadding(new Insets(20));
        menu.setAlignment(Pos.CENTER);

        root.setTop(header);
        root.setCenter(menu);
    }

    public Parent getView() {
        return root;
    }
}
