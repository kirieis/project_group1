package core_app.servlet;

import core_app.dao.CustomerDAO;
import core_app.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/admin/users")
public class UserManagementServlet extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendRedirect("../login.html");
            return;
        }

        String action = req.getParameter("action");
        if ("list".equals(action)) {
            renderJsonList(resp);
            return;
        }

        req.getRequestDispatcher("/admin_users.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("add".equals(action)) {
            String password = req.getParameter("password");
            if (!isValidPassword(password)) {
                resp.getWriter().print("WeakPassword");
                return;
            }

            Customer c = new Customer();
            c.setFullName(req.getParameter("fullName"));
            c.setUsername(req.getParameter("username"));
            c.setPassword(password);
            c.setRole(req.getParameter("role"));

            boolean success = customerDAO.add(c);
            resp.getWriter().print(success ? "OK" : "Error");
        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            boolean success = customerDAO.delete(id);
            resp.getWriter().print(success ? "OK" : "Error");
        }
    }

    private boolean isValidPassword(String p) {
        if (p == null || p.length() < 8)
            return false;
        boolean hasUpper = false, hasLower = false, hasNum = false;
        for (char ch : p.toCharArray()) {
            if (Character.isUpperCase(ch))
                hasUpper = true;
            else if (Character.isLowerCase(ch))
                hasLower = true;
            else if (Character.isDigit(ch))
                hasNum = true;
        }
        return hasUpper && hasLower && hasNum;
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null)
            return false;
        Customer current = (Customer) session.getAttribute("currentUser");
        return current != null && "ADMIN".equals(current.getRole());
    }

    private void renderJsonList(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        List<Customer> list = customerDAO.getAll();
        PrintWriter out = resp.getWriter();
        out.print("[");
        for (int i = 0; i < list.size(); i++) {
            Customer c = list.get(i);
            out.print(String.format(
                    "{\"id\":%d, \"fullName\":\"%s\", \"username\":\"%s\", \"role\":\"%s\"}",
                    c.getCustomerId(), c.getFullName(), c.getUsername(), c.getRole()));
            if (i < list.size() - 1)
                out.print(",");
        }
        out.print("]");
    }
}
