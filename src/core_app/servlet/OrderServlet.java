package core_app.servlet;

import core_app.dao.InvoiceDAO;
import core_app.model.Customer;
import core_app.model.Invoice;
import core_app.model.InvoiceDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            out.print("[]");
            return;
        }

        Customer user = (Customer) session.getAttribute("currentUser");
        String action = req.getParameter("action");

        List<Invoice> list;
        if ("all".equals(action) && "ADMIN".equals(user.getRole())) {
            list = invoiceDAO.getAllInvoices();
        } else {
            list = invoiceDAO.getInvoicesByCustomerId(user.getCustomerId());
        }

        System.out.println("DEBUG: Sending " + list.size() + " orders to frontend.");

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Invoice inv = list.get(i);
            sb.append("{")
                    .append("\"id\":").append(inv.getInvoiceId()).append(",")
                    .append("\"total\":").append(inv.getTotalAmount()).append(",")
                    .append("\"method\":\"").append(inv.getPaymentMethod()).append("\",")
                    .append("\"customerName\":\"")
                    .append(inv.getCustomerName() != null ? inv.getCustomerName().replace("\"", "\\\"") : "")
                    .append("\",")
                    .append("\"status\":\"").append(inv.getStatus() != null ? inv.getStatus() : "PENDING").append("\",")
                    .append("\"paymentProof\":\"")
                    .append(inv.getPaymentProof() != null ? inv.getPaymentProof().replace("\"", "\\\"") : "")
                    .append("\",")
                    .append("\"day\":").append(inv.getInvoiceDate().getDayOfMonth()).append(",")
                    .append("\"month\":").append(inv.getInvoiceDate().getMonthValue()).append(",")
                    .append("\"year\":").append(inv.getInvoiceDate().getYear()).append(",")
                    .append("\"items\":[");

            List<InvoiceDetail> details = inv.getInvoiceDetails();
            for (int j = 0; j < details.size(); j++) {
                InvoiceDetail d = details.get(j);
                sb.append("{")
                        .append("\"name\":\"").append(d.getMedicineName().replace("\"", "\\\"")).append("\",")
                        .append("\"qty\":").append(d.getQuantity()).append(",")
                        .append("\"price\":").append(d.getUnitPrice()).append(",")
                        .append("\"importPrice\":").append(d.getImportPrice())
                        .append("}");
                if (j < details.size() - 1)
                    sb.append(",");
            }
            sb.append("]}");
            if (i < list.size() - 1)
                sb.append(",");
        }
        sb.append("]");
        out.print(sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }
        String body = sb.toString();

        try {
            Invoice inv = new Invoice();
            inv.setPaymentMethod("BANK_TRANSFER");
            inv.setStatus(val(body, "status"));
            inv.setPaymentProof(val(body, "paymentProof"));

            // Extract totalAmount
            inv.setTotalAmount(Double.parseDouble(val(body, "totalAmount")));

            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                Customer c = (Customer) session.getAttribute("currentUser");
                inv.setCustomerId(c.getCustomerId());
            }

            // Extract items - More robust manual split
            int start = body.indexOf("\"items\":[") + 9;
            int end = body.lastIndexOf("]");
            String itemsJson = body.substring(start, end);
            String[] parts = itemsJson.split("\\},\\{");

            for (String part : parts) {
                String p = part.replace("{", "").replace("}", "");
                String medId = val(p, "id");
                String name = val(p, "name");
                double pr = Double.parseDouble(val(p, "price"));
                int qt = Integer.parseInt(val(p, "qty"));
                inv.addInvoiceDetail(new InvoiceDetail(medId, name, qt, pr));
            }

            invoiceDAO.addInvoice(inv);
            resp.getWriter().print("{\"status\":\"success\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        Customer user = (session != null) ? (Customer) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.setStatus(401);
            resp.getWriter().print("{\"status\":\"error\", \"message\":\"Unauthorized\"}");
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }
        String body = sb.toString();

        try {
            int orderId = Integer.parseInt(val(body, "orderId"));
            String newStatus = val(body, "status");

            // Lấy trạng thái hiện tại và chủ sở hữu của đơn hàng
            String currentStatus = invoiceDAO.getInvoiceStatus(orderId);
            Invoice existingInvoice = null;

            // Tìm đơn hàng này thuộc về ai (có thể tối ưu thêm trong DAO sau)
            List<Invoice> userOrders = invoiceDAO.getInvoicesByCustomerId(user.getCustomerId());
            boolean isOwnOrder = false;
            for (Invoice o : userOrders) {
                if (o.getInvoiceId() == orderId) {
                    isOwnOrder = true;
                    existingInvoice = o;
                    break;
                }
            }
            
            // Nếu là admin, thử lấy thông tin đơn hàng từ list tất cả đơn hàng nếu chưa tìm thấy
            if (existingInvoice == null && "ADMIN".equals(user.getRole())) {
                List<Invoice> allOrders = invoiceDAO.getAllInvoices();
                for (Invoice o : allOrders) {
                    if (o.getInvoiceId() == orderId) {
                        existingInvoice = o;
                        break;
                    }
                }
            }

            boolean canUpdate = false;

            // Case 1: Admin có quyền đổi sang bất kỳ trạng thái nào
            if ("ADMIN".equals(user.getRole())) {
                canUpdate = true;
            }
            // Case 2: User tự hủy đơn của mình nếu đang PENDING
            else if (isOwnOrder && "CANCELLED".equals(newStatus) && "PENDING".equals(currentStatus)) {
                canUpdate = true;
            }

            if (canUpdate) {
                if (invoiceDAO.updateStatus(orderId, newStatus)) {
                    // Nếu trạng thái mới là hủy (hoặc từ chối), hoàn lại số lượng thuốc vào kho
                    if (("CANCELLED".equals(newStatus) || "DENIED".equals(newStatus)) 
                            && existingInvoice != null 
                            && "PENDING".equals(currentStatus)) { // Chỉ hoàn kho nếu đang từ PENDING sang hủy
                        System.out.println("🔄 [OrderServlet] Hoàn trả số lượng kho cho đơn hàng " + orderId);
                        core_app.dao.BatchDAO batchDAO = new core_app.dao.BatchDAO();
                        try (java.sql.Connection conn = core_app.util.DBConnection.getConnection()) {
                            conn.setAutoCommit(false);
                            try {
                                for (InvoiceDetail detail : existingInvoice.getInvoiceDetails()) {
                                    batchDAO.addStock(conn, detail.getMedicineId(), detail.getQuantity());
                                }
                                conn.commit();
                                System.out.println("✅ [OrderServlet] Hoàn trả kho thành công");
                            } catch (Exception ex) {
                                conn.rollback();
                                System.err.println("❌ [OrderServlet] Lỗi khi hoàn trả kho: " + ex.getMessage());
                            }
                        } catch (Exception e) {
                            System.err.println("❌ [OrderServlet] Lỗi kết nối CSDL khi hoàn trả kho: " + e.getMessage());
                        }
                    }
                    
                    resp.getWriter().print("{\"status\":\"success\"}");
                } else {
                    resp.getWriter().print("{\"status\":\"error\", \"message\":\"Update failed\"}");
                }
            } else {
                resp.setStatus(403);
                resp.getWriter().print("{\"status\":\"error\", \"message\":\"Permission denied\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private String val(String s, String k) {
        String p = "\"" + k + "\":";
        int idx = s.indexOf(p);
        if (idx == -1)
            return "";
        int st = idx + p.length();
        while (st < s.length() && (s.charAt(st) == ':' || s.charAt(st) == ' '))
            st++;

        if (s.charAt(st) == '\"') {
            st++;
            int ed = s.indexOf("\"", st);
            return s.substring(st, ed);
        } else {
            int ed = st;
            while (ed < s.length() && (Character.isDigit(s.charAt(ed)) || s.charAt(ed) == '.'))
                ed++;
            return s.substring(st, ed);
        }
    }
}
