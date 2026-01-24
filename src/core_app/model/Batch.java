package core_app.model;

import java.time.LocalDate;

public class Batch {

    private int batchId;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    private int quantity; 

    private Medicine medicine; 

    public Batch() {
    }

    public Batch(int batchId, String batchNumber,
                 LocalDate manufactureDate, LocalDate expiryDate,
                 int quantity, Medicine medicine) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.medicine = medicine;
    }

    // ===== Business Method =====

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public void deductQuantity(int amount) {
        if (amount > quantity) {
            throw new IllegalArgumentException("Not enough stock in batch");
        }
        this.quantity -= amount;
    }

    // ===== Getter & Setter =====

    public int getBatchId() {
        return batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public Medicine getMedicine() {
        return medicine;
    }
}
