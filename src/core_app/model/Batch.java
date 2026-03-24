package core_app.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Batch implements Comparable<Batch> {
    private String batchId;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private int tabletsPerBlister;
    private int blisterPerBox;
    private int bottle;
    private int totalTablets;
    private Medicine medicine;
    // ===== Ngưỡng quản lý (sync với BatchDAO) =====
    private static int DAYS_REMOVE = 15;
    private static int DAYS_SALE = 30;
    private static int DISCOUNT_PERCENT = 30;

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
    // Trạng thái hạn sử dụng

    /** Đã hết hạn (≤ 0 ngày) */
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now()) || expiryDate.isEqual(LocalDate.now());
    }

    /** Sắp hết hạn, vùng sale 30% (16–30 ngày) */
    public boolean isNearExpiry() {
        if (isExpired())
            return false;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        /**
         * ChronoUnit.DAYS.between(...):tính số ngày còn lại từ hôm nay đến lúc hết hạn.
         */
        return days > DAYS_REMOVE && days <= DAYS_SALE;
    }

    /** Bị loại khỏi bán (≤ 15 ngày, chưa hết hạn) */
    public boolean isUnsellable() {
        if (isExpired())
            return true;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return days <= DAYS_REMOVE;
    }

    /** Còn bán được (> 15 ngày AND còn tồn kho) */
    public boolean isSellable() {
        return !isExpired() && !isUnsellable() && totalTablets > 0;
    }

    /** % giảm giá: 30% nếu near-expiry, 0% nếu bình thường */
    public int getDiscountPercent() {
        return isNearExpiry() ? DISCOUNT_PERCENT : 0;
    }

    /** Số ngày còn lại */
    public long getDaysRemaining() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Trạng thái text: OK / NEAR_EXPIRY / REMOVED / EXPIRED
     */
    public String getExpiryStatus() {
        if (isExpired())
            return "EXPIRED";
        long days = getDaysRemaining();
        if (days <= DAYS_REMOVE)
            return "REMOVED";
        if (days <= DAYS_SALE)
            return "NEAR_EXPIRY";
        return "OK";
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
            case BOTTLE:
                return quantity * bottle;
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

    // ===== Getter =====

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
