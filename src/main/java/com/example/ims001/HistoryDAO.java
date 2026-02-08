package com.example.ims001;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    // Keep original getAll() so old code won't break
    public static List<HistoryRecord> getAll() {
        return getAllByDateRange(null, null);
    }

    // Generic date range (all actions)
    public static List<HistoryRecord> getAllByDateRange(LocalDate from, LocalDate to) {
        String base = """
                SELECT id, action, product_name, details, created_at
                FROM history
                """;

        return fetchByDateRange(base, from, to, "ORDER BY created_at DESC, id DESC");
    }

    // ✅ Inventory history by date range
    public static List<HistoryRecord> getInventoryHistory(LocalDate from, LocalDate to) {
        String base = """
                SELECT id, action, product_name, details, created_at
                FROM history
                WHERE action IN ('ADD','UPDATE','DELETE','STOCK_IN_PURCHASE','STOCK_IN_RETURN','STOCK_OUT_DAMAGE')
                """;

        return fetchByDateRange(base, from, to, "ORDER BY created_at DESC, id DESC");
    }

    // ✅ Sales history by date range
    public static List<HistoryRecord> getSalesHistory(LocalDate from, LocalDate to) {
        String base = """
                SELECT id, action, product_name, details, created_at
                FROM history
                WHERE action = 'STOCK_OUT_SALE'
                """;

        return fetchByDateRange(base, from, to, "ORDER BY created_at DESC, id DESC");
    }

    // Helper that safely applies date filters
    private static List<HistoryRecord> fetchByDateRange(String baseSql, LocalDate from, LocalDate to, String orderSql) {
        List<HistoryRecord> list = new ArrayList<>();

        boolean hasWhere = baseSql.toLowerCase().contains("where");
        StringBuilder sql = new StringBuilder(baseSql);

        // We filter by created_at between [from 00:00:00] and [to+1day 00:00:00)
        if (from != null) {
            sql.append(hasWhere ? " AND " : " WHERE ");
            sql.append("created_at >= ?");
            hasWhere = true;
        }
        if (to != null) {
            sql.append(hasWhere ? " AND " : " WHERE ");
            sql.append("created_at < ?");
        }

        sql.append("\n").append(orderSql);

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;

            if (from != null) {
                Timestamp start = Timestamp.valueOf(from.atStartOfDay());
                ps.setTimestamp(idx++, start);
            }

            if (to != null) {
                Timestamp endExclusive = Timestamp.valueOf(to.plusDays(1).atStartOfDay());
                ps.setTimestamp(idx++, endExclusive);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HistoryRecord(
                            rs.getInt("id"),
                            rs.getString("action"),
                            rs.getString("product_name"),
                            rs.getString("details"),
                            String.valueOf(rs.getTimestamp("created_at"))
                    ));
                }
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
