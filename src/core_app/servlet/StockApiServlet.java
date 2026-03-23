package core_app.servlet;

import core_app.dao.InvoiceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * API Endpoint: /api/stock
 * Trả về tổng tồn kho (SUM quantity) từ bảng Medicine.
 * Dùng để verify trên dashboard rằng stock giảm sau khi mua hàng.
 */
@WebServlet("/api/stock")
public class StockApiServlet extends HttpServlet {

    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        long totalStock = invoiceDAO.getTotalStock();

        out.print("{\"totalStock\":" + totalStock + "}");
    }
}
