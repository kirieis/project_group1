package simulator;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Simulator {

    // Danh sách medicine IDs thực tế từ database (đã kiểm tra tồn tại)
    private static final String[] MEDICINE_IDS = {
            "MED4", "MED5", "MED6", "MED34", "MED36", "MED47", "MED57", "MED62", "MED75", "MED77",
            "MED93", "MED95", "MED4608", "MED33811", "MED7798", "MED26779", "MED8105", "MED33287",
            "MED33796", "MED24874", "MED4756", "MED17885", "MED14315", "MED27960", "MED32815",
            "MED17164", "MED13731", "MED29664", "MED34141", "MED15190"
    };

    // Tên thuốc tương ứng
    private static final String[] MEDICINE_NAMES = {
            "Aspirin 81mg", "Salbutamol 4mg", "Efferalgan 500mg", "Ambroxol 30mg", "Vitamin B1 B6 B12",
            "Celecoxib 200mg", "Gabapentin 300mg", "Calcium Carbonate 500mg", "Efferalgan 500mg", "Desloratadine 5mg",
            "Allopurinol 300mg", "Tramadol 50mg", "Siro Ginkid ZinC", "Panadol Extra", "Levofloxacin 500mg",
            "Captopril 25mg", "Vitamin K2", "Metformin 500mg", "Salbutamol 4mg", "Pantoprazole 40mg",
            "Fluconazole 150mg", "Levothyroxine 50mcg", "Insulin Basal", "Loperamid 2mg", "Diazepam 5mg",
            "Losartan 50mg", "Cefixime 200mg", "Eugica", "Hapacol 650", "Iron Folic"
    };

    // Giá bán tương ứng
    private static final int[] MEDICINE_PRICES = {
            1000, 1000, 1500, 1250, 1400, 1200, 1700, 1600, 1500, 1450,
            2000, 1250, 50000, 1000, 1500, 1500, 1400, 1200, 1000, 1250,
            2500, 1250, 2500, 3000, 1700, 1200, 2000, 50000, 700, 1200
    };

    public static void main(String[] args) throws Exception {
        String endpoint = "https://unplanked-inculpably-malorie.ngrok-free.dev/api/orders";
        Random rnd = new Random();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   BẮT ĐẦU GIẢ LẬP ĐƠN HÀNG - ORDER SIMULATOR");
        System.out.println("   50 đơn hàng | Status: PAID (auto-approved)");
        System.out.println("═══════════════════════════════════════════════\n");

        int successCount = 0;
        int failCount = 0;

        for (int i = 1; i <= 50; i++) {
            // Tạo 1-5 items ngẫu nhiên cho mỗi đơn hàng
            int numItems = rnd.nextInt(5) + 1;
            StringBuilder itemsJson = new StringBuilder();
            double totalAmount = 0;

            for (int j = 0; j < numItems; j++) {
                int medicineIndex = rnd.nextInt(MEDICINE_IDS.length);
                String medId = MEDICINE_IDS[medicineIndex];
                String medName = MEDICINE_NAMES[medicineIndex];
                int price = MEDICINE_PRICES[medicineIndex];
                int quantity = rnd.nextInt(10) + 1; // Số lượng 1-10

                totalAmount += price * quantity;

                if (j > 0)
                    itemsJson.append(",");
                itemsJson.append(String.format(
                        "{\"id\":\"%s\",\"name\":\"%s\",\"price\":%d,\"qty\":%d}",
                        medId, medName, price, quantity));
            }

            // Tạo JSON payload - status = PAID để tự động duyệt + trừ kho
            String payload = String.format("""
                    {
                      "status":"PAID",
                      "paymentProof":"",
                      "totalAmount":%.2f,
                      "items":[%s]
                    }
                    """, totalAmount, itemsJson.toString());

            // Gửi request
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("ngrok-skip-browser-warning", "true");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String status = (responseCode == 200) ? "✅ OK" : "❌ FAIL";
            if (responseCode == 200)
                successCount++;
            else
                failCount++;

            System.out.printf("đơn #%02d | Items: %d | Total: %,10.0f VND | %s (HTTP %d)\n",
                    i, numItems, totalAmount, status, responseCode);

            Thread.sleep(50); // 50ms delay - siêu nhanh
        }

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.printf("   DONE - %d thành công / %d thất bại\n", successCount, failCount);
        System.out.println("═══════════════════════════════════════════════");
    }
}
