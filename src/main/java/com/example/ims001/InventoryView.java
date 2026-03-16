package com.example.ims001;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class InventoryView {

    private final MainApp mainApp;
    private BorderPane root;

    private final TableView<Product> table = new TableView<>();
    private final ObservableList<Product> masterData = FXCollections.observableArrayList();

    private FilteredList<Product> filtered;
    private SortedList<Product> sorted;

    private final TextField tfName = new TextField();
    private final TextField tfCategory = new TextField();
    private final TextField tfQty = new TextField();
    private final TextField tfPrice = new TextField();
    private final TextField tfSearch = new TextField();

    private final Label status = new Label();
    private final Label stockWarning = new Label();

    private boolean lowStockFirstEnabled = false;
    private boolean warnedOnce = false;

    private String selectedImagePath = null;

    public InventoryView(MainApp mainApp) {
        this.mainApp = mainApp;
        buildUI();
        setupFilterAndSort();
        refresh();
    }

    private void buildUI() {
        VBox page = new VBox(16);
        page.getStyleClass().add("inventory-layout");
        page.setPadding(new Insets(4, 4, 12, 4));
        page.setFillWidth(true);

        HBox toolbar = buildToolbar();
        VBox tableCard = buildTableCard();
        VBox formCard = buildFormCard();

        page.getChildren().addAll(toolbar, tableCard, formCard);

        // Keep table nicely sized inside a scrollable page
        tableCard.setMinHeight(420);
        tableCard.setPrefHeight(520);
        tableCard.setMaxHeight(Region.USE_PREF_SIZE);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("inventory-scroll");

        AppShell shell = new AppShell(mainApp, Session.getUsername(), "inventory", "Inventory", scrollPane);
        root = shell.getRoot();

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app-shell.css")).toExternalForm());
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/inventory-view.css")).toExternalForm());
    }

    private HBox buildToolbar() {
        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("action-btn");
        btnRefresh.setOnAction(e -> refresh());

        Button btnLowStockFirst = new Button("Low Stock First: OFF");
        btnLowStockFirst.getStyleClass().add("action-btn");
        btnLowStockFirst.setOnAction(e -> {
            lowStockFirstEnabled = !lowStockFirstEnabled;
            btnLowStockFirst.setText(lowStockFirstEnabled ? "Low Stock First: ON" : "Low Stock First: OFF");
            applySort();
        });

        tfSearch.setPromptText("Search (name / category / status)...");
        tfSearch.getStyleClass().add("text-input");
        tfSearch.setPrefWidth(380);

        stockWarning.getStyleClass().add("label-warning");

        HBox row = new HBox(10, btnRefresh, tfSearch, btnLowStockFirst, stockWarning);
        row.getStyleClass().add("history-toolbar");
        return row;
    }

    private VBox buildTableCard() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(74);
        table.setPrefHeight(500);

        TableColumn<Product, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject()
        );
        colId.setCellFactory(col -> createCenteredCell());

        TableColumn<Product, Integer> colImg = new TableColumn<>("Image");
        colImg.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject()
        );

        colImg.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            private final StackPane wrapper = new StackPane(iv);

            {
                iv.setFitWidth(64);
                iv.setFitHeight(64);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);

                wrapper.setMaxWidth(Double.MAX_VALUE);
                wrapper.setMaxHeight(Double.MAX_VALUE);

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Integer productId, boolean empty) {
                super.updateItem(productId, empty);

                if (empty || productId == null) {
                    setGraphic(null);
                    return;
                }

                String path = ProductImageDAO.getImagePath(productId);
                if (path == null || path.isBlank()) {
                    setGraphic(null);
                    return;
                }

                try {
                    iv.setImage(new javafx.scene.image.Image("file:" + path, true));
                    setGraphic(wrapper);
                } catch (Exception e) {
                    setGraphic(null);
                }
            }
        });

        TableColumn<Product, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getName())
        );
        colName.setCellFactory(col -> createCenteredCell());

        TableColumn<Product, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory())
        );
        colCat.setCellFactory(col -> createCenteredCell());

        TableColumn<Product, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()).asObject()
        );
        colQty.setCellFactory(col -> createCenteredCell());

        TableColumn<Product, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()).asObject()
        );
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);

                if (empty || price == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("₱%.2f", price));
                    setGraphic(null);
                }

                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        TableColumn<Product, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus())
        );

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
                setAlignment(javafx.geometry.Pos.CENTER);
                switch (s) {
                    case "OUT OF STOCK" -> setStyle("-fx-text-fill: #E11D48; -fx-font-weight: bold;");
                    case "LOW STOCK" -> setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    case "IN STOCK" -> setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        table.getColumns().setAll(colId, colImg, colName, colCat, colQty, colPrice, colStatus);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                tfName.setText(sel.getName());
                tfCategory.setText(sel.getCategory());
                tfQty.setText(String.valueOf(sel.getQuantity()));
                tfPrice.setText(String.valueOf(sel.getPrice()));
                selectedImagePath = ProductImageDAO.getImagePath(sel.getId());
            }
        });

        VBox box = new VBox(table);
        box.getStyleClass().add("card");
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildFormCard() {
        tfName.setPromptText("Name");
        tfCategory.setPromptText("Category");
        tfQty.setPromptText("Quantity");
        tfPrice.setPromptText("Price");

        tfName.getStyleClass().add("text-input");
        tfCategory.getStyleClass().add("text-input");
        tfQty.getStyleClass().add("text-input");
        tfPrice.getStyleClass().add("text-input");

        Button btnAdd = new Button("Add Product");
        btnAdd.getStyleClass().add("action-btn");
        btnAdd.setOnAction(e -> add());

        Button btnEdit = new Button("Edit Selected");
        btnEdit.getStyleClass().add("action-btn");
        btnEdit.setOnAction(e -> editSelected());

        Button btnRemove = new Button("Remove Selected");
        btnRemove.getStyleClass().add("action-btn");
        btnRemove.setOnAction(e -> removeSelected());

        Button btnChooseImage = new Button("Choose Image");
        btnChooseImage.getStyleClass().add("action-btn");
        btnChooseImage.setOnAction(e -> chooseImage());

        GridPane form = new GridPane();
        form.getStyleClass().add("inventory-form-grid");
        form.addRow(0, new Label("Name"), tfName);
        form.addRow(1, new Label("Category"), tfCategory);
        form.addRow(2, new Label("Qty"), tfQty);
        form.addRow(3, new Label("Price"), tfPrice);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(80);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().setAll(c1, c2);

        HBox actions = new HBox(10, btnAdd, btnEdit, btnRemove, btnChooseImage);
        actions.getStyleClass().add("inventory-actions");

        status.getStyleClass().add("inventory-status");

        VBox box = new VBox(10, form, actions, status);
        box.getStyleClass().add("card");
        return box;
    }

    private <T> TableCell<Product, T> createCenteredCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.valueOf(item));
                    setGraphic(null);
                }

                setAlignment(javafx.geometry.Pos.CENTER);
            }
        };
    }

    private void setupFilterAndSort() {
        filtered = new FilteredList<>(masterData, p -> true);
        sorted = new SortedList<>(filtered);

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
            String statusText = safe(p.getStatus());

            return name.contains(q) || cat.contains(q) || statusText.contains(q);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private void applySort() {
        if (!lowStockFirstEnabled) {
            sorted.comparatorProperty().bind(table.comparatorProperty());
            table.sort();
            return;
        }

        Comparator<Product> cmp = Comparator
                .comparingInt(this::statusRank)
                .thenComparingInt(Product::getQuantity)
                .thenComparing(p -> p.getName() == null ? "" : p.getName().toLowerCase());

        sorted.comparatorProperty().unbind();
        sorted.setComparator(cmp);
        table.sort();
    }

    private int statusRank(Product p) {
        int q = p.getQuantity();
        if (q == 0) return 0;
        if (q <= 10) return 1;
        return 2;
    }

    private void refresh() {
        masterData.setAll(ProductDAO.getAll());

        int low = 0, out = 0;
        for (Product p : masterData) {
            if (p.getQuantity() == 0) out++;
            else if (p.getQuantity() <= 10) low++;
        }

        status.setText("Loaded " + masterData.size() + " products.");

        stockWarning.getStyleClass().removeAll("label-success", "label-warning");
        if (out > 0 || low > 0) {
            stockWarning.setText("⚠ Low: " + low + " | Out: " + out);
            stockWarning.getStyleClass().add("label-warning");
        } else {
            stockWarning.setText("All stock healthy ✅");
            stockWarning.getStyleClass().add("label-success");
        }

        applyFilter(tfSearch.getText());
        applySort();

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
                HistoryDAO.log("ADD", name,
                        "Added (" + qty + " pcs) | Status: " + tempStatus,
                        Session.getUsername());
            }

            status.setText(ok ? "Product added." : "Add failed.");
            selectedImagePath = null;
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
                HistoryDAO.log("UPDATE", name,
                        "Qty: " + oldQty + " → " + qty + " | Status: " + newStatus,
                        Session.getUsername());
                if (selectedImagePath != null && !selectedImagePath.isBlank()) {
                    ProductImageDAO.upsertImagePath(sel.getId(), selectedImagePath);
                }
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
            HistoryDAO.log("DELETE", sel.getName(),
                    "Removed product.",
                    Session.getUsername());}

        status.setText(ok ? "Product removed." : "Remove failed.");
        selectedImagePath = null;
        refresh();
    }

    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Product Image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fc.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;

        try {
            Path imagesDir = Paths.get("product_images");
            if (!Files.exists(imagesDir)) Files.createDirectories(imagesDir);

            String ext = getFileExtension(file.getName());
            String newName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);

            Path target = imagesDir.resolve(newName);
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = target.toAbsolutePath().toString();
            status.setText("Image selected. Click 'Edit Selected' to save.");
        } catch (Exception ex) {
            status.setText("Failed to select image.");
            ex.printStackTrace();
        }
    }

    private String getFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0) return "";
        return name.substring(dot + 1);
    }

    public Parent getView() {
        return root;
    }
}