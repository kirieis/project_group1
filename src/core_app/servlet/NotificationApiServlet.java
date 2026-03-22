package core_app.servlet;

import core_app.util.LatestNotification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/notification")
public class NotificationApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String currentMsg = LatestNotification.getMessage();
        long timestamp = LatestNotification.getTimestamp();
        long now = System.currentTimeMillis();
        long fiveMinutesMs = 5 * 60 * 1000;

        // Only show notification if it was set within the last 5 minutes
        if (currentMsg != null && !currentMsg.trim().isEmpty() && (now - timestamp) <= fiveMinutesMs) {
            String escapedMsg = currentMsg.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            long remainingMs = fiveMinutesMs - (now - timestamp);
            out.print("{\"hasNotification\": true, \"message\": \"" + escapedMsg + "\", \"remainingMs\": " + remainingMs + "}");
        } else {
            out.print("{\"hasNotification\": false}");
        }
    }
}
