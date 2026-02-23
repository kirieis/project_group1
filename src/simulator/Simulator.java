package simulator;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Simulator {

    // Danh sách medicine IDs thực tế từ database
    private static final String[] MEDICINE_IDS = {
            "MED6", "MED7", "MED9", "MED11", "MED12", "MED25", "MED26", "MED27", "MED28", "MED30",
            "MED31", "MED32", "MED34", "MED36", "MED42", "MED47", "MED49", "MED50", "MED57", "MED60",
            "MED62", "MED75", "MED76", "MED77", "MED81", "MED88", "MED92", "MED93", "MED95", "MED97"
    };

    // Tên thuốc tương ứng
    private static final String[] MEDICINE_NAMES = {
            "Telfast HD 180mg", "Esomeprazole 40mg", "Cefixime 200mg", "Acetylcysteine 200mg",
            "Smecta", "Zinc Gluconate 20mg", "Levofloxacin 500mg", "Omeprazole 20mg",
            "Vitamin K2", "Panadol Extra", "Cefdinir 300mg", "Enat 400", "Enterogermina",
            "Vitamin B1 B6 B12", "Magnesium B6", "Ketoconazole 200mg", "Levothyroxine 50mcg",
            "Calcium D3", "Levofloxacin 500mg", "Allopurinol 300mg", "Esomeprazole 40mg",
            "Hapacol 650", "Vitamin E 400IU", "Clotrimazole 1%", "Loperamid 2mg",
            "Eugica", "Colchicine 1mg", "Loperamid 2mg", "Enat 400", "Enat 400"
    };

    // Giá bán tương ứng
    private static final int[] MEDICINE_PRICES = {
            1450, 1000, 2000, 50000, 1500, 1000, 1500, 2500, 1400, 2500,
            2000, 2000, 1250, 1400, 3000, 1450, 1450, 50000, 1500, 2000,
            50000, 700, 1400, 50000, 1500, 50000, 50000, 3000, 1250, 1700
    };

    public static void main(String[] args) throws Exception {
        String endpoint = "http://localhost:8080/api/orders";
        Random rnd = new Random();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   BẮT ĐẦU GIẢ LẬP ĐƠN HÀNG - ORDER SIMULATOR");
        System.out.println("═══════════════════════════════════════════════\n");

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

            // Tạo JSON payload theo format của OrderServlet
            String payload = String.format("""
                    {
                      "status":"PENDING",
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
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String status = (responseCode == 200) ? "✅ SUCCESS" : "❌ FAILED";

            System.out.printf("Đơn #%02d | Items: %d | Total: %,.0f VND | %s (HTTP %d)\n",
                    i, numItems, totalAmount, status, responseCode);

            Thread.sleep(200); // Delay 200ms giữa các requests
        }

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("   DONE - 50 simulator orders");
        System.out.println("═══════════════════════════════════════════════");
    }
}
