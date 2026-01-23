package com.example.ims001;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardView {

    private MainApp mainApp;
    private VBox root;

    public DashboardView(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome to Inventory Dashboard!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> mainApp.showLogin());

        root.getChildren().addAll(welcomeLabel, btnLogout);
    }

    public Parent getView() {
        return root;
    }
}
