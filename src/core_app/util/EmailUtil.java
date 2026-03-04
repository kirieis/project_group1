package core_app.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailUtil {

    // THAY ĐỔI THÔNG TIN CỦA BẠN TẠI ĐÂY
    private static final String SENDER_EMAIL = "annguyenbinh325@gmail.com"; // Email của sếp
    private static final String APP_PASSWORD = "mdrw kliu kqtl fmmr"; // Mật khẩu ứng dụng (App Password)

    public static String generateOTP() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public static boolean sendOTP(String recipientEmail, String otp) {
        if (SENDER_EMAIL.contains("your-email"))
            return false; // Chưa cấu hình

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Github Pharmacy"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Mã xác thực OTP - Github Pharmacy");
            message.setText("Chào bạn,\n\nMã OTP để đăng ký tài khoản của bạn là: " + otp +
                    "\n\nMã này có hiệu lực trong 5 phút. Vui lòng không cung cấp mã này cho bất kỳ ai.");

            Transport.send(message);
            return true;
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
