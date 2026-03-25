@echo off
setlocal enabledelayedexpansion

:: Cấu hình màu sắc
color 0B
title 🔥 PHARMACARE - PRO STRESS TEST CONTROLLER 🔥

echo ============================================================
echo   🚀 CONFIG YOUR STRESS TEST SCENARIO
echo ============================================================
echo.

:: 1. KIỂM TRA SERVER 8081
echo [1/4] Kiem tra server (Port 8081)...
netstat -ano | findstr :8081 >nul
if %errorlevel% neq 0 (
    color 0C
    echo ERROR: Khong thay port 8081 hoat dong!
    echo HAY CHAY 'mvn cargo:run' TRUOC KHI CHAY BAT NAY.
    pause
    exit /b
)
echo ✅ Server 8081 dang online.

:: 2. CHO PHÉP NHẬP THÔNG SỐ (Custom Input)
echo.
echo [2/4] Thiet lap thong so ban pha:
set /p USERS="-> NHAP SO LUONG NGUOI DUNG DONG THOI (Mac dinh 350): "
if "!USERS!"=="" set USERS=350

set /p REQS="-> NHAP TONG SO DON HANG MUON BAN (Mac dinh 50000): "
if "!REQS!"=="" set REQS=50000

echo.
echo ------------------------------------------------------------
echo Target: http://localhost:8081/api/orders (status=PAID)
echo Users : !USERS!
echo Total : !REQS!
echo ------------------------------------------------------------
echo.

:: 3. BIÊN DỊCH
echo [3/4] Bien dich StressTest.java...
javac src\core_app\util\StressTest.java
if %errorlevel% neq 0 (
    color 0C
    echo ERROR: Compile failed!
    pause
    exit /b
)
echo ✅ Bien dich thanh cong.

:: 4. CHẠY TEST (DÙNG WINDOWS START ĐỂ CHẠY RIÊNG)
echo [4/4] DANG QUET SACH KHO HANG...
echo [!] Khi cua so moi chay xong, No se DOI ban an phim chu khong tu tat.
start /wait cmd /c "java -Xshare:off -cp src core_app.util.StressTest !USERS! !REQS! & echo. & echo ============================================================ & echo ^G DONE! NHAN PHIM BAT KY DE XEM THONG KE ROI TAT MAN HINH NAY. & echo ============================================================ & pause"

:: TỰ ĐỘNG BẬT BÁO CÁO SAU KHI CỬA SỔ TRÊN ĐÓNG
echo.
echo [!] Dang bat Report HTML...
start stress-test-report.html

echo.
echo ============================================================
echo   🎯 DA XU LY XONG. 
echo ============================================================
pause
exit
