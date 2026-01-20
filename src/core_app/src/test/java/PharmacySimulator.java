
import java.time.LocalDate;

public class PharmacySimulator {

    public static void main(String[] args) throws Exception {

        // 1. Generate legacy data
        LegacyCSVGenerator.generate("legacy_batches.csv", 10000);
        System.out.println("Generated legacy data (dirty)");

        // 2. Sample medicines
        Medicine para = new Medicine("M1", "Paracetamol", 10, 10, 10);
        Medicine amox = new Medicine("M2", "Amoxicillin", 10, 5, 8);

        // 3. Inventory (Server trung tâm)
        Inventory inventory = new Inventory();

        inventory.addBatch(new Batch("B001", para, LocalDate.now().plusDays(20), 3000));
        inventory.addBatch(new Batch("B002", para, LocalDate.now().plusDays(5), 2000)); // FIFO
        inventory.addBatch(new Batch("B003", amox, LocalDate.now().minusDays(2), 1500)); // expired

        // 4. Dashboard cảnh báo
        int expired = inventory.purgeExpired();
        System.out.println("Expired removed: " + expired);

        // 5. POS bán hàng
        int sold = inventory.sell("M1", 2500);
        System.out.println("Sold Paracetamol: " + sold);
    }
}
