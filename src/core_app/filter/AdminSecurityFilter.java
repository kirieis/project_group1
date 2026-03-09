package core_app.filter;

import core_app.model.Customer;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Vệ sĩ bảo vệ khu vực Admin.
 * Bất kỳ request nào vào /admin/* đều phải qua đây kiểm tra.
 */
@WebFilter("/admin/*")
public class AdminSecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // 1. Kiểm tra session và role
        Customer user = (session != null) ? (Customer) session.getAttribute("currentUser") : null;
        boolean isAdmin = (user != null && "ADMIN".equalsIgnoreCase(user.getRole()));

        // Cho phép truy cập vào trang login admin hoặc xử lý login admin để tránh loop
        String path = httpRequest.getRequestURI();
        if (path.contains("/admin/login") || path.contains("/admin/auth")) {
            chain.doFilter(request, response);
            return;
        }

        if (isAdmin) {
            // Là Admin xịn -> Cho qua
            chain.doFilter(request, response);
        } else {
            // Không phải Admin -> Đá về trang cổng Admin bí mật để đăng nhập
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin_gate.html");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
