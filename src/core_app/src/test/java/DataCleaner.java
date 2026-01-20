
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataCleaner {

    public static Batch cleanAndConvert(
            String batchId,
            String medId,
            String medName,
            String expiryStr,
            int qty,
            Medicine med
    ) {
        if (batchId == null || batchId.isBlank()) return null;
        if (medId == null || medId.isBlank()) return null;
        if (qty <= 0) return null;

        try {
            LocalDate expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return new Batch(batchId, med, expiry, qty);
        } catch (Exception e) {
            return null;
        }
    }
}