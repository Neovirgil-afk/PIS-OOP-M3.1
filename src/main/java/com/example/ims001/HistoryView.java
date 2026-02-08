package com.example.ims001;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class HistoryView {

    private final MainApp mainApp;
    private final BorderPane root = new BorderPane();

    private final TableView<HistoryRecord> tblInventory = new TableView<>();
    private final TableView<HistoryRecord> tblSales = new TableView<>();

    private final ObservableList<HistoryRecord> invData = FXCollections.observableArrayList();
    private final ObservableList<HistoryRecord> salesData = FXCollections.observableArrayList();

    // Date filters
    private DatePicker dpFrom;
    private DatePicker dpTo;

    public HistoryView(MainApp mainApp) {
        this.mainApp = mainApp;
        buildUI();
        refresh();
    }

    private void buildUI() {
        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> {
            String user = Session.getUsername();
            if (user == null || user.isBlank()) mainApp.showLoginView();
            else mainApp.showDashboardView(user);
        });

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refresh());

        dpFrom = new DatePicker();
        dpFrom.setPromptText("From");

        dpTo = new DatePicker();
        dpTo.setPromptText("To");

        Button btnToday = new Button("Today");
        btnToday.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            dpFrom.setValue(today);
            dpTo.setValue(today);
            refresh();
        });

        Button btnWeek = new Button("This Week");
        btnWeek.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            dpFrom.setValue(monday);
            dpTo.setValue(today);
            refresh();
        });

        Button btnMonth = new Button("This Month");
        btnMonth.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDate first = today.withDayOfMonth(1);
            dpFrom.setValue(first);
            dpTo.setValue(today);
            refresh();
        });

        Button btnClear = new Button("Clear Filter");
        btnClear.setOnAction(e -> {
            dpFrom.setValue(null);
            dpTo.setValue(null);
            refresh();
        });

        // auto refresh when date changes
        dpFrom.valueProperty().addListener((obs, o, n) -> refresh());
        dpTo.valueProperty().addListener((obs, o, n) -> refresh());

        HBox top = new HBox(10,
                btnBack,
                btnRefresh,
                new Separator(),
                new Label("From:"), dpFrom,
                new Label("To:"), dpTo,
                btnToday, btnWeek, btnMonth, btnClear
        );
        top.setPadding(new Insets(10));
        top.setAlignment(Pos.CENTER_LEFT);

        // Build tables
        setupTable(tblInventory);
        setupTable(tblSales);

        tblInventory.setItems(invData);
        tblSales.setItems(salesData);

        TabPane tabs = new TabPane();

        Tab tabInventory = new Tab("Inventory History", tblInventory);
        tabInventory.setClosable(false);

        Tab tabSales = new Tab("Sales History", tblSales);
        tabSales.setClosable(false);

        tabs.getTabs().addAll(tabInventory, tabSales);

        root.setTop(top);
        root.setCenter(tabs);
    }

    private void setupTable(TableView<HistoryRecord> table) {
        TableColumn<HistoryRecord, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<HistoryRecord, String> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));
        colAction.setPrefWidth(160);

        TableColumn<HistoryRecord, String> colName = new TableColumn<>("Product");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colName.setPrefWidth(220);

        TableColumn<HistoryRecord, String> colDetails = new TableColumn<>("Details");
        colDetails.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetails()));
        colDetails.setPrefWidth(480);

        TableColumn<HistoryRecord, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt()));
        colTime.setPrefWidth(190);

        table.getColumns().setAll(colId, colAction, colName, colDetails, colTime);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refresh() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        // Optional: if user sets only one, we still filter using that one.
        invData.setAll(HistoryDAO.getInventoryHistory(from, to));
        salesData.setAll(HistoryDAO.getSalesHistory(from, to));
    }

    public Parent getView() {
        return root;
    }
}
