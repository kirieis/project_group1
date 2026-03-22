package core_app.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailUtil {

    // THAY ĐỔI THÔNG TIN CỦA BẠN TẠI ĐÂY
    private static final String SENDER_EMAIL = "annguyenbinh325@gmail.com"; // Email của sếp
    private static final String APP_PASSWORD = "przp dbor nerp rrkv"; // Mật khẩu ứng dụng mới của Github Pharmacy

    public static String generateOTP() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public static boolean sendOTP(String recipientEmail, String otp) {
        // Tự động xóa dấu cách nếu có
        String cleanPassword = APP_PASSWORD.replaceAll("\\s+", "");

        if (SENDER_EMAIL.contains("your-email"))
            return false;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, cleanPassword);
            }
        });

        // IN RA CONSOLE ĐỂ BẠN LẤY LUÔN NẾU MAIL CHẬM
        System.out.println("\n================================================");
        System.out.println("   [DEBUG] DANG GUI OTP TOI: " + recipientEmail);
        System.out.println("   >>>> MÃ OTP CỦA BẠN LÀ: " + otp + " <<<<");
        System.out.println("================================================\n");

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Github Pharmacy"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Mã xác thực OTP - Github Pharmacy");
            message.setText("Chào bạn,\n\nMã OTP để đăng ký tài khoản của bạn là: " + otp +
                    "\n\nMã này có hiệu lực trong 5 phút. Vui lòng không cung cấp mã này cho bất kỳ ai.");

            Transport.send(message);
            System.out.println("[SUCCESS] Da gui email thanh cong.");
            return true;
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("[ERROR] Loi khi gui email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendEmail(String recipientEmail, String subject, String body) {
        String cleanPassword = APP_PASSWORD.replaceAll("\\s+", "");

        if (SENDER_EMAIL.contains("your-email"))
            return false;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, cleanPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Github Pharmacy"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("[SUCCESS] Da gui email thanh cong (Subject: " + subject + ").");
            return true;
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("[ERROR] Loi khi gui email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
