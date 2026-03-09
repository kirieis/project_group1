package core_app.util;

import core_app.dao.BatchDAO;
import core_app.dao.BatchDAO.BatchInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ExpiryLogger – Ghi log thuốc sắp hết hạn / đã hết hạn ra file.
 *
 * File output: logs/expired_medicines_log.txt
 * Ghi lại:
 * - Thuốc bị loại khỏi bán (≤ 15 ngày)
 * - Thuốc đã hết hạn (≤ 0 ngày)
 * - Tổng giá trị tồn kho cần xử lý
 */
public class ExpiryLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "expired_medicines_log.txt";
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BatchDAO batchDAO = new BatchDAO();

    /**
     * Tạo báo cáo đầy đủ và ghi ra file.
     * 
     * @return Đường dẫn file log đã tạo
     */
    public String generateReport() throws IOException {
        List<BatchInfo> unsellable = batchDAO.getUnsellableBatches();
        List<BatchInfo> expired = batchDAO.getExpiredBatches();

        // Tạo thư mục logs nếu chưa có
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = LOG_DIR + File.separator + LOG_FILE;

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            writeHeader(pw);
            writeUnsellableSection(pw, unsellable);
            writeExpiredSection(pw, expired);
            writeSummary(pw, unsellable, expired);
        }

        System.out.println("📝 [ExpiryLogger] Báo cáo đã ghi tại: " + filePath);
        return filePath;
    }

    /**
     * Ghi báo cáo và trả về nội dung dưới dạng String (để trả về qua API).
     */
    public String generateReportAsString() {
        List<BatchInfo> unsellable = batchDAO.getUnsellableBatches();
        List<BatchInfo> expired = batchDAO.getExpiredBatches();

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  BÁO CÁO THUỐC HẾT HẠN / SẮP HẾT HẠN\n");
        sb.append("  Ngày xuất: ").append(LocalDateTime.now().format(DT_FMT)).append("\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        // Unsellable
        sb.append("--- THUỐC BỊ LOẠI KHỎI BÁN (≤ 15 ngày còn lại) ---\n");
        if (unsellable.isEmpty()) {
            sb.append("  (Không có)\n");
        } else {
            for (BatchInfo b : unsellable) {
                sb.append(formatBatchLine(b, "Còn " + b.daysLeft + " ngày")).append("\n");
            }
        }
        sb.append("\n");

        // Expired
        sb.append("--- THUỐC ĐÃ HẾT HẠN ---\n");
        if (expired.isEmpty()) {
            sb.append("  (Không có)\n");
        } else {
            for (BatchInfo b : expired) {
                sb.append(formatBatchLine(b, "Đã hết hạn")).append("\n");
            }
        }
        sb.append("\n");

        // Summary
        double totalValue = calculateTotalValue(unsellable) + calculateTotalValue(expired);
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("  Tổng thuốc bị loại:    ").append(unsellable.size()).append(" lô\n");
        sb.append("  Tổng thuốc hết hạn:    ").append(expired.size()).append(" lô\n");
        sb.append("  Tổng giá trị cần xử lý: ").append(String.format("%,.0f", totalValue)).append("đ\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }

    // ============================================================
    // Private helpers
    // ============================================================

    private void writeHeader(PrintWriter pw) {
        pw.println("═══════════════════════════════════════════════════════════════");
        pw.println("  BÁO CÁO THUỐC HẾT HẠN / SẮP HẾT HẠN");
        pw.println("  Ngày xuất: " + LocalDateTime.now().format(DT_FMT));
        pw.println("═══════════════════════════════════════════════════════════════");
        pw.println();
    }

    private void writeUnsellableSection(PrintWriter pw, List<BatchInfo> unsellable) {
        pw.println("--- THUỐC BỊ LOẠI KHỎI BÁN (≤ " + BatchDAO.DAYS_REMOVE_THRESHOLD + " ngày còn lại) ---");
        if (unsellable.isEmpty()) {
            pw.println("  (Không có)");
        } else {
            for (BatchInfo b : unsellable) {
                pw.println(formatBatchLine(b, "Còn " + b.daysLeft + " ngày"));
            }
        }
        pw.println();
    }

    private void writeExpiredSection(PrintWriter pw, List<BatchInfo> expired) {
        pw.println("--- THUỐC ĐÃ HẾT HẠN ---");
        if (expired.isEmpty()) {
            pw.println("  (Không có)");
        } else {
            for (BatchInfo b : expired) {
                pw.println(formatBatchLine(b, "Đã hết hạn"));
            }
        }
        pw.println();
    }

    private void writeSummary(PrintWriter pw, List<BatchInfo> unsellable, List<BatchInfo> expired) {
        double totalValue = calculateTotalValue(unsellable) + calculateTotalValue(expired);

        pw.println("═══════════════════════════════════════════════════════════════");
        pw.println("  Tổng thuốc bị loại:    " + unsellable.size() + " lô");
        pw.println("  Tổng thuốc hết hạn:    " + expired.size() + " lô");
        pw.printf("  Tổng giá trị cần xử lý: %,.0fđ%n", totalValue);
        pw.println("═══════════════════════════════════════════════════════════════");
    }

    private String formatBatchLine(BatchInfo b, String status) {
        String name = (b.medicineName != null) ? b.medicineName : b.medicineId;
        return String.format("  %-25s | Lô: %-12s | HSD: %s | %-15s | SL: %d | Giá nhập: %,.0fđ",
                name,
                b.batchNumber != null ? b.batchNumber : "N/A",
                b.expiryDate != null ? b.expiryDate.toString() : "N/A",
                status,
                b.quantityAvailable,
                b.importPrice * b.quantityAvailable);
    }

    private double calculateTotalValue(List<BatchInfo> batches) {
        double total = 0;
        for (BatchInfo b : batches) {
            total += b.importPrice * b.quantityAvailable;
        }
        return total;
    }
}
