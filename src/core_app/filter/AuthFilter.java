package core_app.filter;

import core_app.model.Customer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        Customer user = (session != null) ? (Customer) session.getAttribute("currentUser") : null;

        // If trying to access /admin/ but not an ADMIN
        if (user == null || !"ADMIN".equals(user.getRole())) {
            // Check if it's an AJAX request (like those from admin_users.html)
            String requestedWith = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print("Access Denied");
            } else {
                // Redirect regular navigation to login
                resp.sendRedirect(req.getContextPath() + "/login.html?error=unauthorized");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
