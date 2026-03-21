package core_app.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTest {

    private static final String TARGET_URL = "https://unplanked-inculpably-malorie.ngrok-free.dev/";

    private static final int CONCURRENT_USERS = 200;

    private static final int TOTAL_REQUESTS = 10000;

    public static void main(String[] args) {
        System.out.println("Bắt đầu Stress Test Tạo Đơn Hàng...");
        System.out.println(
                "Mục tiêu: " + (TARGET_URL.endsWith("/") ? TARGET_URL + "api/orders" : TARGET_URL + "/api/orders"));
        System.out.println("Số luồng đồng thời: " + CONCURRENT_USERS);
        System.out.println("Tổng số request: " + TOTAL_REQUESTS);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    String targetApiUrl = TARGET_URL.endsWith("/") ? TARGET_URL + "api/orders"
                            : TARGET_URL + "/api/orders";
                    String jsonBody = "{" +
                            "\"totalAmount\": 50000," +
                            "\"status\": \"PENDING\"," +
                            "\"paymentProof\": \"\"," +
                            "\"items\": [" +
                            "    {\"id\": \"M001\", \"name\": \"Vitamin C\", \"price\": 50000, \"qty\": 1}" +
                            "]" +
                            "}";

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(targetApiUrl))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        // Đợi tất cả luồng hoàn thành
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        System.out.println("=========================================");
        System.out.println("KẾT QUẢ STRESS TEST");
        System.out.println("Thời gian thực thi: " + durationMs + " ms");
        System.out.println("Số request thành công (Mã 200): " + successCount.get());
        System.out.println("Số request thất bại: " + failureCount.get());
        System.out.println("Tốc độ trung bình: " + ((TOTAL_REQUESTS * 1000L) / durationMs) + " requests/giây");
        System.out.println("=========================================");
    }
}
