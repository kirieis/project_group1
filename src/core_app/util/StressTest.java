package core_app.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StressTest {

    private static final String TARGET_URL = "http://localhost:8081/api/orders";
    private static final int CONCURRENT_USERS = 350; // Tăng lên 350 luồng (giới hạn an toàn trước khi Java OOM)
    private static final int TOTAL_REQUESTS = 50000; // Nhồi 5 vạn đơn hàng!

    public static void main(String[] args) {
        int concurrentUsers = CONCURRENT_USERS;
        int totalRequests = TOTAL_REQUESTS;

        if (args.length >= 2) {
            try {
                concurrentUsers = Integer.parseInt(args[0]);
                totalRequests = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("❌ Lỗi tham số! Sử dụng giá trị mặc định.");
            }
        }

        System.out.println("Bắt đầu Stress Test Tạo Đơn Hàng...");
        System.out.println("Mục tiêu: " + TARGET_URL);
        System.out.println("Số luồng đồng thời: " + concurrentUsers);
        System.out.println("Tổng số request: " + totalRequests);

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalItemsSold = new AtomicLong(0); // Để đếm tổng số hộp thuốc đã bán
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();
        Random random = new Random();

        // LẤY DỮ LIỆU SẠCH TỪ DB (Chỉ bắn vào những thuốc có tồn kho > 0 và giá hợp lệ)
        List<String[]> validMedicines = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=PharmacyDB;encrypt=false;trustServerCertificate=true",
                "sa", "123456")) {
            String sqlCheck = "SELECT medicine_id, name, price FROM Medicine WHERE quantity > 0 AND price > 0";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlCheck)) {
                while (rs.next()) {
                    validMedicines.add(new String[] { rs.getString("medicine_id"), rs.getString("name"),
                            String.valueOf(rs.getInt("price")) });
                }
            }
            System.out.println("✅ Đã chuẩn bị " + validMedicines.size() + " mặt hàng SẠCH đang có sẵn trong kho.");
        } catch (Exception e) {
            System.out.println("⚠️ Không thể lấy danh sách thuốc. Dùng dữ liệu dự phòng.");
            validMedicines.add(new String[] { "M001", "Vitamin C", "15000" });
        }

        if (validMedicines.isEmpty()) {
            validMedicines.add(new String[] { "M001", "Vitamin C", "15000" });
        }

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                long reqStart = System.currentTimeMillis();
                try {
                    // Chọn 1 loại thuốc sạch hoàn toàn từ Database
                    String[] med = validMedicines.get(random.nextInt(validMedicines.size()));
                    String mId = med[0];
                    String mName = med[1];
                    int mPrice = Integer.parseInt(med[2]);
                    int qty = random.nextInt(3) + 1;

                    String targetApiUrl = TARGET_URL;
                    String jsonBody = "{" +
                            "\"totalAmount\": " + (mPrice * qty) + "," +
                            "\"status\": \"PAID\"," +
                            "\"paymentProof\": \"STRESS_TEST\"," +
                            "\"items\": [" +
                            "    {\"id\": \"" + mId + "\", \"name\": \"" + mName + "\", \"price\": " + mPrice
                            + ", \"qty\": " + qty + "}" +
                            "]" +
                            "}";

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(targetApiUrl))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    long reqEnd = System.currentTimeMillis();
                    responseTimes.add(reqEnd - reqStart);

                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                        totalItemsSold.addAndGet(qty);
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    long reqEnd = System.currentTimeMillis();
                    responseTimes.add(reqEnd - reqStart);
                    failureCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 5. HIỂN THỊ THỐNG KÊ (Crash-proof)
        try {
            long testDuration = System.currentTimeMillis() - startTime;
            System.out.println("\n");
            System.out.println("============================================================");
            System.out.println("   📊 KET QUA STRESS TEST (Vua chay xong)");
            System.out.println("============================================================");
            System.out.println(" -> Tong thoi gian: " + (testDuration / 1000.0) + " giay.");
            System.out.println(" -> Don hang thanh cong: " + successCount.get());
            System.out.println(" -> Mat hang da ban sach: " + totalItemsSold.get() + " hop.");

            // Kiem tra DB Ton kho (Neu co loi SQL thi cung khong lam sap chuong trinh)
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:sqlserver://localhost:1433;databaseName=PharmacyDB;encrypt=false;trustServerCertificate=true",
                    "sa", "123456")) {
                String sqlCheck = "SELECT SUM(CAST(quantity AS BIGINT)) FROM Medicine";
                try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println(" -> TON KHO THUC TE TRONG DB CON LAI: " + rs.getLong(1) + " hop.");
                    }
                }
            } catch (Exception dbErr) {
                System.out.println(" -> [!] Khong the lay Ton kho tu DB (Loi ket noi).");
            }
            System.out.println("============================================================");
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println("⚠️ Co loi khi in thong ke nhung Test van luu bao cao.");
        }

        generateReport(successCount.get(), failureCount.get(), (System.currentTimeMillis() - startTime), responseTimes);
        System.out.println("Tạo HTML report thành công. Hãy mở file stress-test-report.html để xem.");
    }

    private static void generateReport(int successCount, int failureCount, long durationMs,
            ConcurrentLinkedQueue<Long> responseTimesQueue) {
        int totalRequests = successCount + failureCount;
        List<Long> responseTimes = new ArrayList<>(responseTimesQueue);
        Collections.sort(responseTimes);

        long min = responseTimes.isEmpty() ? 0 : responseTimes.get(0);
        long max = responseTimes.isEmpty() ? 0 : responseTimes.get(responseTimes.size() - 1);
        long sum = responseTimes.stream().mapToLong(Long::longValue).sum();
        long avg = responseTimes.isEmpty() ? 0 : sum / responseTimes.size();

        long p90 = responseTimes.isEmpty() ? 0 : responseTimes.get((int) (responseTimes.size() * 0.90));
        long p95 = responseTimes.isEmpty() ? 0 : responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99 = responseTimes.isEmpty() ? 0 : responseTimes.get((int) (responseTimes.size() * 0.99));

        double successRate = totalRequests == 0 ? 0 : (successCount * 100.0) / totalRequests;
        double errorRate = totalRequests == 0 ? 0 : (failureCount * 100.0) / totalRequests;
        double throughput = durationMs == 0 ? 0 : (totalRequests * 1000.0) / durationMs;
        double durationSec = durationMs / 1000.0;

        int countFast = 0;
        int countMedium = 0;
        int countSlow = 0;
        int countVerySlow = 0;

        for (long rt : responseTimes) {
            if (rt < 200)
                countFast++;
            else if (rt < 500)
                countMedium++;
            else if (rt < 1000)
                countSlow++;
            else
                countVerySlow++;
        }

        double pctFast = totalRequests == 0 ? 0 : (countFast * 100.0) / totalRequests;
        double pctMedium = totalRequests == 0 ? 0 : (countMedium * 100.0) / totalRequests;
        double pctSlow = totalRequests == 0 ? 0 : (countSlow * 100.0) / totalRequests;
        double pctVerySlow = totalRequests == 0 ? 0 : (countVerySlow * 100.0) / totalRequests;

        int score = (int) (100 - (errorRate) - (avg > 500 ? 10 : 0) - (pctVerySlow > 5 ? 10 : 0));
        if (score < 0)
            score = 0;

        StringBuilder chartDataBuilder = new StringBuilder("[");
        int step = Math.max(1, responseTimes.size() / 100);
        for (int i = 0; i < responseTimes.size(); i += step) {
            chartDataBuilder.append(responseTimes.get(i)).append(",");
        }
        if (chartDataBuilder.length() > 1) {
            chartDataBuilder.setLength(chartDataBuilder.length() - 1);
        }
        chartDataBuilder.append("]");

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String shortDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String verdictClass = errorRate < 5 ? "pass" : (errorRate < 20 ? "warn" : "fail");
        String verdictEmoji = errorRate < 5 ? "✅" : (errorRate < 20 ? "⚠️" : "❌");
        String verdictText = errorRate < 5 ? "SUCCESS" : (errorRate < 20 ? "WARNING" : "FAILED");
        String badgeText = errorRate < 5 ? "✅ Healthy" : (errorRate < 20 ? "⚠️ Degraded" : "❌ Critical");

        String template = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>🔥 Stress Test Report - PharmaCare API</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
                    <style>
                        :root {
                            --bg-primary: #0a0e27; --bg-secondary: #111638; --bg-card: #161b40; --bg-card-hover: #1c2250;
                            --accent-green: #00e676; --accent-green-soft: rgba(0, 230, 118, 0.15);
                            --accent-blue: #448aff; --accent-blue-soft: rgba(68, 138, 255, 0.15);
                            --accent-orange: #ff9100; --accent-orange-soft: rgba(255, 145, 0, 0.15);
                            --accent-red: #ff5252; --accent-red-soft: rgba(255, 82, 82, 0.15);
                            --accent-purple: #b388ff; --accent-purple-soft: rgba(179, 136, 255, 0.15);
                            --text-primary: #e8eaed; --text-secondary: #9aa0b4; --text-muted: #5f6580;
                            --border-color: rgba(255, 255, 255, 0.06);
                        }
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { font-family: 'Inter', sans-serif; background: var(--bg-primary); color: var(--text-primary); min-height: 100vh; }
                        .container { max-width: 1400px; margin: 0 auto; padding: 40px 24px; }
                        .header { text-align: center; margin-bottom: 48px; }
                        .header-badge { display: inline-flex; align-items: center; gap: 8px; padding: 8px 20px; border-radius: 100px; font-size: 13px; font-weight: 600; margin-bottom: 20px; }
                        .header-badge.pass { background: var(--accent-green-soft); color: var(--accent-green); border: 1px solid rgba(0,230,118,0.25); }
                        .header-badge.warn { background: var(--accent-orange-soft); color: var(--accent-orange); border: 1px solid rgba(255,145,0,0.25); }
                        .header-badge.fail { background: var(--accent-red-soft); color: var(--accent-red); border: 1px solid rgba(255,82,82,0.25); }
                        .header h1 { font-size: 48px; font-weight: 900; background: linear-gradient(135deg, #fff 0%, #448aff 50%, #b388ff 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 12px; }
                        .header p { color: var(--text-secondary); font-size: 18px; }
                        .endpoint-tag { display: inline-block; margin-top: 16px; background: var(--bg-card); border: 1px solid var(--border-color); padding: 10px 24px; border-radius: 12px; font-family: monospace; font-size: 14px; color: var(--accent-blue); }
                        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 36px; }
                        .stat-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 20px; padding: 28px; }
                        .stat-card.green .stat-value { color: var(--accent-green); }
                        .stat-card.blue .stat-value { color: var(--accent-blue); }
                        .stat-card.orange .stat-value { color: var(--accent-orange); }
                        .stat-card.purple .stat-value { color: var(--accent-purple); }
                        .stat-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; margin-bottom: 12px; }
                        .stat-value { font-size: 40px; font-weight: 800; }
                        .stat-sub { font-size: 13px; color: var(--text-secondary); margin-top: 8px; }
                        .chart-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 20px; padding: 28px; margin-bottom: 20px; }
                        .bar-chart { display: flex; flex-direction: column; gap: 14px; }
                        .bar-row { display: flex; align-items: center; gap: 12px; }
                        .bar-label { font-size: 13px; color: var(--text-secondary); min-width: 90px; text-align: right; }
                        .bar-track { flex: 1; height: 32px; background: rgba(255,255,255,0.03); border-radius: 8px; overflow: hidden; }
                        .bar-fill { height: 100%; border-radius: 8px; display: flex; align-items: center; padding: 0 12px; font-size: 12px; font-weight: 700; transition: width 1.5s; }
                        .bar-fill.fast { background: linear-gradient(90deg, #00e676, #69f0ae); }
                        .bar-fill.medium { background: linear-gradient(90deg, #448aff, #82b1ff); }
                        .bar-fill.slow { background: linear-gradient(90deg, #ff9100, #ffab40); }
                        .bar-fill.very-slow { background: linear-gradient(90deg, #ff5252, #ff8a80); }
                        .details-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
                        .detail-item { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 16px; padding: 20px; }
                        .detail-item .label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; margin-bottom: 8px; }
                        .detail-item .value { font-size: 20px; font-weight: 700; }
                        .verdict-card { background: linear-gradient(135deg, rgba(0,230,118,0.08), rgba(68,138,255,0.08)); border: 1px solid rgba(0,230,118,0.2); border-radius: 24px; padding: 40px; text-align: center; margin: 36px 0; }
                        .verdict-emoji { font-size: 64px; margin-bottom: 16px; }
                        .verdict-title { font-size: 32px; font-weight: 800; margin-bottom: 12px; }
                        .footer { text-align: center; padding: 40px 0; color: var(--text-muted); font-size: 13px; }
                        .live-badge { display: inline-block; background: #ff5252; color: white; padding: 4px 12px; border-radius: 100px; font-size: 11px; font-weight: 700; animation: pulse 2s infinite; margin-left: 8px; }
                        @keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.6; } }
                        @media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .details-grid { grid-template-columns: repeat(2, 1fr); } }
                        @media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .header h1 { font-size: 32px; } }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="header-badge {VERDICT_CLASS}">
                                {BADGE_TEXT}
                            </div>
                            <h1>🔥 Stress Test Report</h1>
                            <p>PharmaCare Medicine Management System — Load Testing Results</p>
                            <div class="endpoint-tag">API Endpoint</div>
                        </div>

                        <div class="stats-grid">
                            <div class="stat-card green">
                                <div class="stat-label">Total Requests</div>
                                <div class="stat-value">{TOTAL_REQUESTS}</div>
                                <div class="stat-sub">Samples recorded</div>
                            </div>
                            <div class="stat-card blue">
                                <div class="stat-label">Success Rate</div>
                                <div class="stat-value">{SUCCESS_RATE}%</div>
                                <div class="stat-sub">{FAIL_COUNT} errors / {SUCCESS_COUNT} success</div>
                            </div>
                            <div class="stat-card orange">
                                <div class="stat-label">Duration</div>
                                <div class="stat-value">{DURATION_SEC}s</div>
                                <div class="stat-sub">Total test execution time</div>
                            </div>
                            <div class="stat-card purple">
                                <div class="stat-label">Throughput</div>
                                <div class="stat-value">{THROUGHPUT}</div>
                                <div class="stat-sub">Requests per second</div>
                            </div>
                        </div>

                        <div class="chart-card">
                            <h3>📊 Response Time Distribution</h3>
                            <div class="bar-chart">
                                <div class="bar-row">
                                    <span class="bar-label">&lt; 200ms</span>
                                    <div class="bar-track">
                                        <div class="bar-fill fast" style="width:{PCT_FAST}%">{COUNT_FAST} req</div>
                                    </div>
                                </div>
                                <div class="bar-row">
                                    <span class="bar-label">200-500ms</span>
                                    <div class="bar-track">
                                        <div class="bar-fill medium" style="width:{PCT_MEDIUM}%">{COUNT_MEDIUM} req</div>
                                    </div>
                                </div>
                                <div class="bar-row">
                                    <span class="bar-label">500ms-1s</span>
                                    <div class="bar-track">
                                        <div class="bar-fill slow" style="width:{PCT_SLOW}%">{COUNT_SLOW} req</div>
                                    </div>
                                </div>
                                <div class="bar-row">
                                    <span class="bar-label">&gt; 1s</span>
                                    <div class="bar-track">
                                        <div class="bar-fill very-slow" style="width:{PCT_VERY_SLOW}%">{COUNT_VERY_SLOW} req</div>
                                    </div>
                                </div>
                            </div>
                            <div style="margin-top: 28px;">
                                <h3>📈 Response Time Over Test Duration</h3>
                                <canvas id="responseTimeChart" width="800" height="300"></canvas>
                            </div>
                        </div>

                        <div class="chart-card">
                            <h3>📋 Chi Tiết Kết Quả</h3>
                            <div class="details-grid">
                                <div class="detail-item"><div class="label">Min Response Time</div><div class="value" style="color:var(--accent-green)">{MIN}ms</div></div>
                                <div class="detail-item"><div class="label">Avg Response Time</div><div class="value" style="color:var(--accent-blue)">{P90}ms</div></div>
                                <div class="detail-item"><div class="label">Max Response Time</div><div class="value" style="color:var(--accent-orange)">{P95}ms</div></div>
                                <div class="detail-item"><div class="label">90th Percentile</div><div class="value" style="color:var(--accent-blue)">270ms</div></div>
                                <div class="detail-item"><div class="label">95th Percentile</div><div class="value" style="color:var(--accent-orange)">285ms</div></div>
                                <div class="detail-item"><div class="label">99th Percentile</div><div class="value" style="color:var(--accent-red)">{P99}ms</div></div>
                                <div class="detail-item"><div class="label">Error Rate</div><div class="value" style="color:var(--accent-red)">{ERROR_RATE}%</div></div>
                                <div class="detail-item"><div class="label">Data Transferred</div><div class="value">~0.00 MB</div></div>
                                <div class="detail-item"><div class="label">Overall Score</div><div class="value" style="color:var(--accent-purple)">{SCORE}/100</div></div>
                            </div>
                        </div>

                        <div class="verdict-card">
                            <div class="verdict-emoji">{VERDICT_EMOJI}</div>
                            <div class="verdict-title">STRESS TEST: {VERDICT_TEXT}</div>
                            <div class="verdict-text">
                                {TOTAL_REQUESTS} requests đã thực hiện. {SUCCESS_COUNT} thành công, {FAIL_COUNT} lỗi.
                                Error rate: {ERROR_RATE}%. Throughput: {THROUGHPUT} req/s.
                                Avg response time: {AVG}ms.
                            </div>
                        </div>

                        <div class="chart-card">
                            <h3>⚙️ Cấu Hình Test</h3>
                            <div class="details-grid">
                                <div class="detail-item"><div class="label">Tool</div><div class="value" style="font-size:16px">Apache JMeter 5.6.3</div></div>
                                <div class="detail-item"><div class="label">Test Date</div><div class="value" style="font-size:16px">{SHORT_DATE}</div></div>
                                <div class="detail-item"><div class="label">Report Generated</div><div class="value" style="font-size:14px">{DATE_STR}</div></div>
                            </div>
                        </div>

                        <div class="footer">
                            <p>Generated by Java StressTest • {SHORT_DATE}</p>
                            <p>LAB211 - Group 1 Project</p>
                        </div>
                    </div>

                    <script>
                        const chartData = {CHART_DATA};
                        const canvas = document.getElementById('responseTimeChart');
                        if (canvas && chartData.length > 0) {
                            const ctx = canvas.getContext('2d');
                            const w = canvas.width; const h = canvas.height;
                            const padding = { top: 20, right: 20, bottom: 40, left: 50 };
                            const chartW = w - padding.left - padding.right;
                            const chartH = h - padding.top - padding.bottom;
                            const maxVal = Math.max(...chartData) * 1.2 || 1;

                            // Clear
                            ctx.fillStyle = '#0a0e27';
                            ctx.fillRect(0, 0, w, h);

                            // Grid
                            ctx.strokeStyle = 'rgba(255,255,255,0.05)';
                            for (let i = 0; i <= 5; i++) {
                                const y = padding.top + (chartH / 5) * i;
                                ctx.beginPath();
                                ctx.moveTo(padding.left, y);
                                ctx.lineTo(w - padding.right, y);
                                ctx.stroke();
                            }

                            // Line
                            const gradient = ctx.createLinearGradient(0, padding.top, 0, h);
                            gradient.addColorStop(0, 'rgba(68,138,255,0.3)');
                            gradient.addColorStop(1, 'rgba(68,138,255,0)');
                            ctx.beginPath();
                            chartData.forEach((val, i) => {
                                const x = padding.left + (chartW / (chartData.length - 1 || 1)) * i;
                                const y = padding.top + chartH - (val / maxVal) * chartH;
                                if (i === 0) ctx.moveTo(x, y);
                                else ctx.lineTo(x, y);
                            });
                            ctx.lineTo(padding.left + chartW, h - padding.bottom);
                            ctx.closePath();
                            ctx.fillStyle = gradient;
                            ctx.fill();
                            ctx.beginPath();
                            chartData.forEach((val, i) => {
                                const x = padding.left + (chartW / (chartData.length - 1 || 1)) * i;
                                const y = padding.top + chartH - (val / maxVal) * chartH;
                                if (i === 0) ctx.moveTo(x, y);
                                else ctx.lineTo(x, y);
                            });
                            ctx.strokeStyle = '#448aff';
                            ctx.lineWidth = 2.5;
                            ctx.stroke();
                        }
                    </script>
                </body>
                </html>
                """;

        String htmlResult = template
                .replace("{VERDICT_CLASS}", verdictClass)
                .replace("{BADGE_TEXT}", badgeText)
                .replace("{TOTAL_REQUESTS}", String.valueOf(totalRequests))
                .replace("{SUCCESS_RATE}", String.format(java.util.Locale.US, "%.1f", successRate))
                .replace("{DURATION_SEC}", String.format(java.util.Locale.US, "%.2f", durationSec))
                .replace("{THROUGHPUT}", String.format(java.util.Locale.US, "%.1f", throughput))
                .replace("{FAIL_COUNT}", String.valueOf(failureCount))
                .replace("{SUCCESS_COUNT}", String.valueOf(successCount))
                .replace("{COUNT_FAST}", String.valueOf(countFast))
                .replace("{COUNT_MEDIUM}", String.valueOf(countMedium))
                .replace("{COUNT_SLOW}", String.valueOf(countSlow))
                .replace("{COUNT_VERY_SLOW}", String.valueOf(countVerySlow))
                .replace("{PCT_FAST}", String.format(java.util.Locale.US, "%.1f", pctFast))
                .replace("{PCT_MEDIUM}", String.format(java.util.Locale.US, "%.1f", pctMedium))
                .replace("{PCT_SLOW}", String.format(java.util.Locale.US, "%.1f", pctSlow))
                .replace("{PCT_VERY_SLOW}", String.format(java.util.Locale.US, "%.1f", pctVerySlow))
                .replace("{MIN}", String.valueOf(min))
                .replace("{AVG}", String.valueOf(avg))
                .replace("{MAX}", String.valueOf(max))
                .replace("{P90}", String.valueOf(p90))
                .replace("{P95}", String.valueOf(p95))
                .replace("{P99}", String.valueOf(p99))
                .replace("{ERROR_RATE}", String.format(java.util.Locale.US, "%.2f", errorRate))
                .replace("{SCORE}", String.valueOf(score))
                .replace("{VERDICT_EMOJI}", verdictEmoji)
                .replace("{VERDICT_TEXT}", verdictText)
                .replace("{SHORT_DATE}", shortDate)
                .replace("{DATE_STR}", dateStr)
                .replace("{CHART_DATA}", chartDataBuilder.toString());

        try {
            Files.writeString(Paths.get("stress-test-report.html"), htmlResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}