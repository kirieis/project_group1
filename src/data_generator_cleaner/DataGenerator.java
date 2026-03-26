import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Random;

public class DataGenerator {
    private static final int TOTAL_RECORDS = 1000; // 1000 loại thuốc SẠCH - CHUẨN

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Đang khởi động 'Nhà máy Sản xuất Thuốc'... (1000 loại)");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("medicines_raw.csv"))) {
            bw.write(
                    "medicine_id,name,batch,ingredient,dosage_form,strength,unit,manufacturer,expiry,quantity,price\n");

            String[] baseNames = {
                    "Paracetamol", "Aspirin", "Amoxicillin", "Ibuprofen", "Cefuroxim",
                    "Vitamin_C", "Panadol", "Efferalgan", "Hapacol", "Augmentin",
                    "Nexium", "Loperamid", "Tiffy", "Telfast", "Eugica", "Enat",
                    "Ginkgo_Biloba", "Clarithromycin", "Azithromycin", "Ciprofloxacin",
                    "Levofloxacin", "Omeprazole", "Pantoprazole", "Esomeprazole"
            };

            // Hàm lượng chuẩn y tế (mg)
            int[] dosages = { 10, 20, 30, 50, 75, 100, 250, 500, 1000 };
            String[] units = { "Tablet", "Capsule", "Sachet", "Bottle" };
            String[] mfrs = { "Sanofi", "Bayer", "GSK", "Pfizer", "DHG Pharma", "Stella", "AstraZeneca" };
            Random r = new Random();

            for (int i = 1; i <= TOTAL_RECORDS; i++) {
                String id = "MED" + i;

                // Thuật toán "Diverse Names": Ghép Tên + Hàm lượng (mg) CHUẨN
                String base = baseNames[r.nextInt(baseNames.length)];
                int mg = dosages[r.nextInt(dosages.length)];
                String name = base + "_" + mg + "mg";

                String batch = (char) ('A' + r.nextInt(3)) + String.valueOf(100 + r.nextInt(900));
                String dosageForm = units[r.nextInt(units.length)];
                String manufacturer = mfrs[r.nextInt(mfrs.length)];
                String strength = String.valueOf(mg);
                String expiry = LocalDate.now().plusMonths(r.nextInt(24) + 6).toString();

                // Tồn kho chuẩn: 10.000 hộp mỗi loại (Cho kho phình to 10 Triệu đơn giản!)
                int qty = 10000;
                int price = (r.nextInt(40) + 1) * 5000; // Giá nhảy đẹp 5k -> 200k

                bw.write(String.format("%s,%s,%s,%s,%s,%s,mg,%s,%s,%d,%d\n",
                        id, name, batch, base, dosageForm, strength, manufacturer, expiry, qty, price));
            }
        }

        System.out.println("✅ HOÀN THÀNH! Đã sinh 1000 loại thuốc SẠCH vào file medicines_raw.csv");
        System.out.println("👉 Tổng tồn kho đang là: 10.000.000 hộp.");
    }
}
