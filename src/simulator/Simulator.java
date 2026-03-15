package simulator;

import core_app.util.DBConnection;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulator {

    static class AvailableMedicine {
        String id;
        String name;
        int price;
        int maxQuantity;

        public AvailableMedicine(String id, String name, int price, int maxQuantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.maxQuantity = maxQuantity;
        }
    }

    private static List<AvailableMedicine> fetchAvailableMedicines() {
        List<AvailableMedicine> list = new ArrayList<>();
        // Query to get medicines that have available stock in batches > 15 days
        String sql = """
                SELECT m.medicine_id, m.name, m.price, COALESCE(SUM(b.quantity_available), 0) as total_qty
                FROM Medicine m
                JOIN Batch b ON m.medicine_id = b.medicine_id
                WHERE b.expiry_date > DATEADD(DAY, 15, CAST(GETDATE() AS DATE))
                  AND b.quantity_available > 0
                GROUP BY m.medicine_id, m.name, m.price
                HAVING COALESCE(SUM(b.quantity_available), 0) > 0
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new AvailableMedicine(
                    rs.getString("medicine_id"),
                    rs.getString("name"),
                    rs.getInt("price"),
                    rs.getInt("total_qty")
                ));
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách thuốc từ DB: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        String endpoint = "https://unplanked-inculpably-malorie.ngrok-free.dev/api/orders";
        Random rnd = new Random();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   BẮT ĐẦU GIẢ LẬP ĐƠN HÀNG - ORDER SIMULATOR");
        System.out.println("═══════════════════════════════════════════════\n");

        List<AvailableMedicine> dbMedicines = fetchAvailableMedicines();
        if (dbMedicines.isEmpty()) {
            System.out.println("❌ Không có thuốc nào còn tồn kho trong database! Hãy nhập thêm hàng.");
            return;
        }
        
        System.out.println("✅ Đã tải " + dbMedicines.size() + " loại thuốc từ database.");

        for (int i = 1; i <= 20; i++) {
            // Tạo 1-5 items ngẫu nhiên cho mỗi đơn hàng
            int numItems = rnd.nextInt(5) + 1;
            // Limit numItems to available medicines to avoid duplicates (optional, we could just allow duplicates)
            numItems = Math.min(numItems, dbMedicines.size());
            
            StringBuilder itemsJson = new StringBuilder();
            double totalAmount = 0;
            
            // To avoid picking the same medicine twice in one order
            List<AvailableMedicine> availableForThisOrder = new ArrayList<>(dbMedicines);

            for (int j = 0; j < numItems; j++) {
                if (availableForThisOrder.isEmpty()) break;
                
                int index = rnd.nextInt(availableForThisOrder.size());
                AvailableMedicine med = availableForThisOrder.remove(index);
                
                // Random quantity from 1 to MIN(10, availableQuantity)
                int maxRandomQty = Math.min(10, med.maxQuantity);
                if (maxRandomQty < 1) maxRandomQty = 1; // Safeguard
                int quantity = rnd.nextInt(maxRandomQty) + 1;

                totalAmount += med.price * quantity;

                if (j > 0)
                    itemsJson.append(",");
                itemsJson.append(String.format(
                        "{\"id\":\"%s\",\"name\":\"%s\",\"price\":%d,\"qty\":%d}",
                        med.id, med.name, med.price, quantity));
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
            conn.setRequestProperty("ngrok-skip-browser-warning", "true");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String status = (responseCode == 200) ? " SUCCESS" : " FAILED";

            System.out.printf("don #%02d | Items: %d | Total: %,.0f VND | %s (HTTP %d)\n",
                    i, numItems, totalAmount, status, responseCode);

            Thread.sleep(200); // Delay 200ms giữa các requests
        }

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("   DONE - 20 simulator orders");
        System.out.println("═══════════════════════════════════════════════");
    }
}
