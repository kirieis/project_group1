package core_app.model;

import java.time.LocalDate;

public class Batch implements Comparable<Batch> {

    private String batchId;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    private int tabletsPerBlister;
    private int blisterPerBox;

    private int totalTablets;

    private Medicine medicine;

    public Batch() {
    }

    public Batch(String batchId,
                 String batchNumber,
                 LocalDate manufactureDate,
                 LocalDate expiryDate,
                 int tabletsPerBlister,
                 int blisterPerBox,

                 int totalTablets,
                 Medicine medicine) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.tabletsPerBlister = tabletsPerBlister;
        this.blisterPerBox = blisterPerBox;

        this.totalTablets = totalTablets;
        this.medicine = medicine;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isSellable() {
        return !isExpired() && totalTablets > 0;
    }

    @Override
    public int compareTo(Batch other) {
        return this.expiryDate.compareTo(other.expiryDate);
    }

    public int convertToTablets(int quantity, UnitType unitType) {
        switch (unitType) {
            case TABLETS:
                return quantity;
            case BLISTER:
                return quantity * tabletsPerBlister;
            case BOX:
                return quantity * blisterPerBox * tabletsPerBlister;

            default:
                throw new IllegalArgumentException("Invalid unit type");
        }
    }

    public void deductStock(int quantity, UnitType unitType) {
        int tablets = convertToTablets(quantity, unitType);
        if (tablets > totalTablets) {
            throw new IllegalArgumentException("Not enough stock in batch");
        }
        totalTablets -= tablets;
    }

    public String getBatchId() {
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

    public int getTotalTablets() {
        return totalTablets;
    }

    public Medicine getMedicine() {
        return medicine;
    }
}
