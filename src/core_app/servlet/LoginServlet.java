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

        if (customer != null) {
            // Login success
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", customer);

            // Redirect to home
            resp.sendRedirect("home.html");
        } else {
            // Login failed
            resp.sendRedirect("login.html?error=true");
        }
    }
}
