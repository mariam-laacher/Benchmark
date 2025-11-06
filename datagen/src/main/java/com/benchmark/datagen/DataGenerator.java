package com.benchmark.datagen;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class DataGenerator {
    private static final String URL = "jdbc:postgresql://localhost:5432/benchmark_db";
    private static final String USER = "benchmark_user";
    private static final String PASSWORD = "benchmark_pass";

    public static void main(String[] args) {
        try {
            generateCategories();
            generateItems();
            System.out.println("Data generation completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateCategories() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO category (code, name, updated_at) VALUES (?, ?, NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (int i = 1; i <= 2000; i++) {
                    stmt.setString(1, String.format("CAT%04d", i));
                    stmt.setString(2, "Category " + i);
                    stmt.addBatch();
                    if (i % 100 == 0) {
                        stmt.executeBatch();
                        conn.commit();
                    }
                }
                stmt.executeBatch();
                conn.commit();
            }
        }
    }

    private static void generateItems() throws SQLException {
        Random random = new Random();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO item (sku, name, price, stock, category_id, updated_at) VALUES (?, ?, ?, ?, ?, NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                int itemCount = 0;
                for (int catId = 1; catId <= 2000; catId++) {
                    int itemsPerCategory = 50;
                    for (int j = 0; j < itemsPerCategory; j++) {
                        itemCount++;
                        stmt.setString(1, String.format("SKU%06d", itemCount));
                        stmt.setString(2, "Item " + itemCount);
                        stmt.setBigDecimal(3, BigDecimal.valueOf(random.nextDouble() * 1000 + 10).setScale(2, BigDecimal.ROUND_HALF_UP));
                        stmt.setInt(4, random.nextInt(1000));
                        stmt.setLong(5, catId);
                        stmt.addBatch();
                        if (itemCount % 1000 == 0) {
                            stmt.executeBatch();
                            conn.commit();
                        }
                    }
                }
                stmt.executeBatch();
                conn.commit();
            }
        }
    }
}

