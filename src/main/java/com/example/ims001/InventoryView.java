package com.example.ims001;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Comparator;

public class InventoryView {

    private final MainApp mainApp;
    private final BorderPane root = new BorderPane();

    private final TableView<Product> table = new TableView<>();
    private final ObservableList<Product> masterData = FXCollections.observableArrayList();

    // ✅ For search + sorting
    private FilteredList<Product> filtered;
    private SortedList<Product> sorted;

    private final TextField tfName = new TextField();
    private final TextField tfCategory = new TextField();
    private final TextField tfQty = new TextField();
    private final TextField tfPrice = new TextField();

    private final Label status = new Label();
    private final Label stockWarning = new Label();

    // ✅ Search
    private final TextField tfSearch = new TextField();

    // ✅ Sort toggle
    private boolean lowStockFirstEnabled = false;

    private boolean warnedOnce = false;

    public InventoryView(MainApp mainApp) {
        this.mainApp = mainApp;
        buildUI();
        setupFilterAndSort();
        refresh();
    }

    private void buildUI() {
        // Top bar
        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> mainApp.showDashboardView(Session.getUsername()));

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refresh());

        // ✅ Search box
        tfSearch.setPromptText("Search (name / category / status)...");
        tfSearch.setPrefWidth(320);

        // ✅ Sort button
        Button btnLowStockFirst = new Button("Low Stock First: OFF");
        btnLowStockFirst.setOnAction(e -> {
            lowStockFirstEnabled = !lowStockFirstEnabled;
            btnLowStockFirst.setText(lowStockFirstEnabled ? "Low Stock First: ON" : "Low Stock First: OFF");
            applySort();
        });

        HBox top = new HBox(10, btnBack, btnRefresh, tfSearch, btnLowStockFirst, stockWarning);
        top.setPadding(new Insets(10));
        top.setAlignment(Pos.CENTER_LEFT);

        // Table columns
        TableColumn<Product, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());

        TableColumn<Product, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Product, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));

        TableColumn<Product, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()).asObject());

        TableColumn<Product, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()).asObject());

        // Status column
        TableColumn<Product, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus())
        );

        // Color status
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(s);
                switch (s) {
                    case "OUT OF STOCK" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    case "LOW STOCK" -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    case "IN STOCK" -> setStyle("-fx-text-fill: #03DE82; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        table.getColumns().setAll(colId, colName, colCat, colQty, colPrice, colStatus);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                tfName.setText(sel.getName());
                tfCategory.setText(sel.getCategory());
                tfQty.setText(String.valueOf(sel.getQuantity()));
                tfPrice.setText(String.valueOf(sel.getPrice()));
            }
        });

        // Form
        tfName.setPromptText("Name");
        tfCategory.setPromptText("Category");
        tfQty.setPromptText("Quantity");
        tfPrice.setPromptText("Price");

        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(e -> add());

        Button btnEdit = new Button("Edit Selected");
        btnEdit.setOnAction(e -> editSelected());

        Button btnRemove = new Button("Remove Selected");
        btnRemove.setOnAction(e -> removeSelected());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Name:"), tfName);
        form.addRow(1, new Label("Category:"), tfCategory);
        form.addRow(2, new Label("Qty:"), tfQty);
        form.addRow(3, new Label("Price:"), tfPrice);

        HBox actions = new HBox(10, btnAdd, btnEdit, btnRemove);
        actions.setPadding(new Insets(10));
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox bottom = new VBox(5, form, actions, status);
        bottom.setPadding(new Insets(10));

        root.setTop(top);
        root.setCenter(table);
        root.setBottom(bottom);
    }

    // ✅ Setup filtered + sorted lists and bind to search box
    private void setupFilterAndSort() {
        filtered = new FilteredList<>(masterData, p -> true);
        sorted = new SortedList<>(filtered);

        // Make sorted list obey TableView header sorting unless we override for low-stock-first
        sorted.comparatorProperty().bind(table.comparatorProperty());

        table.setItems(sorted);

        tfSearch.textProperty().addListener((obs, old, text) -> applyFilter(text));
    }

    private void applyFilter(String text) {
        String q = (text == null) ? "" : text.trim().toLowerCase();

        filtered.setPredicate(p -> {
            if (q.isEmpty()) return true;

            String name = safe(p.getName());
            String cat = safe(p.getCategory());
            String status = safe(p.getStatus());

            return name.contains(q) || cat.contains(q) || status.contains(q);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    // ✅ Low stock first sorting
    private void applySort() {
        if (!lowStockFirstEnabled) {
            // Return to normal: let table header sorting take over
            sorted.comparatorProperty().bind(table.comparatorProperty());
            table.sort(); // refresh
            return;
        }

        // When enabled, override comparator:
        // OUT OF STOCK (0) first, LOW STOCK (1..5) second, IN STOCK (6+) last
        Comparator<Product> cmp = Comparator
                .comparingInt(this::statusRank)
                .thenComparingInt(Product::getQuantity)
                .thenComparing(p -> p.getName() == null ? "" : p.getName().toLowerCase());

        // Break the binding then set our comparator
        sorted.comparatorProperty().unbind();
        sorted.setComparator(cmp);
        table.sort();
    }

    private int statusRank(Product p) {
        int q = p.getQuantity();
        if (q == 0) return 0;       // Out first
        if (q <= 5) return 1;       // Low second
        return 2;                   // In stock last
    }

    private void refresh() {
        masterData.setAll(ProductDAO.getAll());

        int low = 0, out = 0;
        for (Product p : masterData) {
            if (p.getQuantity() == 0) out++;
            else if (p.getQuantity() <= 5) low++;
        }

        status.setText("Loaded " + masterData.size() + " products.");
        if (out > 0 || low > 0) {
            stockWarning.setText("⚠ Low: " + low + " | Out: " + out);
            stockWarning.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            stockWarning.setText("All stock healthy ✅");
            stockWarning.setStyle("-fx-text-fill: #03DE82; -fx-font-weight: bold;");
        }

        // Re-apply filter + sort after refresh
        applyFilter(tfSearch.getText());
        applySort();

        // optional popup once
        if (!warnedOnce && (out > 0 || low > 0)) {
            warnedOnce = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Stock Alert");
            alert.setHeaderText("Stock warning detected");
            alert.setContentText("Low stock items: " + low + "\nOut of stock items: " + out);
            alert.showAndWait();
        }
    }

    private void add() {
        try {
            String name = tfName.getText().trim();
            String cat = tfCategory.getText().trim();
            int qty = Integer.parseInt(tfQty.getText().trim());
            double price = Double.parseDouble(tfPrice.getText().trim());

            if (name.isEmpty() || cat.isEmpty()) {
                status.setText("Name and Category are required.");
                return;
            }

            boolean ok = ProductDAO.add(name, cat, qty, price);
            if (ok) {
                String tempStatus = new Product(0, name, cat, qty, price).getStatus();
                HistoryDAO.log("ADD", name, "Added (" + qty + " pcs) | Status: " + tempStatus);
            }

            status.setText(ok ? "Product added." : "Add failed.");
            refresh();
        } catch (Exception ex) {
            status.setText("Invalid input (qty/price must be numbers).");
        }
    }

    private void editSelected() {
        Product sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            status.setText("Select a product first.");
            return;
        }

        try {
            String name = tfName.getText().trim();
            String cat = tfCategory.getText().trim();
            int qty = Integer.parseInt(tfQty.getText().trim());
            double price = Double.parseDouble(tfPrice.getText().trim());

            int oldQty = sel.getQuantity();
            boolean ok = ProductDAO.update(sel.getId(), name, cat, qty, price);

            if (ok) {
                String newStatus = new Product(sel.getId(), name, cat, qty, price).getStatus();
                HistoryDAO.log("UPDATE", name, "Qty: " + oldQty + " → " + qty + " | Status: " + newStatus);
            }

            status.setText(ok ? "Product updated." : "Update failed.");
            refresh();
        } catch (Exception ex) {
            status.setText("Invalid input (qty/price must be numbers).");
        }
    }

    private void removeSelected() {
        Product sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            status.setText("Select a product first.");
            return;
        }

        boolean ok = ProductDAO.delete(sel.getId());
        if (ok) {
            HistoryDAO.log("DELETE", sel.getName(), "Removed product.");
        }

        status.setText(ok ? "Product removed." : "Remove failed.");
        refresh();
    }

    public Parent getView() {
        return root;
    }
}
