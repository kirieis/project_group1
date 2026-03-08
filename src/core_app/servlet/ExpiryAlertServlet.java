package core_app.servlet;

import core_app.dao.BatchDAO.BatchInfo;
import core_app.model.Customer;
import core_app.service.InventoryService;
import core_app.util.ExpiryLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * API cảnh báo thuốc sắp hết hạn / đã hết hạn + Xuất báo cáo kiểm kê.
 *
 * GET /api/expiry-alerts?days=30 → batch sắp hết hạn trong 30 ngày
 * GET /api/expiry-alerts?type=expired → batch đã hết hạn nhưng còn tồn kho
 * GET /api/expiry-alerts?type=unsellable → batch bị loại khỏi bán (≤ 15 ngày)
 * GET /api/expiry-alerts?type=sale → batch đang sale 30% (16–30 ngày)
 * GET /api/expiry-alerts?action=export → xuất file log kiểm kê
 */
@WebServlet("/api/expiry-alerts")
public class ExpiryAlertServlet extends HttpServlet {

    private final InventoryService inventoryService = new InventoryService();
    private final ExpiryLogger expiryLogger = new ExpiryLogger();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Chỉ Admin mới được xem cảnh báo
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.setStatus(401);
            out.print("{\"error\":\"Unauthorized\"}");
            return;
        }

        Customer user = (Customer) session.getAttribute("currentUser");
        if (!"ADMIN".equals(user.getRole())) {
            resp.setStatus(403);
            out.print("{\"error\":\"Admin only\"}");
            return;
        }

        try {
            // === Action: Export file log ===
            String action = req.getParameter("action");
            if ("export".equals(action)) {
                String filePath = expiryLogger.generateReport();
                String reportContent = expiryLogger.generateReportAsString();
                out.print("{\"status\":\"success\", \"filePath\":\"" + escapeJson(filePath)
                        + "\", \"report\":\"" + escapeJson(reportContent) + "\"}");
                return;
            }

            // === Query alerts ===
            String type = req.getParameter("type");
            List<BatchInfo> alerts;

            if ("expired".equals(type)) {
                alerts = inventoryService.getExpiredStock();
            } else if ("unsellable".equals(type)) {
                alerts = inventoryService.getUnsellableStock();
            } else if ("sale".equals(type)) {
                alerts = inventoryService.getNearExpirySales();
            } else {
                int days = 90;
                String daysParam = req.getParameter("days");
                if (daysParam != null) {
                    try {
                        days = Integer.parseInt(daysParam);
                    } catch (NumberFormatException ignored) {
                    }
                }
                alerts = inventoryService.getExpiryWarnings(days);
            }

            // Build JSON
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < alerts.size(); i++) {
                BatchInfo b = alerts.get(i);
                if (i > 0)
                    sb.append(",");
                sb.append("{")
                        .append("\"batchId\":").append(b.batchId).append(",")
                        .append("\"medicineId\":\"").append(escapeJson(b.medicineId)).append("\",")
                        .append("\"medicineName\":\"").append(escapeJson(b.medicineName != null ? b.medicineName : ""))
                        .append("\",")
                        .append("\"batchNumber\":\"").append(escapeJson(b.batchNumber != null ? b.batchNumber : ""))
                        .append("\",")
                        .append("\"expiryDate\":\"").append(b.expiryDate != null ? b.expiryDate.toString() : "")
                        .append("\",")
                        .append("\"daysUntilExpiry\":").append(b.daysUntilExpiry()).append(",")
                        .append("\"daysLeft\":").append(b.daysLeft).append(",")
                        .append("\"expiryStatus\":\"").append(escapeJson(b.getExpiryStatus())).append("\",")
                        .append("\"quantityAvailable\":").append(b.quantityAvailable).append(",")
                        .append("\"importPrice\":").append(b.importPrice).append(",")
                        .append("\"discountPercent\":").append(b.discountPercent)
                        .append("}");
            }
            sb.append("]");

            out.print(sb.toString());

        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
