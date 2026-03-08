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
        req.setCharacterEncoding("UTF-8");
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String email = req.getParameter("email");
        String address = req.getParameter("address");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (!isValidPassword(password)) {
            resp.sendRedirect("register.html?error=weak_password");
            return;
        }

        // Tạo đối tượng chờ xác nhận
        Customer c = new Customer();
        c.setFullName(fullName);
        c.setPhoneNumber(phone);
        c.setEmail(email);
        c.setAddress(address);
        c.setUsername(username);
        c.setPassword(password);
        c.setRole("CUSTOMER");

        // Gửi OTP (Bất đồng bộ)
        String otp = core_app.util.EmailUtil.generateOTP();

        // IN MÃ OTP RA CONSOLE NGAY LẬP TỨC (ĐỒNG BỘ) - KHÔNG PHỤ THUỘC EMAIL
        System.out.println("\n================================================");
        System.out.println("   DANG KY MOI - " + new java.util.Date());
        System.out.println("   Email: " + email);
        System.out.println("   >>>> MA OTP: " + otp + " <<<<");
        System.out.println("================================================\n");

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                boolean sent = core_app.util.EmailUtil.sendOTP(email, otp);
                if (sent) {
                    System.out.println("[OK] Email OTP da gui thanh cong toi: " + email);
                } else {
                    System.out.println("[WARN] Email KHONG gui duoc toi: " + email);
                }
            } catch (Exception ex) {
                System.err.println("[ERROR] Loi gui email: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        jakarta.servlet.http.HttpSession session = req.getSession();
        session.setAttribute("pendingUser", c);
        session.setAttribute("registrationOTP", otp);
        session.setAttribute("otpTime", System.currentTimeMillis());

        // Chuyển hướng sang trang nhập OTP ngay lập tức
        resp.sendRedirect("verify_otp.html");
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
