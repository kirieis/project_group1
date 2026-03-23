package data_generator_cleaner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

public class DataCleaner {

    // ================================
    // DANH SÁCH TÊN THUỐC HỢP LỆ
    // ================================
    // Dùng Set để kiểm tra nhanh (O(1))
    static final Set<String> VALID_NAMES = Set.of(
            "Paracetamol_500mg", "Aspirin_500mg", "Amoxicillin_500mg", "Ibuprofen_200mg",
            "Cefixime_200mg", "Vitamin_C", "Meloxicam_7.5mg", "Cefdinir_300mg",
            "SkillMax_Ocavill", "Siro_Ginkid_ZinC", "Echina_Kingphar", "Panadol_Extra",
            "Efferalgan_500mg", "Hapacol_650", "Alphachoay", "Augmentin_625mg",
            "Cefuroxim_500mg", "Acyclovir_400mg", "Nexium_mups_20mg", "Loperamid_2mg",
            "Enterogermina", "Tiffy_Dey", "Telfast_HD_180mg", "Eugica",
            "Enat_400", "Ginkgo_Biloba_120mg", "Clarithromycin_500mg", "Azithromycin_250mg",
            "Ciprofloxacin_500mg", "Levofloxacin_500mg", "Metronidazole_500mg",
            "Omeprazole_20mg", "Pantoprazole_40mg", "Esomeprazole_40mg",
            "Domperidone_10mg", "Metoclopramide_10mg", "Cetirizine_10mg",
            "Loratadine_10mg", "Desloratadine_5mg", "Fexofenadine_120mg",
            "Salbutamol_4mg", "Ambroxol_30mg", "Bromhexine_8mg", "Acetylcysteine_200mg",
            "Calcium_Carbonate_500mg", "Calcium_D3", "Magnesium_B6",
            "Vitamin_B1_B6_B12", "Vitamin_E_400IU", "Iron_Folic",
            "ORS_Plus", "Smecta", "Krebs_Cough_Syrup", "Prospan_Syrup",
            "Zinc_Gluconate_20mg", "Hydrocortisone_10mg", "Betadine_Solution",
            "Clotrimazole_1pct", "Ketoconazole_200mg", "Fluconazole_150mg",
            "Nystatin_Suspension", "Diclofenac_50mg", "Celecoxib_200mg",
            "Tramadol_50mg", "Prednisolone_5mg", "Methylprednisolone_16mg",
            "Losartan_50mg", "Amlodipine_5mg", "Bisoprolol_5mg",
            "Atorvastatin_20mg", "Simvastatin_20mg", "Metformin_500mg",
            "Gliclazide_80mg", "Insulin_Rapid", "Insulin_Basal",
            "Levothyroxine_50mcg", "Allopurinol_300mg", "Colchicine_1mg",
            "Vitamin_K2", "Ranitidine_150mg", "Famotidine_20mg",
            "Sucralfate_1g", "Bismuth_Subsalicylate", "Montelukast_10mg",
            "Theophylline_200mg", "Spironolactone_25mg", "Furosemide_40mg",
            "Hydrochlorothiazide_25mg", "Clopidogrel_75mg", "Aspirin_81mg",
            "Warfarin_5mg", "Enalapril_10mg", "Captopril_25mg",
            "Valsartan_80mg", "Glimepiride_2mg", "Sitagliptin_100mg",
            "Gabapentin_300mg", "Carbamazepine_200mg", "Diazepam_5mg"
    );

    public static void main(String[] args) throws Exception {

        // ================================
        // 1. KHAI BÁO FILE INPUT / OUTPUT
        // ================================
        String inputFile = "medicines_raw.csv";
        String cleanFile = "medicines_clean.csv";

        // ================================
        // 2. MỞ FILE ĐỂ ĐỌC / GHI
        // ================================
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        FileWriter fwClean = new FileWriter(cleanFile);

        // ================================
        // 3. ĐỌC HEADER (DÒNG ĐẦU)
        // ================================
        // Giữ nguyên header cho file clean
        String header = br.readLine();
        fwClean.write(header + "\n");

        // ================================
        // 4. SET DÙNG ĐỂ CHECK ID TRÙNG
        // ================================
        Set<String> seenIds = new HashSet<>();

        String line;
        int cleanRows = 0;

        // ================================
        // 5. ĐỌC TỪNG DÒNG TRONG FILE
        // ================================
        while ((line = br.readLine()) != null) {

            // validate dòng dữ liệu
            if (validate(line, seenIds)) {
                // nếu hợp lệ → ghi vào file clean
                fwClean.write(line + "\n");
                cleanRows++;
            }
            // nếu không hợp lệ → bỏ qua (không ghi gì)
        }

        // ================================
        // 6. ĐÓNG FILE
        // ================================
        br.close();
        fwClean.close();

        // ================================
        // 7. THÔNG BÁO KẾT QUẢ
        // ================================
        System.out.println("Done! Clean rows: " + cleanRows);
    }

    // =========================================================
    // HÀM VALIDATE
    // → trả về true nếu dòng hợp lệ
    // → false nếu có lỗi
    // =========================================================
    static boolean validate(String line, Set<String> seenIds) {

        // ================================
        // 1. TÁCH CÁC CỘT TRONG CSV
        // ================================
        // -1 giúp giữ lại cả cột rỗng
        String[] p = line.split(",", -1);

        // ================================
        // 2. KIỂM TRA SỐ LƯỢNG CỘT
        // ================================
        if (p.length != 11) return false;

        // ================================
        // 3. LẤY CÁC TRƯỜNG CẦN KIỂM TRA
        // ================================
        String id = p[0].trim();
        String name = p[1].trim();
        String batch = p[2].trim();
        String expiryStr = p[8].trim();
        String qtyStr = p[9].trim();
        String priceStr = p[10].trim();

        // ================================
        // 4. KIỂM TRA ID
        // - không được rỗng
        // - không được trùng
        // ================================
        if (id.isEmpty() || !seenIds.add(id)) return false;

        // ================================
        // 5. KIỂM TRA TÊN THUỐC
        // phải nằm trong danh sách hợp lệ
        // ================================
        if (!VALID_NAMES.contains(name)) return false;

        // ================================
        // 6. KIỂM TRA BATCH
        // không được rỗng hoặc "???"
        // ================================
        if (batch.isEmpty() || batch.equals("???")) return false;

        // ================================
        // 7. KIỂM TRA NGÀY HẾT HẠN
        // - phải đúng format
        // - không được hết hạn
        // ================================
        try {
            LocalDate expiry = LocalDate.parse(expiryStr);
            if (expiry.isBefore(LocalDate.now())) return false;
        } catch (DateTimeParseException e) {
            return false;
        }

        // ================================
        // 8. KIỂM TRA SỐ LƯỢNG
        // - phải là số
        // - phải > 0
        // ================================
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) return false;
        } catch (NumberFormatException e) {
            return false;
        }

        // ================================
        // 9. KIỂM TRA GIÁ
        // - phải là số
        // - phải > 0
        // ================================
        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) return false;
        } catch (NumberFormatException e) {
            return false;
        }

        // ================================
        // 10. NẾU PASS HẾT → HỢP LỆ
        // ================================
        return true;
    }
}