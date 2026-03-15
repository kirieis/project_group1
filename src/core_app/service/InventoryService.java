package core_app.service;

import core_app.dao.BatchDAO;
import core_app.dao.BatchDAO.BatchInfo;
import core_app.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * InventoryService – FEFO Logic trung tâm + Hệ thống 3-tier hạn sử dụng.
 *
 * FEFO = First Expired, First Out
 * Khi bán thuốc, hệ thống tự động chọn batch có ngày hết hạn sớm nhất trước.
 * Nếu batch đầu không đủ → tiếp tục lấy từ batch tiếp theo cho đến khi đủ.
 *
 * 3-tier hạn sử dụng:
 * - OK (> 30 ngày): Bán bình thường, giá gốc
 * - NEAR_EXPIRY (16–30 ngày): Sale 30%, tag "SẮP HẾT HẠN"
 * - REMOVED (≤ 15 ngày): Loại khỏi bán, ghi log kiểm kê
 */
public class InventoryService {

    private final BatchDAO batchDAO = new BatchDAO();

    // ============================================================
    // FEFOResult – Kết quả sau khi bán FEFO
    // ============================================================

    public static class FEFOResult {
        public final List<FEFOAllocation> allocations = new ArrayList<>();
        public boolean success;
        public String errorMessage;

        public static class FEFOAllocation {
            public int batchId;
            public String batchNumber;
            public int quantityDeducted;
            public double importPrice;
            public int discountPercent; // 30 nếu batch near-expiry, 0 nếu bình thường

            public FEFOAllocation(int batchId, String batchNumber, int quantityDeducted,
                    double importPrice, int discountPercent) {
                this.batchId = batchId;
                this.batchNumber = batchNumber;
                this.quantityDeducted = quantityDeducted;
                this.importPrice = importPrice;
                this.discountPercent = discountPercent;
            }
        }
    }

    // ============================================================
    // sellFEFO – Bán thuốc theo FEFO (có xử lý discount near-expiry)
    // ============================================================

