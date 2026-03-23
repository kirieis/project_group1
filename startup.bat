@echo off
setlocal
title PHARMACY SYSTEM - MAVEN & NGROK

:: ====================================================================
::   PHARMACY MANAGEMENT SYSTEM - MAVEN & NGROK STARTUP
:: ====================================================================

echo.
echo ====================================================================
echo   🚀 PHARMACY SYSTEM - MAVEN + NGROK
echo ====================================================================
echo.

echo [1/3] CHUAN BI: Dang Clean va Package project (Tao file .war)...
:: Su dung mvn package thay vi compile de tao file .war vao thu muc target
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ❌ LOI: Build project that bai. Vui long kiem tra code!
    pause
    exit /b %errorlevel%
)
echo ✅ Build thanh cong.

echo.
echo [2/3] KHOI CHAY WEB SERVER (Tomcat 10 / Port 8081)...
echo Vui long doi khoang 15-30 giay cho den khi thay dong "Tomcat started".
start "WEB SERVER - Tomcat 10" cmd /c "mvn cargo:run"

echo.
echo [3/3] KHOI CHAY NGROK (Connecting to Port 8081)...
if exist "ngrok.exe" (
    start "NGROK - Tunnel 8081" cmd /c "ngrok.exe http 8081"
) else (
    start "NGROK - Tunnel 8081" cmd /c "ngrok http 8081"
)

echo.
echo ====================================================================
echo   ✨ MAVEN & NGROK DANG DUOC KHOI CHAY! ✨
echo ====================================================================
echo.
echo   Luu y: Neu Ngrok bao loi "Target machine actively refused it", 
echo   do la vi Web Server dang khoi dong. Hay F5 trinh duyet sau 15 giay!
echo.
echo   - Local Portal: http://localhost:8081/home.html
echo   - Ngrok Tunnel: Xem URL public tai cua so Ngrok.
echo.
echo   Nhan phim bat ky de ket thuc trinh quan ly.
echo ====================================================================
echo.

pause
