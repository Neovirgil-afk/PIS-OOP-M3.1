package com.example.ims001;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    public static List<HistoryRecord> getAll() {
        List<HistoryRecord> list = new ArrayList<>();
        String sql = "SELECT id, action, product_name, details, created_at FROM history ORDER BY id DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new HistoryRecord(
                        rs.getInt("id"),
                        rs.getString("action"),
                        rs.getString("product_name"),
                        rs.getString("details"),
                        rs.getString("created_at").toString()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void log(String action, String productName, String details) {
        String sql = "INSERT INTO history(action, product_name, details) VALUES (?, ?, ?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, action);
            ps.setString(2, productName);
            ps.setString(3, details);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