    /**
     * Bán thuốc theo FEFO – Logic cốt lõi.
     *
     * Quy trình:
     * 1. Lấy tất cả batch khả dụng (> 15 ngày), sắp xếp FEFO
     * 2. Batch ≤ 15 ngày đã bị loại bởi query
     * 3. Batch 16–30 ngày: vẫn bán nhưng ghi nhận discount 30%
     * 4. Trừ kho từ batch hết hạn sớm nhất trước
     * 5. Nếu batch đầu không đủ → chuyển sang batch tiếp theo
     */
    public FEFOResult sellFEFO(String medicineId, int quantity) {
        FEFOResult result = new FEFOResult();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Lấy batch khả dụng, FEFO order (expiry sớm nhất trước)
                // Query đã loại batch ≤ 15 ngày
                List<BatchInfo> batches = batchDAO.getAvailableBatches(conn, medicineId);

                if (batches.isEmpty()) {
                    result.success = false;
                    result.errorMessage = "Thuốc " + medicineId + " đã hết hàng hoặc tất cả lô đã hết hạn/sắp hết hạn";
                    conn.rollback();
                    return result;
                }

                // 2. Tính tổng tồn kho
                int totalAvailable = 0;
                for (BatchInfo b : batches) {
                    totalAvailable += b.quantityAvailable;
                }

                if (totalAvailable < quantity) {
                    result.success = false;
                    result.errorMessage = "Không đủ tồn kho cho thuốc " + medicineId
                            + ". Yêu cầu: " + quantity + ", Còn: " + totalAvailable;
                    conn.rollback();
                    return result;
                }

                // 3. FEFO: Trừ từ batch hết hạn sớm nhất trước
                int remaining = quantity;
                for (BatchInfo batch : batches) {
                    if (remaining <= 0)
                        break;

                    int deduct = Math.min(remaining, batch.quantityAvailable);
                    boolean ok = batchDAO.deductStock(conn, batch.batchId, deduct);
                    if (!ok) {
                        throw new SQLException("Không thể trừ kho batch " + batch.batchId);
                    }

                    // Ghi nhận discount nếu batch nằm trong vùng near-expiry (16–30 ngày)
                    result.allocations.add(new FEFOResult.FEFOAllocation(
                            batch.batchId, batch.batchNumber, deduct,
                            batch.importPrice, batch.discountPercent));

                    remaining -= deduct;

                    String discountTag = batch.discountPercent > 0
                            ? " [SALE -" + batch.discountPercent + "%]"
                            : "";
                    System.out.println("📦 [FEFO] Trừ " + deduct + " từ batch " + batch.batchNumber
                            + " (HSD: " + batch.expiryDate + ", còn " + batch.daysLeft + " ngày"
                            + ", còn lại: " + (batch.quantityAvailable - deduct) + ")" + discountTag);
                }

                conn.commit();
                result.success = true;
                System.out.println("✅ [FEFO] Bán thành công " + quantity + " đơn vị thuốc " + medicineId);
                return result;

            } catch (SQLException e) {
                conn.rollback();
                result.success = false;
                result.errorMessage = "Lỗi giao dịch FEFO: " + e.getMessage();
                System.err.println("❌ [FEFO] Transaction error: " + e.getMessage());
                e.printStackTrace();
                return result;
            }

        } catch (SQLException e) {
            result.success = false;
            result.errorMessage = "Lỗi kết nối DB: " + e.getMessage();
            System.err.println("❌ [FEFO] Connection error: " + e.getMessage());
            return result;
        }
    }

    /**
     * Bán thuốc theo FEFO (Sử dụng Connection có sẵn từ transaction bên ngoài).
     * Dùng khi tạo Invoice để đảm bảo cả Invoice và Stock Deduction nằm trong cùng 1 transaction.
     */
    public FEFOResult sellFEFO(Connection conn, String medicineId, int quantity) throws SQLException {
        FEFOResult result = new FEFOResult();
        
        // 1. Lấy batch khả dụng, FEFO order (expiry sớm nhất trước)
        // Query đã loại batch ≤ 15 ngày
        List<BatchInfo> batches = batchDAO.getAvailableBatches(conn, medicineId);

        if (batches.isEmpty()) {
            result.success = false;
            result.errorMessage = "Thuốc " + medicineId + " đã hết hàng hoặc tất cả lô đã hết hạn/sắp hết hạn";
            return result;
        }

        // 2. Tính tổng tồn kho
        int totalAvailable = 0;
        for (BatchInfo b : batches) {
            totalAvailable += b.quantityAvailable;
        }

        if (totalAvailable < quantity) {
            result.success = false;
            result.errorMessage = "Không đủ tồn kho cho thuốc " + medicineId
                    + ". Yêu cầu: " + quantity + ", Còn: " + totalAvailable;
            return result;
        }

        // 3. FEFO: Trừ từ batch hết hạn sớm nhất trước
        int remaining = quantity;
        for (BatchInfo batch : batches) {
            if (remaining <= 0)
                break;

            int deduct = Math.min(remaining, batch.quantityAvailable);
            boolean ok = batchDAO.deductStock(conn, batch.batchId, deduct);
            if (!ok) {
                throw new SQLException("Không thể trừ kho batch " + batch.batchId);
            }

            // Ghi nhận discount nếu batch nằm trong vùng near-expiry (16–30 ngày)
            result.allocations.add(new FEFOResult.FEFOAllocation(
                    batch.batchId, batch.batchNumber, deduct,
                    batch.importPrice, batch.discountPercent));

            remaining -= deduct;

            String discountTag = batch.discountPercent > 0
                    ? " [SALE -" + batch.discountPercent + "%]"
                    : "";
            System.out.println("📦 [FEFO] Trừ " + deduct + " từ batch " + batch.batchNumber
                    + " (HSD: " + batch.expiryDate + ", còn " + batch.daysLeft + " ngày"
                    + ", còn lại: " + (batch.quantityAvailable - deduct) + ")" + discountTag);
        }

        result.success = true;
        System.out.println("✅ [FEFO] Bán thành công " + quantity + " đơn vị thuốc " + medicineId);
        return result;
    }

    // ============================================================
    // validateStock – Kiểm tra tồn kho trước khi bán
    // ============================================================

    public String validateStock(String medicineId, int quantity) {
        int available = batchDAO.getTotalAvailableQuantity(medicineId);
        if (available < quantity) {
            return "Thuốc " + medicineId + " không đủ tồn kho. Yêu cầu: " + quantity + ", Còn: " + available;
        }
        return null;
    }

    // ============================================================
    // Expiry Alerts & Reports
    // ============================================================

    /** Cảnh báo thuốc sắp hết hạn trong N ngày */
    public List<BatchInfo> getExpiryWarnings(int days) {
        return batchDAO.getExpiringBatches(days);
    }

    /** Thuốc đã hết hạn nhưng còn tồn kho */
    public List<BatchInfo> getExpiredStock() {
        return batchDAO.getExpiredBatches();
    }

    /** Thuốc đang sale 30% (16–30 ngày) */
    public List<BatchInfo> getNearExpirySales() {
        return batchDAO.getNearExpirySaleBatches();
    }

    /** Thuốc bị loại khỏi bán (≤ 15 ngày) – dùng cho kiểm kê */
    public List<BatchInfo> getUnsellableStock() {
        return batchDAO.getUnsellableBatches();
    }
}
