

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class LegacyCSVGenerator {
    private static Random rnd = new Random();
    private static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void generate(String file, int rows) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("batch_id,medicine_id,medicine_name,expiry_date,quantity_vien,branch_id\n");

            for (int i = 1; i <= rows; i++) {
                String batchId = (i % 50 == 0) ? "" : "B" + (i % 500); // trùng + null
                String medId = (i % 30 == 0) ? "" : "M" + (rnd.nextInt(20) + 1);
                String medName = "Thuoc_" + medId;

                String expiry = (i % 40 == 0)
                        ? "INVALID_DATE"
                        : LocalDate.now().plusDays(rnd.nextInt(800) - 400).format(df);

                int qty = (i % 25 == 0) ? -100 : (rnd.nextInt(20) + 1) * 100;
                String branch = "CN" + (rnd.nextInt(10) + 1);

                bw.write(batchId + "," + medId + "," + medName + "," + expiry + "," + qty + "," + branch + "\n");
            }
        }
    }
}
