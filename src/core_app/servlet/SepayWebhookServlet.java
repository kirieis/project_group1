package core_app.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core_app.dao.InvoiceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/api/sepay/webhook")
public class SepayWebhookServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 1. Lấy body của request từ SePay
            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // 2. Lấy nội dung chuyển khoản và mã đơn hàng (mã thanh toán)
            // SePay tự bóc tách mã ở trường "code" nếu bro cấu hình đúng
            String code = json.has("code") ? json.get("code").getAsString() : "";
            String content = json.has("content") ? json.get("content").getAsString() : "";

            // Nếu không có code, thử tìm trong content (Ví dụ content là "DH123")
            if (code == null || code.isEmpty()) {
                code = extractOrderCode(content);
            }

            if (code != null && !code.isEmpty()) {
                try {
                    int invoiceId = Integer.parseInt(code.replaceAll("[^0-9]", ""));

                    // 3. Cập nhật trạng thái trong database
                    invoiceDAO.updateStatus(invoiceId, "PAID");
                    System.out.println("✅ SePay Webhook: Đã xác thực đơn hàng #" + invoiceId);
                } catch (NumberFormatException e) {
                    System.err.println("❌ SePay Webhook: Mã đơn hàng không hợp lệ: " + code);
                }
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print("{\"status\":\"success\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private String extractOrderCode(String content) {
        // Logíc tách mã đơn hàng đơn giản: tìm chuỗi "DH" seguido bởi số
        // Bro có thể tùy chỉnh regex này tùy theo cách bro đặt mã
        return content;
    }
}
