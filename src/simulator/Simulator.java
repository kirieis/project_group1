package simulator;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Simulator {

    public static void main(String[] args) throws Exception {
        String endpoint = "http://localhost:8080/core_app/servlet";
        Random rnd = new Random();

        for (int i = 1; i <= 50; i++) {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = """
                {
                  "medicineId":"M%d",
                  "quantity":%d
                }
            """.formatted(rnd.nextInt(100), rnd.nextInt(5) + 1);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Sent order #" + i + " → HTTP " + conn.getResponseCode());
            Thread.sleep(200);
        }
    }
}
