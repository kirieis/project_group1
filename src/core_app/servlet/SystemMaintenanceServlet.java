package core_app.servlet;

import core_app.model.Customer;
import core_app.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Administrative tools for database maintenance.
 * ONLY for cleaning test data.
 */
@WebServlet("/admin/maintenance")
public class SystemMaintenanceServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("reset_sales".equals(action)) {
            resetSales(resp);
        } else if ("reset_users".equals(action)) {
            resetUsers(resp);
        } else {
            resp.getWriter().print("UnknownAction");
        }
    }

    private void resetSales(HttpServletResponse resp) throws IOException {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Truncate details first
            stmt.execute("DELETE FROM Invoice_Detail");
            // Then invoices
            stmt.execute("DELETE FROM Invoice");

            // Re-seed IDs
            stmt.execute("DBCC CHECKIDENT ('Invoice', RESEED, 0)");

            System.out.println("✅ [Maintenance] Sales data reset by Admin");
            resp.getWriter().print("CLEAN_OK");
        } catch (Exception e) {
            resp.getWriter().print("ERROR: " + e.getMessage());
        }
    }

    private void resetUsers(HttpServletResponse resp) throws IOException {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Delete all customers EXCLUDING the main admin 'admin'
            // and any other administrators to avoid locking yourself out
            stmt.execute("DELETE FROM Customer WHERE role != 'ADMIN' AND username != 'admin'");

            System.out.println("✅ [Maintenance] Non-admin users cleared by Admin");
            resp.getWriter().print("CLEAN_OK");
        } catch (Exception e) {
            resp.getWriter().print("ERROR: " + e.getMessage());
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
