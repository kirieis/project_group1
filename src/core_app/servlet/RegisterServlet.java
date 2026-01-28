package core_app.servlet;

import core_app.dao.CustomerDAO;
import core_app.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Customer c = new Customer();
        c.setFullName(fullName);
        c.setPhoneNumber(phone);
        c.setAddress(address);
        c.setUsername(username);
        c.setPassword(password);
        c.setRole("CUSTOMER"); // Force CUSTOMER role for public registration

        if (!isValidPassword(password)) {
            resp.sendRedirect("register.html?error=weak_password");
            return;
        }

        if (customerDAO.add(c)) {
            resp.sendRedirect("login.html?registered=true");
        } else {
            resp.sendRedirect("register.html?error=exists");
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
}
