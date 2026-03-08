package core_app.servlet;

import core_app.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin/sales")
public class AdminSalesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/admin_gate.html");
            return;
        }
        req.getRequestDispatcher("/admin_sales.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("approve_all_sepay".equals(action)) {
            core_app.dao.InvoiceDAO dao = new core_app.dao.InvoiceDAO();
            int count = dao.approveAllSepayOrders();
            resp.getWriter().print("OK:" + count);
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null)
            return false;
        Customer current = (Customer) session.getAttribute("currentUser");
        return current != null && "ADMIN".equals(current.getRole());
    }
}
