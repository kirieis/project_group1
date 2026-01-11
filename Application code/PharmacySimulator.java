// ===============================
// Pharmacy Chain Management Simulator
// Java 8+
// Includes: Domain model + Legacy CSV data generator (>10,000 rows)
// ===============================

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

// -------------------------------
// ENTITY: Medicine
// -------------------------------
class Medicine {
    String id;
    String name;
    String unit; // Vien
    int vienPerVi;
    int viPerHop;
    int hopPerThung;

    public Medicine(String id, String name, int vienPerVi, int viPerHop, int hopPerThung) {
        this.id = id;
        this.name = name;
        this.unit = "VIEN";
        this.vienPerVi = vienPerVi;
        this.viPerHop = viPerHop;
        this.hopPerThung = hopPerThung;
    }

    public int thungToVien(int thung) {
        return thung * hopPerThung * viPerHop * vienPerVi;
    }
}

// -------------------------------
// ENTITY: Batch (FIFO)
// -------------------------------
class Batch {
    String batchId;
    Medicine medicine;
    LocalDate expiryDate;
    int quantityVien;

    public Batch(String batchId, Medicine medicine, LocalDate expiryDate, int quantityVien) {
        this.batchId = batchId;
        this.medicine = medicine;
        this.expiryDate = expiryDate;
        this.quantityVien = quantityVien;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }
}

// -------------------------------
// INVENTORY (FIFO by expiry)
// -------------------------------
class Inventory {
    Map<String, PriorityQueue<Batch>> stock = new HashMap<>();

    public void addBatch(Batch b) {
        stock.computeIfAbsent(b.medicine.id, k -> new PriorityQueue<>(Comparator.comparing(batch -> batch.expiryDate)));
        stock.get(b.medicine.id).add(b);
    }

    public int sell(String medicineId, int quantityVien) {
        int sold = 0;
        PriorityQueue<Batch> pq = stock.get(medicineId);
        if (pq == null) return 0;

        while (!pq.isEmpty() && sold < quantityVien) {
            Batch b = pq.peek();
            if (b.quantityVien <= quantityVien - sold) {
                sold += b.quantityVien;
                pq.poll();
            } else {
                b.quantityVien -= (quantityVien - sold);
                sold = quantityVien;
            }
        }
        return sold;
    }

    public int purgeExpired() {
        int removed = 0;
        for (PriorityQueue<Batch> pq : stock.values()) {
            while (!pq.isEmpty() && pq.peek().isExpired()) {
                removed += pq.poll().quantityVien;
            }
        }
        return removed;
    }
}

// -------------------------------
// LEGACY CSV GENERATOR (>10,000 rows)
// -------------------------------
class LegacyCSVGenerator {
    static Random rnd = new Random();
    static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void generate(String file, int rows) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("batch_id,medicine_id,medicine_name,expiry_date,quantity_vien,branch_id\n");
            for (int i = 1; i <= rows; i++) {
                String batchId = "B" + i;
                String medId = "M" + (rnd.nextInt(50) + 1);
                String medName = "Thuoc_" + medId;
                LocalDate expiry = LocalDate.now().plusDays(rnd.nextInt(900) - 300);
                int qty = (rnd.nextInt(20) + 1) * 100;
                String branch = "CN" + (rnd.nextInt(10) + 1);

                bw.write(batchId + "," + medId + "," + medName + "," + expiry.format(df) + "," + qty + "," + branch + "\n");
            }
        }
    }
}

// -------------------------------
// SIMULATOR
// -------------------------------
public class PharmacySimulator {
    public static void main(String[] args) throws Exception {
        // 1. Generate legacy CSV (>10k rows)
        LegacyCSVGenerator.generate("legacy_batches.csv", 12000);
        System.out.println("Legacy CSV generated: legacy_batches.csv");

        // 2. Sample medicines
        Medicine para = new Medicine("M1", "Paracetamol", 10, 10, 10);
        Medicine amox = new Medicine("M2", "Amoxicillin", 10, 5, 8);

        // 3. Inventory
        Inventory inv = new Inventory();
        inv.addBatch(new Batch("B001", para, LocalDate.now().plusDays(100), 5000));
        inv.addBatch(new Batch("B002", para, LocalDate.now().plusDays(10), 2000)); // FIFO
        inv.addBatch(new Batch("B003", amox, LocalDate.now().minusDays(5), 3000)); // expired

        // 4. Purge expired
        int removed = inv.purgeExpired();
        System.out.println("Expired removed (vien): " + removed);

        // 5. Simulate sale
        int sold = inv.sell("M1", 2500);
        System.out.println("Sold Paracetamol (vien): " + sold);
    }
}
