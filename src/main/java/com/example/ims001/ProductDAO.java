package com.example.ims001;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

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

    // ✅ Dashboard summary counts (Total / In / Low / Out)
    public static StockSummary getStockSummary() {
        String sql = """
                SELECT
                  COUNT(*) AS total,
                  SUM(CASE WHEN quantity > 5 THEN 1 ELSE 0 END) AS in_stock,
                  SUM(CASE WHEN quantity BETWEEN 1 AND 5 THEN 1 ELSE 0 END) AS low_stock,
                  SUM(CASE WHEN quantity = 0 THEN 1 ELSE 0 END) AS out_stock
                FROM products
                """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new StockSummary(
                        rs.getInt("total"),
                        rs.getInt("in_stock"),
                        rs.getInt("low_stock"),
                        rs.getInt("out_stock")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StockSummary(0, 0, 0, 0);
    }
}
