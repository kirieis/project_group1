
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Inventory {
    private Map<String, PriorityQueue<Batch>> stock = new HashMap<>();

    public void addBatch(Batch b) {
        stock
            .computeIfAbsent(
                b.getMedicine().getId(),
                k -> new PriorityQueue<>(Comparator.comparing(Batch::getExpiryDate))
            )
            .add(b);
    }

    // FIFO bán thuốc
    public int sell(String medicineId, int quantityVien) {
        PriorityQueue<Batch> pq = stock.get(medicineId);
        if (pq == null) return 0;

        int sold = 0;
        while (!pq.isEmpty() && sold < quantityVien) {
            Batch b = pq.peek();
            int canSell = Math.min(b.getQuantityVien(), quantityVien - sold);
            b.reduce(canSell);
            sold += canSell;

            if (b.getQuantityVien() == 0) {
                pq.poll();
            }
        }
        return sold;
    }

    // Dashboard: cảnh báo thuốc hết hạn
    public int purgeExpired() {
        int removed = 0;
        for (PriorityQueue<Batch> pq : stock.values()) {
            while (!pq.isEmpty() && pq.peek().isExpired()) {
                removed += pq.poll().getQuantityVien();
            }
        }
        return removed;
    }
}
