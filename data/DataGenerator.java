import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Random;

public class DataGenerator {

    public static void main(String[] args) throws Exception {
        FileWriter fw = new FileWriter("medicines_raw_10000.csv");
        fw.write("medicine_id,name,batch,expiry,quantity\n");

        Random r = new Random();
        for (int i = 1; i <= 10000; i++) {
            boolean error = r.nextInt(100) < 8; // ~8% lỗi
            String id = error ? "" : "MED" + i;
            String name = error ? "###" : "Paracetamol";
            String batch = "B" + (i % 50);
            String expiry = error ? "invalid-date"
                    : LocalDate.now().plusDays(r.nextInt(500) - 200).toString();
            int qty = error ? -5 : r.nextInt(200) + 1;

            fw.write(id + "," + name + "," + batch + "," + expiry + "," + qty + "\n");
        }
        fw.close();
    }
}
