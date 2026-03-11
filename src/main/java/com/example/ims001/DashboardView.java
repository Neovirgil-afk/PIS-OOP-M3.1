package com.example.ims001;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class DashboardView {

    private final MainApp mainApp;
    private final String username;

    private BorderPane root;

    private Label lblSummary;
    private Label lblWarn;

    private final TableView<Product> productsTable = new TableView<>();
    private final ObservableList<Product> productsData = FXCollections.observableArrayList();

    private final TableView<CartItem> cartTable = new TableView<>();
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();

    private final Map<Integer, CartItem> cartMap = new HashMap<>();
    private final Map<Integer, Boolean> selectedMap = new HashMap<>();

    private Label lblItemsCount;
    private Label lblTotal;
    private Label lblMsg;

    private TextField txtSearch;

    public DashboardView(MainApp mainApp, String username) {
        this.mainApp = mainApp;
        this.username = username;
        buildUI();
        refreshAll();
    }

    private void buildUI() {
        VBox page = new VBox(16);

        HBox headerRow = buildHeaderRow();
        HBox body = buildBody();

        page.getChildren().addAll(headerRow, body);
        VBox.setVgrow(body, Priority.ALWAYS);

        AppShell shell = new AppShell(mainApp, username, "home", "Home", page);
        root = shell.getRoot();

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app-shell.css")).toExternalForm());
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/dashboard-view.css")).toExternalForm());
    }

    private HBox buildHeaderRow() {
        Label title = new Label("Home");
        title.getStyleClass().add("pill-title");

        Button btnSell = new Button("Sell");
        btnSell.getStyleClass().add("primary-pill");

        HBox left = new HBox(12, title, btnSell);
        left.getStyleClass().add("dashboard-left-header");

        lblSummary = new Label("Loading summary...");
        lblSummary.getStyleClass().add("label-muted");

        lblWarn = new Label("");

        VBox summaryBox = new VBox(3, lblSummary, lblWarn);
        summaryBox.getStyleClass().add("dashboard-summary-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(18, left, spacer, summaryBox);
        row.getStyleClass().add("dashboard-header-row");
        return row;
    }

    private HBox buildBody() {
        VBox left = buildProductsPane();
        VBox right = buildCartPane();

        left.getStyleClass().addAll("card", "dashboard-left");
        right.getStyleClass().addAll("card", "dashboard-right");

        HBox body = new HBox(20, left, right);
        body.getStyleClass().add("dashboard-body");
        HBox.setHgrow(left, Priority.ALWAYS);
        VBox.setVgrow(left, Priority.ALWAYS);

        return body;
    }

    private VBox buildProductsPane() {
        txtSearch = new TextField();
        txtSearch.setPromptText("Search");
        txtSearch.getStyleClass().add("text-input");
        txtSearch.textProperty().addListener((obs, oldV, newV) -> applySearch(newV));

        TableColumn<Product, String> colSelect = new TableColumn<>("✓");
        colSelect.setPrefWidth(45);
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

        TableColumn<Product, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));

        TableColumn<Product, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());

        TableColumn<Product, String> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(String.format("₱%.2f", c.getValue().getPrice())));

        TableColumn<Product, String> colQty = new TableColumn<>("Qty");
        colQty.setPrefWidth(140);
        colQty.setCellFactory(tc -> new TableCell<>() {
            private final Button minus = new Button("-");
            private final Button plus = new Button("+");
            private final Label qtyLbl = new Label("0");
            private final HBox box = new HBox(6, minus, qtyLbl, plus);

            {
                minus.getStyleClass().add("qty-btn");
                plus.getStyleClass().add("qty-btn");
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
        VBox.setVgrow(productsTable, Priority.ALWAYS);

        return new VBox(10, txtSearch, productsTable);
    }

    private VBox buildCartPane() {
        Label title = new Label("Cart Summary");
        title.getStyleClass().add("cart-title");

        TableColumn<CartItem, String> colName = new TableColumn<>("Product");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<CartItem, String> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));

        TableColumn<CartItem, String> colUnit = new TableColumn<>("Price");
        colUnit.setCellValueFactory(c -> new SimpleStringProperty(String.format("₱%.2f", c.getValue().getUnitPrice())));

        TableColumn<CartItem, String> colLine = new TableColumn<>("Total");
        colLine.setCellValueFactory(c -> new SimpleStringProperty(String.format("₱%.2f", c.getValue().getLineTotal())));

        TableColumn<CartItem, String> colRemove = new TableColumn<>("X");
        colRemove.setPrefWidth(90);
        colRemove.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Remove");

            {
                btn.getStyleClass().add("action-btn");
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
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        lblItemsCount = new Label("ITEM: 0");
        lblItemsCount.getStyleClass().add("label-muted");

        lblTotal = new Label("TOTAL: ₱0.00");
        lblTotal.getStyleClass().add("label-muted");

        Button btnCheckout = new Button("CONFIRM SALE");
        btnCheckout.getStyleClass().add("action-btn");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);
        btnCheckout.setOnAction(e -> checkout());

        Button btnClear = new Button("CLEAR CART");
        btnClear.getStyleClass().add("action-btn");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> {
            clearCart();
            lblMsg.getStyleClass().removeAll("label-warning");
            if (!lblMsg.getStyleClass().contains("label-success")) lblMsg.getStyleClass().add("label-success");
            lblMsg.setText("Cart cleared ✅");
        });

        lblMsg = new Label("");

        VBox box = new VBox(10, title, cartTable, new Separator(), lblItemsCount, lblTotal, btnCheckout, btnClear, lblMsg);
        return box;
    }

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

        lblItemsCount.setText("ITEM: " + itemCount);
        lblTotal.setText(String.format("TOTAL: ₱%.2f", total));
    }

    private void clearCart() {
        cartMap.clear();
        selectedMap.clear();
        cartData.clear();
        updateTotals();
        productsTable.refresh();
    }

    private void checkout() {
        if (cartData.isEmpty()) {
            lblMsg.getStyleClass().removeAll("label-success");
            if (!lblMsg.getStyleClass().contains("label-warning")) lblMsg.getStyleClass().add("label-warning");
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
            lblMsg.getStyleClass().removeAll("label-success");
            if (!lblMsg.getStyleClass().contains("label-warning")) lblMsg.getStyleClass().add("label-warning");
            lblMsg.setText("Checkout failed. Refreshing products...");
            refreshProductsOnly();
            return;
        }

        clearCart();
        refreshAll();

        lblMsg.getStyleClass().removeAll("label-warning");
        if (!lblMsg.getStyleClass().contains("label-success")) lblMsg.getStyleClass().add("label-success");
        lblMsg.setText("Checkout success ✅");
    }

    private void refreshSummary() {
        StockSummary s = ProductDAO.getStockSummary();

        lblSummary.setText(
                "Total: " + s.getTotal() +
                        " | In Stock: " + s.getInStock() +
                        " | Low: " + s.getLowStock() +
                        " | Out: " + s.getOutStock()
        );

        lblWarn.getStyleClass().removeAll("label-warning", "label-success");
        if (s.getOutStock() > 0 || s.getLowStock() > 0) {
            lblWarn.setText("⚠ Some items are LOW/OUT OF STOCK");
            lblWarn.getStyleClass().add("label-warning");
        } else {
            lblWarn.setText("All stock healthy ✅");
            lblWarn.getStyleClass().add("label-success");
        }
    }

    private void refreshProductsOnly() {
        String q = (txtSearch == null) ? "" : txtSearch.getText();
        if (q == null || q.isBlank()) {
            productsData.setAll(ProductDAO.getAllByNameAsc());
        } else {
            applySearch(q);
        }
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