package core_app.util;

import java.sql.Connection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Optimized DB Connection using HikariCP Pool.
 * Essential for handling high-concurrency stress testing without crashing.
 */
public class DBConnection {

    private static final HikariDataSource dataSource;

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(
                    "jdbc:sqlserver://localhost:1433;databaseName=PharmacyDB;encrypt=false;trustServerCertificate=true");
            config.setUsername("sa");
            config.setPassword("123456");

            // Pool Configuration for Stress Testing (350+ threads)
            config.setMaximumPoolSize(50); // Mở tối đa 50 kết nối thực sự cùng lúc (SQL Server chịu được tốt)
            config.setMinimumIdle(10);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(30000); // 30s timeout cho mỗi request xin kết nối
            config.setLeakDetectionThreshold(2000); // Phát hiện rò rỉ kết nối nếu quên đóng

            dataSource = new HikariDataSource(config);
            System.out.println("🚀 [DBConnection] HikariCP Pool Initialized Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Failed to initialize HikariCP Pool", e);
        }
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            System.err.println("🔴 [DBConnection] Error getting connection from pool: " + e.getMessage());
            throw new RuntimeException("❌ DB Connection failed", e);
        }
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
