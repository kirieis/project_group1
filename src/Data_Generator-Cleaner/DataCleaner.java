import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DataCleaner {

    // ---- Counters ----
    static int totalRows = 0;
    static int cleanRows = 0;
    static int rejectedRows = 0;

    // Error-type counters
    static int errColumnCount = 0;
    static int errEmptyId = 0;
    static int errBadName = 0;
    static int errBadBatch = 0;
    static int errBadExpiry = 0;
    static int errExpired = 0;
    static int errBadQty = 0;
    static int errBadPrice = 0;
    static int errDuplicateId = 0;

    // ---- Valid medicine names (must match DataGenerator) ----
    static final java.util.Set<String> VALID_NAMES = java.util.Set.of(
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
            "Gabapentin_300mg", "Carbamazepine_200mg", "Diazepam_5mg");

    public static void main(String[] args) throws Exception {

        String inputFile = "medicines_raw.csv";
        String cleanFile = "medicines_clean.csv";
        String rejectedFile = "medicines_rejected.csv";
        String reportFile = "cleaning_report.txt";

        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        FileWriter fwClean = new FileWriter(cleanFile);
        FileWriter fwReject = new FileWriter(rejectedFile);
        PrintWriter report = new PrintWriter(new FileWriter(reportFile));

        // ------ Read & write header ------
        String header = br.readLine();
        fwClean.write(header + "\n");
        fwReject.write(header + ",reject_reason\n");

        // ------ Duplicate-ID tracking ------
        java.util.Set<String> seenIds = new java.util.HashSet<>();

        // ------ Process each row ------
        String line;
        while ((line = br.readLine()) != null) {
            totalRows++;
            String reason = validate(line, seenIds);

            if (reason == null) {
                // Row is clean
                fwClean.write(line + "\n");
                cleanRows++;
            } else {
                // Row is rejected – append reason
                fwReject.write(line + "," + reason + "\n");
                rejectedRows++;
            }
        }

        br.close();
        fwClean.close();
        fwReject.close();

        // ------ Print report ------
        printReport(report);
        report.close();

        // Also print to console
        printReport(new PrintWriter(System.out, true));

        System.out.println("\n>> Done! Files created:");
        System.out.println("   - " + cleanFile + "  (" + cleanRows + " records)");
        System.out.println("   - " + rejectedFile + "  (" + rejectedRows + " records)");
        System.out.println("   - " + reportFile);
    }

    // ================================================================
    // VALIDATE – returns null if clean, or a rejection reason String
    // ================================================================
    static String validate(String line, java.util.Set<String> seenIds) {

        String[] p = line.split(",", -1); // keep trailing empties

        // ---------- 1. Column count ----------
        if (p.length != 11) {
            errColumnCount++;
            return "COLUMN_COUNT_MISMATCH(" + p.length + ")";
        }

        String id = p[0].trim();
        String name = p[1].trim();
        String batch = p[2].trim();
        // p[3] = ingredient, p[4] = dosage_form, p[5] = strength, p[6] = unit, p[7] =
        // manufacturer
        String expiryStr = p[8].trim();
        String qtyStr = p[9].trim();
        String priceStr = p[10].trim();

        // ---------- 2. Empty ID ----------
        if (id.isEmpty()) {
            errEmptyId++;
            return "EMPTY_ID";
        }

        // ---------- 3. Duplicate ID ----------
        if (!seenIds.add(id)) {
            errDuplicateId++;
            return "DUPLICATE_ID(" + id + ")";
        }

        // ---------- 4. Bad name (###) ----------
        if (name.equals("###") || name.isEmpty() || !VALID_NAMES.contains(name)) {
            errBadName++;
            return "INVALID_NAME(" + name + ")";
        }

        // ---------- 5. Bad batch (???) ----------
        if (batch.equals("???") || batch.isEmpty()) {
            errBadBatch++;
            return "INVALID_BATCH(" + batch + ")";
        }

        // ---------- 6. Expiry date ----------
        try {
            LocalDate expiry = LocalDate.parse(expiryStr);
            if (expiry.isBefore(LocalDate.now())) {
                errExpired++;
                return "EXPIRED(" + expiryStr + ")";
            }
        } catch (DateTimeParseException e) {
            errBadExpiry++;
            return "INVALID_EXPIRY(" + expiryStr + ")";
        }

        // ---------- 7. Quantity ----------
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                errBadQty++;
                return "INVALID_QTY(" + qty + ")";
            }
        } catch (NumberFormatException e) {
            errBadQty++;
            return "INVALID_QTY(" + qtyStr + ")";
        }

        // ---------- 8. Price ----------
        try {
            int price = Integer.parseInt(priceStr);
            if (price <= 0) {
                errBadPrice++;
                return "INVALID_PRICE(" + price + ")";
            }
        } catch (NumberFormatException e) {
            errBadPrice++;
            return "INVALID_PRICE(" + priceStr + ")";
        }

        return null; // all checks passed
    }

    // ================================================================
    // REPORT
    // ================================================================
    static void printReport(PrintWriter pw) {
        pw.println("==============================================");
        pw.println("         DATA CLEANING REPORT");
        pw.println("==============================================");
        pw.println();
        pw.println("Total rows processed  : " + totalRows);
        pw.println("Clean rows            : " + cleanRows);
        pw.println("Rejected rows         : " + rejectedRows);
        pw.println();
        pw.printf("Clean rate            : %.2f%%%n",
                totalRows > 0 ? (cleanRows * 100.0 / totalRows) : 0);
        pw.printf("Rejection rate        : %.2f%%%n",
                totalRows > 0 ? (rejectedRows * 100.0 / totalRows) : 0);
        pw.println();
        pw.println("----------------------------------------------");
        pw.println("  REJECTION BREAKDOWN");
        pw.println("----------------------------------------------");
        pw.println("  Column count error  : " + errColumnCount);
        pw.println("  Empty ID            : " + errEmptyId);
        pw.println("  Duplicate ID        : " + errDuplicateId);
        pw.println("  Invalid name (###)  : " + errBadName);
        pw.println("  Invalid batch (???) : " + errBadBatch);
        pw.println("  Invalid expiry date : " + errBadExpiry);
        pw.println("  Already expired     : " + errExpired);
        pw.println("  Invalid quantity    : " + errBadQty);
        pw.println("  Invalid price       : " + errBadPrice);
        pw.println("==============================================");
    }
}