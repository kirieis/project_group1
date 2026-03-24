<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Xác thực OTP - Aura Pharmacy</title>
        <link rel="stylesheet" href="home.css">
        <style>
            .otp-wrapper {
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 80vh;
            }

            .otp-card {
                background: white;
                padding: 40px;
                border-radius: 20px;
                box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
                width: 100%;
                max-width: 400px;
                text-align: center;
            }

            .otp-input {
                width: 100%;
                padding: 15px;
                font-size: 24px;
                text-align: center;
                letter-spacing: 5px;
                border: 2px solid #e2e8f0;
                border-radius: 12px;
                margin: 20px 0;
            }

            .otp-input:focus {
                border-color: #6366f1;
                outline: none;
            }
        </style>
    </head>

    <body>
        <div class="otp-wrapper">
            <div class="otp-card">
                <h1 style="color: #6366f1; margin-bottom: 10px;">Xác thực Email</h1>
                <p style="color: #64748b;">Mã OTP đã được gửi tới email của bạn. Vui lòng kiểm tra và nhập mã bên dưới.
                </p>

                <% if ("invalid".equals(request.getParameter("error"))) { %>
                    <div
                        style="background: #fee2e2; color: #ef4444; padding: 10px; border-radius: 8px; margin-top: 15px;">
                        Mã OTP không chính xác!
                    </div>
                    <% } %>

                        <form action="verify-otp" method="POST">
                            <input type="text" name="otp" class="otp-input" maxlength="6" required placeholder="000000"
                                pattern="\d{6}">
                            <button type="submit" class="btn btn--primary"
                                style="width: 100%; justify-content: center;">
                                Xác nhận đăng ký
                            </button>
                        </form>

                        <div style="margin-top: 20px;">
                            <a href="register.html" style="color: #6366f1; text-decoration: none; font-size: 14px;">
                                Quay lại trang đăng ký</a>
                        </div>
            </div>
        </div>
    </body>

    </html>