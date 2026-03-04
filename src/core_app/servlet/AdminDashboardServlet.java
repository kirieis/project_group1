package core_app.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Entry point for the Admin Backoffice.
 * Redirects or forwards to the main dashboard (Sales).
 */
@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // We just forward to the sales dashboard as the default admin landing page
        resp.sendRedirect(req.getContextPath() + "/admin/sales");
    }
}
