package core_app.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core_app.model.Customer;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/auth-status")
public class SessionAuthServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        PrintWriter out = resp.getWriter();

        if (session != null && session.getAttribute("currentUser") != null) {
            Customer c = (Customer) session.getAttribute("currentUser");

            JsonObject json = new JsonObject();
            json.addProperty("isLoggedIn", true);
            json.addProperty("username", c.getUsername() != null ? c.getUsername() : "");
            json.addProperty("fullName", c.getFullName() != null ? c.getFullName() : "");
            json.addProperty("role", c.getRole() != null ? c.getRole() : "CUSTOMER");
            json.addProperty("phone", c.getPhoneNumber() != null ? c.getPhoneNumber() : "");
            json.addProperty("address", c.getAddress() != null ? c.getAddress() : "");

            out.print(gson.toJson(json));
        } else {
            out.print("{\"isLoggedIn\": false}");
        }
        out.flush();
    }
}
