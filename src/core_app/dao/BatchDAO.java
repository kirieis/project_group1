package core_app.dao;

import core_app.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * BatchDAO – Quản lý truy vấn liên quan đến Batch (Lô thuốc).
 * 
 * Hệ thống 3-tier quản lý hạn sử dụng:
 * - OK (> 30 ngày): Bán bình thường
 * - NEAR_EXPIRY (16–30 ngày): Sale 30%, vẫn bán được
 * - REMOVED (≤ 15 ngày): Loại khỏi bán, ghi log kiểm kê
 * - EXPIRED (≤ 0 ngày): Đã hết hạn
 */

public class BatchDAO {

    public static final int DAYS_REMOVE_THRESHOLD = 15; // ≤ 15 ngày → loại khỏi bán
    public static final int DAYS_SALE_THRESHOLD = 30; // 16–30 ngày → sale 30%
    public static final int NEAR_EXPIRY_DISCOUNT = 30; // % giảm giá

    public List<BatchInfo> getAvailableBatches(String medicineId) {
        List<BatchInfo> list = new ArrayList<>();
        String sql = """
                SELECT batch_id, medicine_id, batch_number, expiry_date, quantity_available, import_price,
                       DATEDIFF(DAY, CAST(GETDATE() AS DATE), expiry_date) AS days_left
                FROM Batch
                WHERE medicine_id = ?
                  AND expiry_date > DATEADD(DAY, ?, CAST(GETDATE() AS DATE))
                  AND quantity_available > 0
                ORDER BY expiry_date ASC
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicineId);
            ps.setInt(2, DAYS_REMOVE_THRESHOLD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BatchInfo info = mapBatchInfo(rs);
                info.daysLeft = rs.getInt("days_left");
                info.discountPercent = (info.daysLeft <= DAYS_SALE_THRESHOLD) ? NEAR_EXPIRY_DISCOUNT : 0;
                list.add(info);
            }
        } catch (SQLException e) {
            System.err.println("❌ [BatchDAO] getAvailableBatches error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Overload: nhận Connection từ ngoài (dùng trong transaction).
     */
    public List<BatchInfo> getAvailableBatches(Connection conn, String medicineId) throws SQLException {
        List<BatchInfo> list = new ArrayList<>();
        String sql = """
                SELECT batch_id, medicine_id, batch_number, expiry_date, quantity_available, import_price,
                       DATEDIFF(DAY, CAST(GETDATE() AS DATE), expiry_date) AS days_left
                FROM Batch
                WHERE medicine_id = ?
                  AND expiry_date > DATEADD(DAY, ?, CAST(GETDATE() AS DATE))
                  AND quantity_available > 0
                ORDER BY expiry_date ASC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicineId);
            ps.setInt(2, DAYS_REMOVE_THRESHOLD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BatchInfo info = mapBatchInfo(rs);
                info.daysLeft = rs.getInt("days_left");
                info.discountPercent = (info.daysLeft <= DAYS_SALE_THRESHOLD) ? NEAR_EXPIRY_DISCOUNT : 0;
                list.add(info);
            }
        }
        return list;
    }

    // ============================================================
    // getTotalAvailableQuantity – Tổng tồn kho bán được (> 15 ngày)
    // ============================================================

    public int getTotalAvailableQuantity(String medicineId) {
        String sql = """
                SELECT COALESCE(SUM(quantity_available), 0)
                FROM Batch
                WHERE medicine_id = ?
                  AND expiry_date > DATEADD(DAY, ?, CAST(GETDATE() AS DATE))
                  AND quantity_available > 0
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicineId);
            ps.setInt(2, DAYS_REMOVE_THRESHOLD);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ [BatchDAO] getTotalAvailableQuantity error: " + e.getMessage());
        }
        return 0;
    }

    // ============================================================
    // deductStock – Trừ kho batch cụ thể (trong transaction)
    // ============================================================

    public boolean deductStock(Connection conn, int batchId, int quantity) throws SQLException {
        String sql = "UPDATE Batch SET quantity_available = quantity_available - ? WHERE batch_id = ? AND quantity_available >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, batchId);
            ps.setInt(3, quantity);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // ============================================================
    // getExpiringBatches – Batch sắp hết hạn trong N ngày (cảnh báo)
    // ============================================================

    public List<BatchInfo> getExpiringBatches(int daysUntilExpiry) {
        List<BatchInfo> list = new ArrayList<>();
        String sql = """
                SELECT b.batch_id, b.medicine_id, b.batch_number, b.expiry_date,
                       b.quantity_available, b.import_price, m.name AS medicine_name,
                       DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) AS days_left
                FROM Batch b
                JOIN Medicine m ON b.medicine_id = m.medicine_id
                WHERE b.expiry_date > CAST(GETDATE() AS DATE)
                  AND b.expiry_date <= DATEADD(DAY, ?, CAST(GETDATE() AS DATE))
                  AND b.quantity_available > 0
                ORDER BY b.expiry_date ASC
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, daysUntilExpiry);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BatchInfo info = mapBatchInfo(rs);
                info.medicineName = rs.getString("medicine_name");
                info.daysLeft = rs.getInt("days_left");
                info.discountPercent = (info.daysLeft <= DAYS_SALE_THRESHOLD) ? NEAR_EXPIRY_DISCOUNT : 0;
                list.add(info);
            }
        } catch (SQLException e) {
            System.err.println("❌ [BatchDAO] getExpiringBatches error: " + e.getMessage());
        }
        return list;
    }

    // ============================================================
    // getExpiredBatches – Batch ĐÃ HẾT HẠN nhưng còn tồn kho
    // ============================================================

    public List<BatchInfo> getExpiredBatches() {
        List<BatchInfo> list = new ArrayList<>();
        String sql = """
                SELECT b.batch_id, b.medicine_id, b.batch_number, b.expiry_date,
                       b.quantity_available, b.import_price, m.name AS medicine_name,
                       DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) AS days_left
                FROM Batch b
                JOIN Medicine m ON b.medicine_id = m.medicine_id
                WHERE b.expiry_date <= CAST(GETDATE() AS DATE)
                  AND b.quantity_available > 0
                ORDER BY b.expiry_date ASC
                """;

        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                BatchInfo info = mapBatchInfo(rs);
                info.medicineName = rs.getString("medicine_name");
                info.daysLeft = rs.getInt("days_left");
                info.discountPercent = 0;
                list.add(info);
            }
        } catch (SQLException e) {
            System.err.println("❌ [BatchDAO] getExpiredBatches error: " + e.getMessage());
        }
        return list;
    }

    // ============================================================
    // getNearExpirySaleBatches – Batch 16–30 ngày (Sale 30%)
    // ============================================================

    /**
     * Lấy các batch nằm trong vùng SALE (16–30 ngày còn lại).
     * Dùng cho frontend hiển thị tag "SẮP HẾT HẠN -30%".
     */
    public List<BatchInfo> getNearExpirySaleBatches() {
        List<BatchInfo> list = new ArrayList<>();
        String sql = """
                SELECT b.batch_id, b.medicine_id, b.batch_number, b.expiry_date,
                       b.quantity_available, b.import_price, m.name AS medicine_name,
                       DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) AS days_left
                FROM Batch b
                JOIN Medicine m ON b.medicine_id = m.medicine_id
                WHERE DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) > ?
                  AND DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) <= ?
                  AND b.quantity_available > 0
                ORDER BY b.expiry_date ASC
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, DAYS_REMOVE_THRESHOLD);
            ps.setInt(2, DAYS_SALE_THRESHOLD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BatchInfo info = mapBatchInfo(rs);
                info.medicineName = rs.getString("medicine_name");
                info.daysLeft = rs.getInt("days_left");
                info.discountPercent = NEAR_EXPIRY_DISCOUNT;
                list.add(info);
            }
        } catch (SQLException e) {
            System.err.println("❌ [BatchDAO] getNearExpirySaleBatches error: " + e.getMessage());
        }
        return list;
    }

    // ============================================================
    // getUnsellableBatches – Batch ≤ 15 ngày (loại khỏi bán, ghi log)
    // ============================================================

    /**
     * Lấy các batch bị loại khỏi bán (≤ 15 ngày còn lại, chưa hết hạn).
     * Dùng cho ExpiryLogger ghi file kiểm kê.
     */
    public List<BatchInfo> getUnsellableBatches() {
        List<BatchInfo> list = new ArrayList<>();
        String sql = """
                SELECT b.batch_id, b.medicine_id, b.batch_number, b.expiry_date,
                       b.quantity_available, b.import_price, m.name AS medicine_name,
                       DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) AS days_left
                FROM Batch b
                JOIN Medicine m ON b.medicine_id = m.medicine_id
                WHERE b.expiry_date > CAST(GETDATE() AS DATE)
                  AND DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) <= ?
                  AND b.quantity_available > 0
                ORDER BY b.expiry_date ASC
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, DAYS_REMOVE_THRESHOLD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BatchInfo info = mapBatchInfo(rs);
                info.medicineName = rs.getString("medicine_name");
                info.daysLeft = rs.getInt("days_left");
                info.discountPercent = 0;
                list.add(info);
            }
        } catch (SQLException e) {
            System.err.println("❌ [BatchDAO] getUnsellableBatches error: " + e.getMessage());
        }
        return list;
    }

    // ============================================================
    // HELPER – mapBatchInfo
    // ============================================================

    private BatchInfo mapBatchInfo(ResultSet rs) throws SQLException {
        BatchInfo info = new BatchInfo();
        info.batchId = rs.getInt("batch_id");
        info.medicineId = rs.getString("medicine_id");
        info.batchNumber = rs.getString("batch_number");
        Date d = rs.getDate("expiry_date");
        info.expiryDate = (d != null) ? d.toLocalDate() : null;
        info.quantityAvailable = rs.getInt("quantity_available");
        info.importPrice = rs.getDouble("import_price");
        return info;
    }

    // ============================================================
    // DTO – BatchInfo
    // ============================================================

    /**
     * DTO chứa thông tin batch + trạng thái hạn sử dụng.
     */
    public static class BatchInfo {
        public int batchId;
        public String medicineId;
        public String batchNumber;
        public LocalDate expiryDate;
        public int quantityAvailable;
        public double importPrice;
        public String medicineName; // optional, khi JOIN Medicine
        public int daysLeft; // số ngày còn lại
        public int discountPercent; // % giảm giá (30 nếu near-expiry, 0 nếu bình thường)

        public long daysUntilExpiry() {
            if (expiryDate == null)
                return 999;
            return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        }

        /**
         * Trạng thái 4-tier:
         * OK > 30d | NEAR_EXPIRY 16-30d | REMOVED ≤15d | EXPIRED ≤0d
         */
        public String getExpiryStatus() {
            long days = daysUntilExpiry();
            if (days <= 0)
                return "EXPIRED";
            if (days <= BatchDAO.DAYS_REMOVE_THRESHOLD)
                return "REMOVED";
            if (days <= BatchDAO.DAYS_SALE_THRESHOLD)
                return "NEAR_EXPIRY";
            return "OK";
        }
    }
}
