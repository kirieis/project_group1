
import java.time.LocalDate;

public class Batch {
    private String batchId;
    private Medicine medicine;
    private LocalDate expiryDate;
    private int quantityVien;

    public Batch(String batchId, Medicine medicine, LocalDate expiryDate, int quantityVien) {
        this.batchId = batchId;
        this.medicine = medicine;
        this.expiryDate = expiryDate;
        this.quantityVien = quantityVien;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public String getBatchId() {
        return batchId;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public int getQuantityVien() {
        return quantityVien;
    }

    public void reduce(int amount) {
        this.quantityVien -= amount;
    }
}
