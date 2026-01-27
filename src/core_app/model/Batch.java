package core_app.model;

import java.time.LocalDate;

public class Batch implements Comparable<Batch> {

    private String batchId;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    private int vienPerVi;
    private int viPerHop;
    

    private int totalVien;

    private Medicine medicine;

    public Batch() {
    }

    public Batch(String batchId,
                 String batchNumber,
                 LocalDate manufactureDate,
                 LocalDate expiryDate,
                 int vienPerVi,
                 int viPerHop,

                 int totalVien,
                 Medicine medicine) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.vienPerVi = vienPerVi;
        this.viPerHop = viPerHop;
        
        this.totalVien = totalVien;
        this.medicine = medicine;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isSellable() {
        return !isExpired() && totalVien > 0;
    }

    @Override
    public int compareTo(Batch other) {
        return this.expiryDate.compareTo(other.expiryDate);
    }

    public int convertToVien(int quantity, UnitType unitType) {
        switch (unitType) {
            case VIEN:
                return quantity;
            case VI:
                return quantity * vienPerVi;
            case HOP:
                return quantity * viPerHop * vienPerVi;
           
            default:
                throw new IllegalArgumentException("Invalid unit type");
        }
    }

    public void deductStock(int quantity, UnitType unitType) {
        int vien = convertToVien(quantity, unitType);
        if (vien > totalVien) {
            throw new IllegalArgumentException("Not enough stock in batch");
        }
        totalVien -= vien;
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

    public int getTotalVien() {
        return totalVien;
    }

    public Medicine getMedicine() {
        return medicine;
    }
}
