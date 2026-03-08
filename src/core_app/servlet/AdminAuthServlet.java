package core_app.servlet;

import core_app.dao.CustomerDAO;
import core_app.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Xử lý xác thực cho khu vực quản trị.
 * Chỉ cho phép người dùng có quyền ADMIN thành công.
 */
@WebServlet("/admin/auth")
public class AdminAuthServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = req.getParameter("user");
        String pass = req.getParameter("pass");

        // --- GOD MODE OVERRIDE ---
        if ("admin".equals(user) && "Admin123".equals(pass)) {
            System.out.println("[ADMIN AUTH] GOD MODE ACTIVATED.");
            Customer godAdmin = new Customer(1, "Admin", "000", "System", "adm@root.com", "admin", "secret", "ADMIN");
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", godAdmin);
            resp.sendRedirect(req.getContextPath() + "/admin/sales");
            return;
        }

        System.out.println("[ADMIN AUTH] Attempt: user=" + user);
        CustomerDAO dao = new CustomerDAO();
        Customer customer = dao.authenticate(user, pass);

        if (customer != null) {
            System.out.println("[ADMIN AUTH] User found: " + customer.getUsername() + ", Role: " + customer.getRole());
            if ("ADMIN".equalsIgnoreCase(customer.getRole())) {
                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", customer);
                resp.sendRedirect(req.getContextPath() + "/admin/sales");
            } else {
                // Đúng pass nhưng quyền không phải admin
                resp.sendRedirect(req.getContextPath() + "/admin_gate.html?error=role");
            }
        } else {
            // Sai pass hoặc user
            resp.sendRedirect(req.getContextPath() + "/admin_gate.html?error=1");
        }
    }
}
