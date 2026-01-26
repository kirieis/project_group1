package core_app.servlet;

import core_app.model.Customer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    // Pseudo-database for demonstration
    private static final Map<String, Customer> USERS_DB = new HashMap<>();

    static {
        // Pre-populate with some users
        USERS_DB.put("admin", new Customer(1, "Quản Trị Viên", "0909000111", "HCMC", "admin", "admin", "ADMIN"));
        USERS_DB.put("user", new Customer(2, "Nguyễn Văn A", "0912345678", "Hanoi", "user", "123", "CUSTOMER"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Show login page
        // Context root is src/core_app/web_app, so login.html is at root "/"
        req.getRequestDispatcher("/login.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        Customer customer = authenticate(u, p);

        if (customer != null) {
            // Login success
            HttpSession session = req.getSession();
            session.setAttribute("currentUser", customer);
            
            // Redirect to home
            resp.sendRedirect("home.html");
        } else {
            // Login failed
            req.setAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu!");
            req.getRequestDispatcher("/login.html").forward(req, resp);
        }
    }

    private Customer authenticate(String username, String password) {
        Customer c = USERS_DB.get(username);
        if (c != null && c.getPassword().equals(password)) {
            return c;
        }
        return null;
    }
}
