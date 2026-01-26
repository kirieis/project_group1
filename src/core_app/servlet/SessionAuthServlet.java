package core_app.servlet;

import core_app.model.Customer;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionAuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        HttpSession session = req.getSession(false);
        PrintWriter out = resp.getWriter();
        
        if (session != null && session.getAttribute("currentUser") != null) {
            Customer c = (Customer) session.getAttribute("currentUser");
            // Returns simple JSON: {"isLoggedIn": true, "username": "...", "role": "..."}
            out.print(String.format("{\"isLoggedIn\": true, \"username\": \"%s\", \"fullName\": \"%s\", \"role\": \"%s\"}", 
                    c.getUsername(), c.getFullName(), c.getRole()));
        } else {
            out.print("{\"isLoggedIn\": false}");
        }
        out.flush();
    }
}
