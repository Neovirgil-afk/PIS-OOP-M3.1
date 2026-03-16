package com.example.ims001;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public class HistoryView {

    private final MainApp mainApp;
    private BorderPane root;

    private final TableView<HistoryRecord> tblInventory = new TableView<>();
    private final TableView<HistoryRecord> tblSales = new TableView<>();

    private final ObservableList<HistoryRecord> invData = FXCollections.observableArrayList();
    private final ObservableList<HistoryRecord> salesData = FXCollections.observableArrayList();

    private DatePicker dpFrom;
    private DatePicker dpTo;

    public HistoryView(MainApp mainApp) {
        this.mainApp = mainApp;
        buildUI();
        refresh();
    }

    private void buildUI() {
        VBox page = new VBox(16);

        HBox toolbar = buildToolbar();
        TabPane tabs = buildTabs();

        VBox tabsCard = new VBox(tabs);
        tabsCard.getStyleClass().add("card");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        page.getChildren().addAll(toolbar, tabsCard);
        VBox.setVgrow(tabsCard, Priority.ALWAYS);

        AppShell shell = new AppShell(mainApp, Session.getUsername(), "history", "History", page);
        root = shell.getRoot();

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app-shell.css")).toExternalForm());
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/history-view.css")).toExternalForm());
    }

    private HBox buildToolbar() {
        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("action-btn");
        btnRefresh.setOnAction(e -> refresh());

        dpFrom = new DatePicker();
        dpFrom.setPromptText("From");
        dpFrom.getStyleClass().add("text-input");

        dpTo = new DatePicker();
        dpTo.setPromptText("To");
        dpTo.getStyleClass().add("text-input");

        Button btnToday = new Button("Today");
        btnToday.getStyleClass().add("action-btn");
        btnToday.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            dpFrom.setValue(today);
            dpTo.setValue(today);
            refresh();
        });

        Button btnWeek = new Button("This Week");
        btnWeek.getStyleClass().add("action-btn");
        btnWeek.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            dpFrom.setValue(monday);
            dpTo.setValue(today);
            refresh();
        });

        Button btnMonth = new Button("This Month");
        btnMonth.getStyleClass().add("action-btn");
        btnMonth.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDate first = today.withDayOfMonth(1);
            dpFrom.setValue(first);
            dpTo.setValue(today);
            refresh();
        });

        Button btnClear = new Button("Clear Filter");
        btnClear.getStyleClass().add("action-btn");
        btnClear.setOnAction(e -> {
            dpFrom.setValue(null);
            dpTo.setValue(null);
            refresh();
        });

        dpFrom.valueProperty().addListener((obs, o, n) -> refresh());
        dpTo.valueProperty().addListener((obs, o, n) -> refresh());

        HBox row = new HBox(
                10,
                btnRefresh,
                new Label("From:"), dpFrom,
                new Label("To:"), dpTo,
                btnToday,
                btnWeek,
                btnMonth,
                btnClear
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("history-toolbar");

        return row;
    }

    private TabPane buildTabs() {
        setupTable(tblInventory);
        setupTable(tblSales);

        tblInventory.setItems(invData);
        tblSales.setItems(salesData);

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("history-tabpane");

        Tab tabInventory = new Tab("Inventory History", tblInventory);
        tabInventory.setClosable(false);

        Tab tabSales = new Tab("Sales History", tblSales);
        tabSales.setClosable(false);

        tabs.getTabs().addAll(tabInventory, tabSales);
        return tabs;
    }

    private void setupTable(TableView<HistoryRecord> table) {
        TableColumn<HistoryRecord, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());

        TableColumn<HistoryRecord, String> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));

        TableColumn<HistoryRecord, String> colName = new TableColumn<>("Product");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));

        TableColumn<HistoryRecord, String> colDetails = new TableColumn<>("Changes");
        colDetails.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetails()));

        TableColumn<HistoryRecord, String> colHandledBy = new TableColumn<>("Handled By");
        colHandledBy.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHandledBy()));

        TableColumn<HistoryRecord, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt()));

        table.getColumns().setAll(colId, colAction, colName, colDetails, colHandledBy, colTime);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refresh() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        invData.setAll(HistoryDAO.getInventoryHistory(from, to));
        salesData.setAll(HistoryDAO.getSalesHistory(from, to));
    }

    public Parent getView() {
        return root;
    }
}