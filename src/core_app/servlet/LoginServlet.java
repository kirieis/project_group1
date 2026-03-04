package core_app.servlet;

import core_app.dao.CustomerDAO;
import core_app.model.Customer;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Show login page
        req.getRequestDispatcher("/login.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        Customer customer = customerDAO.authenticate(u, p);

        // Check if it's an AJAX request (looking for X-Requested-With or similar, but
        // let's check Accept header)
        String acceptHeader = req.getHeader("Accept");
        boolean isAjax = acceptHeader != null && acceptHeader.contains("application/json");

        if (customer != null) {
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", customer);

            if (isAjax) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                // Return simple JSON with user info for caching
                String json = String.format(
                        "{\"status\":\"success\", \"isLoggedIn\":true, \"fullName\":\"%s\", \"role\":\"%s\", \"username\":\"%s\"}",
                        customer.getFullName(), customer.getRole(), customer.getUsername());
                resp.getWriter().print(json);
            } else {
                if ("ADMIN".equals(customer.getRole())) {
                    resp.sendRedirect(req.getContextPath() + "/admin");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/home.html");
                }
            }
        } else {
            if (isAjax) {
                resp.setStatus(401);
                resp.setContentType("application/json");
                resp.getWriter().print("{\"status\":\"error\", \"message\":\"Invalid credentials\"}");
            } else {
                resp.sendRedirect("login.html?error=true");
            }
        }
    }
}
