package com.example.ims001;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class DashboardView {

    private final MainApp mainApp;
    private final String username;
    private final BorderPane root = new BorderPane();

    // Stock summary (inventory health)
    private Label lblSummary;
    private Label lblWarn;

    // Left: products
    private final TableView<Product> productsTable = new TableView<>();
    private final ObservableList<Product> productsData = FXCollections.observableArrayList();

    // Right: cart
    private final TableView<CartItem> cartTable = new TableView<>();
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();

    // State
    private final Map<Integer, CartItem> cartMap = new HashMap<>();
    private final Map<Integer, Boolean> selectedMap = new HashMap<>();

    // Totals
    private Label lblItemsCount;
    private Label lblTotal;
    private Label lblMsg;

    // Search
    private TextField txtSearch;

    public DashboardView(MainApp mainApp, String username) {
        this.mainApp = mainApp;
        this.username = username;
        buildUI();
        refreshAll();
    }

    private void buildUI() {
        // =========================
        // TOP BAR
        // =========================
        Label lblTopTitle = new Label("Dashboard");
        lblTopTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        lblTopTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTopTitle, Priority.ALWAYS);

        Button btnInventory = new Button("Inventory");
        btnInventory.setOnAction(e -> mainApp.showInventoryView());

        Button btnHistory = new Button("History");
        btnHistory.setOnAction(e -> mainApp.showHistoryView());

        Button btnTheme = new Button(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        btnTheme.setOnAction(e -> {
            // get current scene and toggle
            ThemeManager.toggle(root.getScene());
            btnTheme.setText(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        });


        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> mainApp.showLoginView());

        HBox topBar = new HBox(10, lblTopTitle, btnInventory, btnHistory, btnTheme, btnLogout);
        topBar.setPadding(new Insets(12, 15, 12, 15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        // =========================
        // SUMMARY (inventory health)
        // =========================
        Label lblUser = new Label("Logged in as: " + username);
        lblUser.setStyle("-fx-font-size: 14px;");

        lblSummary = new Label("Loading summary...");
        lblSummary.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        lblWarn = new Label("");
        lblWarn.setStyle("-fx-font-weight: bold;");

        VBox summaryBox = new VBox(6, lblUser, lblSummary, lblWarn);
        summaryBox.setPadding(new Insets(10));
        summaryBox.setAlignment(Pos.CENTER_LEFT);

        // =========================
        // TRANSACTIONS AREA
        // =========================
        VBox leftProducts = buildProductsPane();
        VBox rightCart = buildCartPane();

        HBox transactionsArea = new HBox(15, leftProducts, rightCart);
        transactionsArea.setPadding(new Insets(10));
        HBox.setHgrow(leftProducts, Priority.ALWAYS);

        VBox center = new VBox(12, summaryBox, new Separator(), transactionsArea);
        center.setPadding(new Insets(15));
        root.setCenter(center);
    }

    // =========================
    // PRODUCTS PANE (left)
    // =========================
    private VBox buildProductsPane() {
        Label title = new Label("Products");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Search product...");
        txtSearch.textProperty().addListener((obs, oldV, newV) -> applySearch(newV));

        TableColumn<Product, String> colSelect = new TableColumn<>("✓");
        colSelect.setPrefWidth(50);
        colSelect.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();

            {
                cb.setOnAction(e -> {
                    Product p = getCurrentItem();
                    if (p == null) return;

                    boolean checked = cb.isSelected();
                    selectedMap.put(p.getId(), checked);

                    if (checked) {
                        if (p.getQuantity() <= 0) {
                            cb.setSelected(false);
                            selectedMap.put(p.getId(), false);
                            return;
                        }
                        CartItem item = cartMap.get(p.getId());
                        if (item == null) {
                            cartMap.put(p.getId(), new CartItem(p, 1));
                        } else {
                            item.setQuantity(Math.max(1, item.getQuantity()));
                        }
                    } else {
                        cartMap.remove(p.getId());
                    }

                    syncCartData();
                    productsTable.refresh();
                });
            }

            private Product getCurrentItem() {
                if (getIndex() < 0 || getIndex() >= productsTable.getItems().size()) return null;
                return productsTable.getItems().get(getIndex());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product p = getCurrentItem();
                    if (p == null) {
                        setGraphic(null);
                        return;
                    }
                    cb.setDisable(p.getQuantity() <= 0);
                    cb.setSelected(Boolean.TRUE.equals(selectedMap.get(p.getId())));
                    setGraphic(cb);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Product, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(220);

        TableColumn<Product, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colCat.setPrefWidth(160);

        TableColumn<Product, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());
        colStock.setPrefWidth(80);

        TableColumn<Product, String> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(String.format("₱%.2f", c.getValue().getPrice())));
        colPrice.setPrefWidth(90);

        TableColumn<Product, String> colQty = new TableColumn<>("Qty");
        colQty.setPrefWidth(180);
        colQty.setCellFactory(tc -> new TableCell<>() {
            private final Button minus = new Button("-");
            private final Button plus = new Button("+");
            private final Label qtyLbl = new Label("0");
            private final HBox box = new HBox(8, minus, qtyLbl, plus);

            {
                box.setAlignment(Pos.CENTER);

                minus.setOnAction(e -> {
                    Product p = getCurrentProduct();
                    if (p == null) return;

                    CartItem ci = cartMap.get(p.getId());
                    if (ci == null) return;

                    int newQty = ci.getQuantity() - 1;
                    if (newQty <= 0) {
                        cartMap.remove(p.getId());
                        selectedMap.put(p.getId(), false);
                    } else {
                        ci.setQuantity(newQty);
                    }

                    syncCartData();
                    productsTable.refresh();
                });

                plus.setOnAction(e -> {
                    Product p = getCurrentProduct();
                    if (p == null) return;
                    if (p.getQuantity() <= 0) return;

                    if (!Boolean.TRUE.equals(selectedMap.get(p.getId()))) {
                        selectedMap.put(p.getId(), true);
                    }

                    CartItem ci = cartMap.get(p.getId());
                    if (ci == null) ci = new CartItem(p, 0);

                    int newQty = ci.getQuantity() + 1;
                    if (newQty > p.getQuantity()) return;

                    ci.setQuantity(newQty);
                    cartMap.put(p.getId(), ci);

                    syncCartData();
                    productsTable.refresh();
                });
            }

            private Product getCurrentProduct() {
                if (getIndex() < 0 || getIndex() >= productsTable.getItems().size()) return null;
                return productsTable.getItems().get(getIndex());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Product p = getCurrentProduct();
                if (p == null) {
                    setGraphic(null);
                    return;
                }

                CartItem ci = cartMap.get(p.getId());
                int qty = (ci == null) ? 0 : ci.getQuantity();
                qtyLbl.setText(String.valueOf(qty));

                boolean selected = Boolean.TRUE.equals(selectedMap.get(p.getId()));
                minus.setDisable(!selected || qty <= 0);
                plus.setDisable(!selected || qty >= p.getQuantity() || p.getQuantity() <= 0);

                setGraphic(box);
            }
        });

        productsTable.getColumns().setAll(colSelect, colName, colCat, colStock, colPrice, colQty);
        productsTable.setItems(productsData);
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productsTable.setPrefHeight(520);

        VBox left = new VBox(10, title, txtSearch, productsTable);
        left.setPadding(new Insets(12));
        left.setStyle("""
                -fx-background-color: rgba(255,255,255,0.06);
                -fx-background-radius: 12;
                -fx-border-color: rgba(255,255,255,0.12);
                -fx-border-radius: 12;
                """);

        return left;
    }

    // =========================
    // CART PANE (right)
    // =========================
    private VBox buildCartPane() {
        Label title = new Label("Cart Summary");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableColumn<CartItem, String> colName = new TableColumn<>("Product");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(220);

        TableColumn<CartItem, String> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colQty.setPrefWidth(60);

        TableColumn<CartItem, String> colUnit = new TableColumn<>("Price");
        colUnit.setCellValueFactory(c -> new SimpleStringProperty(String.format("₱%.2f", c.getValue().getUnitPrice())));
        colUnit.setPrefWidth(80);

        TableColumn<CartItem, String> colLine = new TableColumn<>("Total");
        colLine.setCellValueFactory(c -> new SimpleStringProperty(String.format("₱%.2f", c.getValue().getLineTotal())));
        colLine.setPrefWidth(90);

        TableColumn<CartItem, String> colRemove = new TableColumn<>("X");
        colRemove.setPrefWidth(80);
        colRemove.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Remove");

            {
                btn.setOnAction(e -> {
                    CartItem ci = getCurrentItem();
                    if (ci == null) return;
                    cartMap.remove(ci.getProductId());
                    selectedMap.put(ci.getProductId(), false);
                    syncCartData();
                    productsTable.refresh();
                });
                btn.setMaxWidth(Double.MAX_VALUE);
            }

            private CartItem getCurrentItem() {
                if (getIndex() < 0 || getIndex() >= cartTable.getItems().size()) return null;
                return cartTable.getItems().get(getIndex());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        cartTable.getColumns().setAll(colName, colQty, colUnit, colLine, colRemove);
        cartTable.setItems(cartData);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartTable.setPrefHeight(420);

        lblItemsCount = new Label("Items: 0");
        lblTotal = new Label("Total: ₱0.00");
        lblTotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnCheckout = new Button("Confirm Sale");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);
        btnCheckout.setOnAction(e -> checkout());

        Button btnClear = new Button("Clear Cart");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> clearCart());

        lblMsg = new Label("");
        lblMsg.setStyle("-fx-font-size: 13px;");

        VBox right = new VBox(10,
                title,
                cartTable,
                new Separator(),
                lblItemsCount,
                lblTotal,
                btnCheckout,
                btnClear,
                lblMsg
        );

        right.setPadding(new Insets(12));
        right.setPrefWidth(440);
        right.setStyle("""
                -fx-background-color: rgba(255,255,255,0.06);
                -fx-background-radius: 12;
                -fx-border-color: rgba(255,255,255,0.12);
                -fx-border-radius: 12;
                """);

        return right;
    }

    // =========================
    // LOGIC
    // =========================
    private void applySearch(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            productsData.setAll(ProductDAO.getAllByNameAsc());
            return;
        }

        List<Product> all = ProductDAO.getAllByNameAsc();
        List<Product> filtered = new ArrayList<>();
        for (Product p : all) {
            if (p.getName().toLowerCase().contains(q) ||
                    p.getCategory().toLowerCase().contains(q)) {
                filtered.add(p);
            }
        }
        productsData.setAll(filtered);
    }

    private void syncCartData() {
        cartData.setAll(cartMap.values());
        updateTotals();
    }

    private void updateTotals() {
        int itemCount = 0;
        double total = 0;

        for (CartItem ci : cartData) {
            itemCount += ci.getQuantity();
            total += ci.getLineTotal();
        }

        lblItemsCount.setText("Items: " + itemCount);
        lblTotal.setText(String.format("Total: ₱%.2f", total));
    }

    private void clearCart() {
        cartMap.clear();
        selectedMap.clear();
        cartData.clear();
        updateTotals();
        productsTable.refresh();

        lblMsg.setStyle("-fx-text-fill: #03DE82; -fx-font-weight: bold;");
        lblMsg.setText("Cart cleared ✅");
    }

    private void checkout() {
        if (cartData.isEmpty()) {
            lblMsg.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            lblMsg.setText("Cart is empty.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Sale");
        confirm.setHeaderText("Proceed with checkout?");
        confirm.setContentText(lblTotal.getText());

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        boolean ok = ProductDAO.sellCart(new ArrayList<>(cartData), username);

        if (!ok) {
            lblMsg.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            lblMsg.setText("Checkout failed (stock may have changed). Refreshing products...");
            refreshProductsOnly();
            return;
        }

        lblMsg.setStyle("-fx-text-fill: #03DE82; -fx-font-weight: bold;");
        lblMsg.setText("Checkout success ✅");

        clearCart();
        refreshAll();
    }

    private void refreshSummary() {
        StockSummary s = ProductDAO.getStockSummary();

        lblSummary.setText(
                "Total: " + s.getTotal() +
                        " | In Stock: " + s.getInStock() +
                        " | Low: " + s.getLowStock() +
                        " | Out: " + s.getOutStock()
        );

        if (s.getOutStock() > 0 || s.getLowStock() > 0) {
            lblWarn.setText("⚠ Warning: Some items are LOW/OUT OF STOCK");
            lblWarn.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            lblWarn.setText("All stock healthy ✅");
            lblWarn.setStyle("-fx-text-fill: #03DE82; -fx-font-weight: bold;");
        }
    }

    private void refreshProductsOnly() {
        String q = (txtSearch == null) ? "" : txtSearch.getText();
        if (q == null || q.isBlank()) productsData.setAll(ProductDAO.getAllByNameAsc());
        else applySearch(q);

        productsTable.refresh();
    }

    private void refreshAll() {
        refreshSummary();
        refreshProductsOnly();
    }

    public Parent getView() {
        return root;
    }
}
