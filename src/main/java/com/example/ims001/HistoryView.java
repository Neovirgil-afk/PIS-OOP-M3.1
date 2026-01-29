package com.example.ims001;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class HistoryView {

    private final MainApp mainApp;
    private final BorderPane root = new BorderPane();

    private final TableView<HistoryRecord> table = new TableView<>();
    private final ObservableList<HistoryRecord> data = FXCollections.observableArrayList();

    public HistoryView(MainApp mainApp) {
        this.mainApp = mainApp;
        buildUI();
        refresh();
    }

    private void buildUI() {
        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> {
            String user = Session.getUsername();
            if (user == null || user.isBlank()) {
                mainApp.showLoginView();
            } else {
                mainApp.showDashboardView(user);
            }
        });

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refresh());

        HBox top = new HBox(10, btnBack, btnRefresh);
        top.setPadding(new Insets(10));
        top.setAlignment(Pos.CENTER_LEFT);

        TableColumn<HistoryRecord, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject()
        );

        TableColumn<HistoryRecord, String> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getAction())
        );

        TableColumn<HistoryRecord, String> colName = new TableColumn<>("Product");
        colName.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getProductName())
        );

        TableColumn<HistoryRecord, String> colDetails = new TableColumn<>("Details");
        colDetails.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDetails())
        );

        TableColumn<HistoryRecord, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCreatedAt())
        );

        table.getColumns().setAll(colId, colAction, colName, colDetails, colTime);
        table.setItems(data);

        root.setTop(top);
        root.setCenter(table);
    }

    private void refresh() {
        data.setAll(HistoryDAO.getAll());
    }

    public Parent getView() {
        return root;
    }
}
