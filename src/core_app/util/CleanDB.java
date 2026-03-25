package core_app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CleanDB {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=PharmacyDB;encrypt=false;trustServerCertificate=true";
        String user = "sa";
        String pass = "123456";

        try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
            System.out.println("🔧 Đang dọn dẹp Database (Xóa hóa đơn rác và reset tồn kho)...");

            // 1. Xóa toàn bộ hóa đơn rác (đã sinh ra)
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM Invoice_Detail");
                st.executeUpdate("DELETE FROM Invoice");
                System.out.println("✅ Đã xóa sạch dữ liệu Hóa đơn cũ (Invoice/Invoice_Detail).");
            }

            // 2. Chỉnh số lượng âm/giá âm về chuẩn
            String sqlClean = "UPDATE Medicine SET quantity = 1000, price = CASE WHEN price <= 0 THEN 25000 ELSE price END";
            try (Statement st = conn.createStatement()) {
                int rows = st.executeUpdate(sqlClean);
                System.out.println("✅ Đã chuẩn hóa " + rows + " loại thuốc. Tồn kho mỗi loại = 1000 hộp.");
            }

            // 3. In tổng kho ra
            String sqlCheck = "SELECT SUM(CAST(quantity AS BIGINT)) FROM Medicine";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("🚀 TỔNG TỒN KHO MỚI: " + rs.getLong(1) + " hộp.");
                }
            }

            System.out.println("🎉 Database đã sạch bóng và sẵn sàng cho Stress Test!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi dọn dẹp: " + e.getMessage());
        }
    }
}
