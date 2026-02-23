package core_app.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

@WebServlet("/api/sepay/create-order")
public class SepayOrderServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final Gson gson = new Gson();

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

        try {
            JsonObject jsonBody = gson.fromJson(sb.toString(), JsonObject.class);

            Invoice inv = new Invoice();
            inv.setPaymentMethod("SEPAY");
            inv.setStatus("PENDING");
            inv.setTotalAmount(jsonBody.get("totalAmount").getAsDouble());

            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                Customer c = (Customer) session.getAttribute("currentUser");
                inv.setCustomerId(c.getCustomerId());
            }

            // Parse items
            if (jsonBody.has("items")) {
                jsonBody.getAsJsonArray("items").forEach(itemElement -> {
                    JsonObject itemObj = itemElement.getAsJsonObject();
                    String id = itemObj.get("id").getAsString();
                    String name = itemObj.get("name").getAsString();
                    int qty = itemObj.get("qty").getAsInt();
                    double price = itemObj.get("price").getAsDouble();
                    inv.addInvoiceDetail(new InvoiceDetail(id, name, qty, price));
                });
            }

            // Save to DB to get Invoice ID
            int invoiceId = invoiceDAO.addInvoice(inv);
            if (invoiceId == -1) {
                throw new Exception("Không thể tạo hóa đơn trong hệ thống!");
            }

            // Return invoiceId to frontend to generate QR
            resp.getWriter().print("{\"status\":\"success\", \"invoiceId\":" + invoiceId + "}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
