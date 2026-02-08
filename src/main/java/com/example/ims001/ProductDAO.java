package com.example.ims001;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final int LOW_STOCK_THRESHOLD = 10;

    // =========================
    // READ
    // =========================
    public static List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, category, quantity, price FROM products ORDER BY id DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Product> getAllByNameAsc() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, category, quantity, price FROM products ORDER BY name ASC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Alias if other parts call getAllProducts()
    public static List<Product> getAllProducts() {
        return getAllByNameAsc();
    }

    // =========================
    // CREATE
    // =========================
    public static boolean add(String name, String category, int qty, double price) {
        String sql = "INSERT INTO products(name, category, quantity, price) VALUES (?, ?, ?, ?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, category);
            ps.setInt(3, qty);
            ps.setDouble(4, price);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // UPDATE
    // =========================
    public static boolean update(int id, String name, String category, int qty, double price) {
        String sql = "UPDATE products SET name=?, category=?, quantity=?, price=? WHERE id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, category);
            ps.setInt(3, qty);
            ps.setDouble(4, price);
            ps.setInt(5, id);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // DELETE
    // =========================
    public static boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // DASHBOARD SUMMARY
    // =========================
    public static StockSummary getStockSummary() {
        String sql = """
                SELECT
                  COUNT(*) AS total,
                  SUM(CASE WHEN quantity > ? THEN 1 ELSE 0 END) AS in_stock,
                  SUM(CASE WHEN quantity BETWEEN 1 AND ? THEN 1 ELSE 0 END) AS low_stock,
                  SUM(CASE WHEN quantity = 0 THEN 1 ELSE 0 END) AS out_stock
                FROM products
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, LOW_STOCK_THRESHOLD);
            ps.setInt(2, LOW_STOCK_THRESHOLD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StockSummary(
                            rs.getInt("total"),
                            rs.getInt("in_stock"),
                            rs.getInt("low_stock"),
                            rs.getInt("out_stock")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StockSummary(0, 0, 0, 0);
    }

    // =========================
    // SELL CART (Foodpanda checkout)
    // - Updates stock safely
    // - Writes sales + sales_items
    // - Writes history logs (optional but nice)
    // =========================
    public static boolean sellCart(List<CartItem> cart, String username) {
        if (cart == null || cart.isEmpty()) return false;

        String safeUpdate = """
                UPDATE products
                SET quantity = quantity - ?
                WHERE id = ? AND quantity >= ?
                """;

        String insertSale = """
                INSERT INTO sales(username, total_amount, total_items, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """;

        String insertSaleItem = """
                INSERT INTO sales_items(sale_id, product_id, product_name, qty, unit_price, line_total)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String insertHistory = """
                INSERT INTO history(action, item_id, product_name, details, created_at)
                VALUES (?, NULL, ?, ?, CURRENT_TIMESTAMP)
                """;

        Connection con = null;

        try {
            con = DB.getConnection();
            con.setAutoCommit(false);

            // totals
            int totalItems = 0;
            double totalAmount = 0.0;

            for (CartItem item : cart) {
                if (item.getQuantity() <= 0) {
                    con.rollback();
                    return false;
                }
                totalItems += item.getQuantity();
                totalAmount += item.getLineTotal();
            }

            // 1) Insert sales header and get sale_id
            int saleId;
            try (PreparedStatement ps = con.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setDouble(2, totalAmount);
                ps.setInt(3, totalItems);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }
                    saleId = keys.getInt(1);
                }
            }

            // 2) Per item: safe stock update + sales_items + history
            for (CartItem item : cart) {
                int productId = item.getProductId();
                int qty = item.getQuantity();

                // safe update (prevents negative stock)
                int updated;
                try (PreparedStatement ps = con.prepareStatement(safeUpdate)) {
                    ps.setInt(1, qty);
                    ps.setInt(2, productId);
                    ps.setInt(3, qty);
                    updated = ps.executeUpdate();
                }

                if (updated == 0) {
                    con.rollback();
                    return false; // stock changed / not enough
                }

                // sales_items row
                try (PreparedStatement ps = con.prepareStatement(insertSaleItem)) {
                    ps.setInt(1, saleId);
                    ps.setInt(2, productId);
                    ps.setString(3, item.getName());
                    ps.setInt(4, qty);
                    ps.setDouble(5, item.getUnitPrice());
                    ps.setDouble(6, item.getLineTotal());
                    ps.executeUpdate();
                }

                // history log (optional)
                String details = "User: " + username +
                        " | Type: Stock Out | Purpose: SALE | Qty: " + qty +
                        " | SaleID: " + saleId;

                try (PreparedStatement ps = con.prepareStatement(insertHistory)) {
                    ps.setString(1, "STOCK_OUT_SALE");
                    ps.setString(2, item.getName());
                    ps.setString(3, details);
                    ps.executeUpdate();
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;

        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
