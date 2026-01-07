import java.time.LocalDate;
import java.util.*;

/* ===================== MEDICINE ===================== */
class Medicine {
    String name;

    static final int BOX_PER_CARTON = 10;
    static final int BLISTER_PER_BOX = 10;
    static final int PILL_PER_BLISTER = 10;

    public Medicine(String name) {
        this.name = name;
    }

    public static int toPills(int cartons, int boxes, int blisters, int pills) {
        return cartons * BOX_PER_CARTON * BLISTER_PER_BOX * PILL_PER_BLISTER
                + boxes * BLISTER_PER_BOX * PILL_PER_BLISTER
                + blisters * PILL_PER_BLISTER
                + pills;
    }
}

/* ===================== BATCH ===================== */
class Batch {
    Medicine medicine;
    int quantityPills;
    LocalDate expiryDate;

    public Batch(Medicine medicine, int quantityPills, LocalDate expiryDate) {
        this.medicine = medicine;
        this.quantityPills = quantityPills;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }
}

/* ===================== INVENTORY ===================== */
class Inventory {
    Map<String, Queue<Batch>> stock = new HashMap<>();

    public void addBatch(Batch batch) {
        stock.putIfAbsent(batch.medicine.name, new LinkedList<>());
        stock.get(batch.medicine.name).offer(batch);
    }

    public int sell(String medicineName, int pills) {
        Queue<Batch> batches = stock.get(medicineName);
        if (batches == null) return 0;

        int sold = 0;
        while (!batches.isEmpty() && pills > 0) {
            Batch b = batches.peek();
            int take = Math.min(b.quantityPills, pills);
            b.quantityPills -= take;
            pills -= take;
            sold += take;

            if (b.quantityPills == 0) {
                batches.poll();
            }
        }
        return sold;
    }

    public void removeExpired() {
        for (Queue<Batch> batches : stock.values()) {
            batches.removeIf(Batch::isExpired);
        }
    }
}

/* ===================== INVOICE ===================== */
class Invoice {
    static int counter = 1;
    int id;
    String branchName;
    String medicineName;
    int pills;
    LocalDate date;

    public Invoice(String branchName, String medicineName, int pills) {
        this.id = counter++;
        this.branchName = branchName;
        this.medicineName = medicineName;
        this.pills = pills;
        this.date = LocalDate.now();
    }

    public void print() {
        System.out.println("Invoice #" + id +
                " | Branch: " + branchName +
                " | Medicine: " + medicineName +
                " | Pills: " + pills +
                " | Date: " + date);
    }
}

/* ===================== BRANCH ===================== */
class Branch {
    String name;
    Inventory inventory = new Inventory();
    CentralServer server;

    public Branch(String name, CentralServer server) {
        this.name = name;
        this.server = server;
    }

    public void sellMedicine(String medicineName, int pills) {
        int sold = inventory.sell(medicineName, pills);
        if (sold > 0) {
            Invoice invoice = new Invoice(name, medicineName, sold);
            server.receiveInvoice(invoice);
        } else {
            System.out.println("[" + name + "] Không đủ thuốc!");
        }
    }
}

/* ===================== CENTRAL SERVER ===================== */
class CentralServer {
    List<Invoice> invoices = new ArrayList<>();

    public synchronized void receiveInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoice.print();
    }

    public void report() {
        System.out.println("\n===== CENTRAL REPORT =====");
        System.out.println("Total invoices: " + invoices.size());
    }
}

/* ===================== SIMULATOR ===================== */
public class prototype {

    public static void main(String[] args) {
        CentralServer server = new CentralServer();

        Branch hanoi = new Branch("Hà Nội", server);
        Branch saigon = new Branch("Sài Gòn", server);

        Medicine paracetamol = new Medicine("Paracetamol");

        int pills1 = Medicine.toPills(1, 0, 0, 0);
        int pills2 = Medicine.toPills(0, 5, 0, 0);

        hanoi.inventory.addBatch(
                new Batch(paracetamol, pills1, LocalDate.now().plusDays(30))
        );
        hanoi.inventory.addBatch(
                new Batch(paracetamol, pills2, LocalDate.now().minusDays(1))
        );

        System.out.println("\n🔍 Quét kho & hủy thuốc hết hạn...");
        hanoi.inventory.removeExpired();

        System.out.println("\n🛒 Khách mua lẻ (FIFO)...");
        hanoi.sellMedicine("Paracetamol", 120);
        saigon.sellMedicine("Paracetamol", 50);

        server.report();
    }
}
