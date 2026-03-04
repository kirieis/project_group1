
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Random;

public class DataGenerator {

    public static void main(String[] args) throws Exception {
        FileWriter fw = new FileWriter("medicines_raw.csv");
        fw.write("medicine_id,name,batch,ingredient,dosage_form,strength,unit,manufacturer,expiry,quantity,price\n");

        String[] medicines = {
                "Paracetamol_500mg",
                "Aspirin_500mg",
                "Amoxicillin_500mg",
                "Ibuprofen_200mg",
                "Cefixime_200mg",
                "Vitamin_C",
                "Meloxicam_7.5mg",
                "Cefdinir_300mg",
                "SkillMax_Ocavill",
                "Siro_Ginkid_ZinC",
                "Echina_Kingphar",
                "Panadol_Extra",
                "Efferalgan_500mg",
                "Hapacol_650",
                "Alphachoay",
                "Augmentin_625mg",
                "Cefuroxim_500mg",
                "Acyclovir_400mg",
                "Nexium_mups_20mg",
                "Loperamid_2mg",
                "Enterogermina",
                "Tiffy_Dey",
                "Telfast_HD_180mg",
                "Eugica",
                "Enat_400",
                "Ginkgo_Biloba_120mg",
                "Clarithromycin_500mg",
                "Azithromycin_250mg",
                "Ciprofloxacin_500mg",
                "Levofloxacin_500mg",
                "Metronidazole_500mg",
                "Omeprazole_20mg",
                "Pantoprazole_40mg",
                "Esomeprazole_40mg",
                "Domperidone_10mg",
                "Metoclopramide_10mg",
                "Cetirizine_10mg",
                "Loratadine_10mg",
                "Desloratadine_5mg",
                "Fexofenadine_120mg",
                "Salbutamol_4mg",
                "Ambroxol_30mg",
                "Bromhexine_8mg",
                "Acetylcysteine_200mg",
                "Calcium_Carbonate_500mg",
                "Calcium_D3",
                "Magnesium_B6",
                "Vitamin_B1_B6_B12",
                "Vitamin_E_400IU",
                "Iron_Folic",
                "ORS_Plus",
                "Smecta",
                "Krebs_Cough_Syrup",
                "Prospan_Syrup",
                "Zinc_Gluconate_20mg",
                "Hydrocortisone_10mg",
                "Betadine_Solution",
                "Clotrimazole_1pct",
                "Ketoconazole_200mg",
                "Fluconazole_150mg",
                "Nystatin_Suspension",
                "Diclofenac_50mg",
                "Celecoxib_200mg",
                "Tramadol_50mg",
                "Prednisolone_5mg",
                "Methylprednisolone_16mg",
                "Losartan_50mg",
                "Amlodipine_5mg",
                "Bisoprolol_5mg",
                "Atorvastatin_20mg",
                "Simvastatin_20mg",
                "Metformin_500mg",
                "Gliclazide_80mg",
                "Insulin_Rapid",
                "Insulin_Basal",
                "Levothyroxine_50mcg",
                "Allopurinol_300mg",
                "Colchicine_1mg",
                "Vitamin_K2",
                "Ranitidine_150mg",
                "Famotidine_20mg",
                "Sucralfate_1g",
                "Bismuth_Subsalicylate",
                "Montelukast_10mg",
                "Theophylline_200mg",
                "Spironolactone_25mg",
                "Furosemide_40mg",
                "Hydrochlorothiazide_25mg",
                "Clopidogrel_75mg",
                "Aspirin_81mg",
                "Warfarin_5mg",
                "Enalapril_10mg",
                "Captopril_25mg",
                "Valsartan_80mg",
                "Glimepiride_2mg",
                "Sitagliptin_100mg",
                "Gabapentin_300mg",
                "Carbamazepine_200mg",
                "Diazepam_5mg",

        };

        int[] pricePool = {
                1500, 1600, 1700, 2000, 1000,
                2500, 3000, 1200, 1250, 1450
        };
        String[] ingredientlist = {
                "Paracetamol", "Aspirin", "Amoxicillin", "Ibuprofen", "Cefixime",
                "Vitamin C", "Meloxicam", "Cefdinir", "Multivitamin", "Zinc + Vitamin",
                "Echinacea", "Paracetamol + Caffeine", "Ascorbic Acid", "Alpha chymotrypsin",
                "Amoxicillin + Clavulanate", "Cefuroxime", "Acyclovir"
        };
        String[] dosageFormlist = { "Tablet", "Capsule", "Syrup" };
        String[] strengthlist = { "500", "200", "10", "7.5", "300", "250", "400", "20", "40", "2", "5", "120", "650",
                "4200", "625", "50", "16", "80", "300", "1" };
        String[] unitlist = { "mg", "ml" };
        String[] manufacturerlist = { "Sanofi", "Bayer", "GSK", "Abbott", "Imexpharm", "DHG Pharma", "Pfizer",
                "OPV Pharma", "Kingphar", "UPSA", "UDI", "ASDEX", "Stella", "AstraZeneca", "United Pharma",
                "Mega We Care", "Traphaco", "Choay Pharma", "Sandoz" };

        Random r = new Random();

        for (int i = 1; i <= 35000; i++) {
            boolean errorid = r.nextInt(100) < 8;
            boolean errorname = r.nextInt(100) < 8;
            boolean errorbatch = r.nextInt(100) < 8;
            boolean errorexpiry = r.nextInt(100) < 8;
            boolean errorqty = r.nextInt(100) < 8;
            boolean errorprice = r.nextInt(100) < 8;

            String id = errorid ? "" : "MED" + i;
            String name = errorname ? "###" : medicines[r.nextInt(medicines.length)];

            String batch;
            if (errorbatch) {
                batch = "???";
            } else {
                batch = (char) ('A' + r.nextInt(3)) + String.valueOf(r.nextInt(100));
            }

            // default values
            String ingredient = "unknown";
            String dosageForm = "TABLET";
            String strength = "500";
            String unit = "mg";
            String manufacturer = "Unknown";

            switch (name) {

                case "Paracetamol_500mg":
                    ingredient = "Paracetamol";
                    manufacturer = "Sanofi";
                    break;

                case "Aspirin_500mg":
                    ingredient = "Aspirin";
                    manufacturer = "Bayer";
                    break;

                case "Amoxicillin_500mg":
                    ingredient = "Amoxicillin";
                    dosageForm = "Capsule";
                    manufacturer = "GSK";
                    break;

                case "Ibuprofen_200mg":
                    ingredient = "Ibuprofen";
                    strength = "200";
                    manufacturer = "Abbott";
                    break;

                case "Cefixime_200mg":
                    ingredient = "Cefixime";
                    dosageForm = "Capsule";
                    strength = "200";
                    manufacturer = "Imexpharm";
                    break;

                case "Vitamin_C":
                    ingredient = "Ascorbic Acid";
                    manufacturer = "DHG Pharma";
                    break;

                case "Meloxicam_7.5mg":
                    ingredient = "Meloxicam";
                    strength = "7.5";
                    manufacturer = "Sanofi";
                    break;

                case "Cefdinir_300mg":
                    ingredient = "Cefdinir";
                    dosageForm = "Capsule";
                    strength = "300";
                    manufacturer = "Pfizer";
                    break;

                case "SkillMax_Ocavill":
                    ingredient = "Multivitamin";
                    dosageForm = "Syrup";
                    strength = "10";
                    unit = "ml";
                    manufacturer = "OPV Pharma";
                    break;

                case "Siro_Ginkid_ZinC":
                    ingredient = "Zinc + Vitamin";
                    dosageForm = "Syrup";
                    strength = "10";
                    unit = "ml";
                    manufacturer = "OPV Pharma";
                    break;

                case "Echina_Kingphar":
                    ingredient = "Echinacea";
                    manufacturer = "Kingphar";
                    break;

                case "Panadol_Extra":
                    ingredient = "Paracetamol + Caffeine";
                    manufacturer = "GSK";
                    break;

                case "Efferalgan_500mg":
                    ingredient = "Paracetamol";
                    manufacturer = "UPSA";
                    break;

                case "Hapacol_650":
                    ingredient = "Paracetamol";
                    strength = "650";
                    manufacturer = "DHG Pharma";
                    break;

                case "Alphachoay":
                    ingredient = "Alpha chymotrypsin";
                    strength = "4200";
                    unit = "IU";
                    manufacturer = "Choay Pharma";
                    break;

                case "Augmentin_625mg":
                    ingredient = "Amoxicillin + Clavulanate";
                    strength = "625";
                    manufacturer = "GSK";
                    break;

                case "Cefuroxim_500mg":
                    ingredient = "Cefuroxime";
                    manufacturer = "Sandoz";
                    break;

                case "Acyclovir_400mg":
                    ingredient = "Acyclovir";
                    strength = "400";
                    manufacturer = "Stella";
                    break;

                case "Nexium_mups_20mg":
                    ingredient = "Esomeprazole";
                    strength = "20";
                    manufacturer = "AstraZeneca";
                    break;

                case "Loperamid_2mg":
                    ingredient = "Loperamide";
                    strength = "2";
                    manufacturer = "Imexpharm";
                    break;

                case "Enterogermina":
                    ingredient = "Bacillus clausii";
                    dosageForm = "Suspension";
                    strength = "5";
                    unit = "ml";
                    manufacturer = "Sanofi";
                    break;

                case "Tiffy_Dey":
                    ingredient = "Paracetamol + Chlorpheniramine";
                    manufacturer = "United Pharma";
                    break;

                case "Telfast_HD_180mg":
                    ingredient = "Fexofenadine";
                    strength = "180";
                    manufacturer = "Sanofi";
                    break;

                case "Eugica":
                    ingredient = "Herbal Extract";
                    dosageForm = "Syrup";
                    strength = "10";
                    unit = "ml";
                    manufacturer = "Mega We Care";
                    break;

                case "Enat_400":
                    ingredient = "Vitamin E";
                    strength = "400";
                    unit = "IU";
                    manufacturer = "Mega We Care";
                    break;

                case "Ginkgo_Biloba_120mg":
                    ingredient = "Ginkgo Biloba";
                    strength = "120";
                    manufacturer = "Traphaco";
                    break;

                case "Clarithromycin_500mg":
                    ingredient = "Clarithromycin";
                    manufacturer = "Abbott";
                    break;

                case "Azithromycin_250mg":
                    ingredient = "Azithromycin";
                    dosageForm = "Capsule";
                    strength = "250";
                    manufacturer = "Pfizer";
                    break;

                case "Ciprofloxacin_500mg":
                    ingredient = "Ciprofloxacin";
                    manufacturer = "Bayer";
                    break;

                case "Levofloxacin_500mg":
                    ingredient = "Levofloxacin";
                    manufacturer = "Sanofi";
                    break;

                case "Metronidazole_500mg":
                    ingredient = "Metronidazole";
                    manufacturer = "Imexpharm";
                    break;

                case "Omeprazole_20mg":
                    ingredient = "Omeprazole";
                    dosageForm = "Capsule";
                    strength = "20";
                    manufacturer = "AstraZeneca";
                    break;

                case "Pantoprazole_40mg":
                    ingredient = "Pantoprazole";
                    strength = "40";
                    manufacturer = "Abbott";
                    break;

                case "Esomeprazole_40mg":
                    ingredient = "Esomeprazole";
                    strength = "40";
                    manufacturer = "AstraZeneca";
                    break;

                case "Domperidone_10mg":
                    ingredient = "Domperidone";
                    strength = "10";
                    manufacturer = "Sanofi";
                    break;

                case "Metoclopramide_10mg":
                    ingredient = "Metoclopramide";
                    strength = "10";
                    manufacturer = "Imexpharm";
                    break;

                case "Cetirizine_10mg":
                    ingredient = "Cetirizine";
                    strength = "10";
                    manufacturer = "GSK";
                    break;

                case "Loratadine_10mg":
                    ingredient = "Loratadine";
                    strength = "10";
                    manufacturer = "Bayer";
                    break;

                case "Desloratadine_5mg":
                    ingredient = "Desloratadine";
                    strength = "5";
                    manufacturer = "Bayer";
                    break;

                case "Fexofenadine_120mg":
                    ingredient = "Fexofenadine";
                    strength = "120";
                    manufacturer = "Sanofi";
                    break;

                case "Salbutamol_4mg":
                    ingredient = "Salbutamol";
                    strength = "4";
                    manufacturer = "GSK";
                    break;

                case "Ambroxol_30mg":
                    ingredient = "Ambroxol";
                    strength = "30";
                    manufacturer = "Sanofi";
                    break;

                case "Bromhexine_8mg":
                    ingredient = "Bromhexine";
                    strength = "8";
                    manufacturer = "Imexpharm";
                    break;

                case "Acetylcysteine_200mg":
                    ingredient = "Acetylcysteine";
                    strength = "200";
                    manufacturer = "Sandoz";
                    break;

                case "Calcium_Carbonate_500mg":
                    ingredient = "Calcium Carbonate";
                    manufacturer = "DHG Pharma";
                    break;

                case "Calcium_D3":
                    ingredient = "Calcium + Vitamin D3";
                    manufacturer = "Mega We Care";
                    break;

                case "Magnesium_B6":
                    ingredient = "Magnesium + Vitamin B6";
                    manufacturer = "Sanofi";
                    break;

                case "Vitamin_B1_B6_B12":
                    ingredient = "Vitamin B1 + B6 + B12";
                    manufacturer = "DHG Pharma";
                    break;

                case "Vitamin_E_400IU":
                    ingredient = "Vitamin E";
                    strength = "400";
                    unit = "IU";
                    manufacturer = "Mega We Care";
                    break;

                case "Iron_Folic":
                    ingredient = "Iron + Folic Acid";
                    manufacturer = "DHG Pharma";
                    break;

                case "ORS_Plus":
                    ingredient = "Oral Rehydration Salts";
                    dosageForm = "Powder";
                    strength = "4.1";
                    unit = "g";
                    manufacturer = "OPV Pharma";
                    break;

                case "Smecta":
                    ingredient = "Diosmectite";
                    dosageForm = "Powder";
                    strength = "3";
                    unit = "g";
                    manufacturer = "Ipsen";
                    break;

                case "Krebs_Cough_Syrup":
                    ingredient = "Dextromethorphan + Guaifenesin";
                    dosageForm = "Syrup";
                    strength = "10";
                    unit = "ml";
                    manufacturer = "OPV Pharma";
                    break;

                case "Prospan_Syrup":
                    ingredient = "Ivy Leaf Extract";
                    dosageForm = "Syrup";
                    strength = "10";
                    unit = "ml";
                    manufacturer = "Engelhard";
                    break;

                case "Zinc_Gluconate_20mg":
                    ingredient = "Zinc Gluconate";
                    strength = "20";
                    manufacturer = "Sanofi";
                    break;

                case "Hydrocortisone_10mg":
                    ingredient = "Hydrocortisone";
                    strength = "10";
                    manufacturer = "Pfizer";
                    break;

                case "Betadine_Solution":
                    ingredient = "Povidone-Iodine";
                    dosageForm = "Solution";
                    strength = "10";
                    unit = "ml";
                    manufacturer = "Mundipharma";
                    break;

                case "Clotrimazole_1pct":
                    ingredient = "Clotrimazole";
                    dosageForm = "Cream";
                    strength = "1";
                    unit = "%";
                    manufacturer = "Bayer";
                    break;

                case "Ketoconazole_200mg":
                    ingredient = "Ketoconazole";
                    strength = "200";
                    manufacturer = "Imexpharm";
                    break;

                case "Fluconazole_150mg":
                    ingredient = "Fluconazole";
                    dosageForm = "Capsule";
                    strength = "150";
                    manufacturer = "Pfizer";
                    break;

                case "Nystatin_Suspension":
                    ingredient = "Nystatin";
                    dosageForm = "Suspension";
                    strength = "100000";
                    unit = "IU";
                    manufacturer = "Teva";
                    break;

                case "Diclofenac_50mg":
                    ingredient = "Diclofenac";
                    strength = "50";
                    manufacturer = "Sandoz";
                    break;

                case "Celecoxib_200mg":
                    ingredient = "Celecoxib";
                    dosageForm = "Capsule";
                    strength = "200";
                    manufacturer = "Pfizer";
                    break;

                case "Tramadol_50mg":
                    ingredient = "Tramadol";
                    dosageForm = "Capsule";
                    strength = "50";
                    manufacturer = "Stella";
                    break;

                case "Prednisolone_5mg":
                    ingredient = "Prednisolone";
                    strength = "5";
                    manufacturer = "Sanofi";
                    break;

                case "Methylprednisolone_16mg":
                    ingredient = "Methylprednisolone";
                    strength = "16";
                    manufacturer = "Pfizer";
                    break;

                case "Losartan_50mg":
                    ingredient = "Losartan";
                    strength = "50";
                    manufacturer = "MSD";
                    break;

                case "Amlodipine_5mg":
                    ingredient = "Amlodipine";
                    strength = "5";
                    manufacturer = "Pfizer";
                    break;

                case "Bisoprolol_5mg":
                    ingredient = "Bisoprolol";
                    strength = "5";
                    manufacturer = "Sandoz";
                    break;

                case "Atorvastatin_20mg":
                    ingredient = "Atorvastatin";
                    strength = "20";
                    manufacturer = "Pfizer";
                    break;

                case "Simvastatin_20mg":
                    ingredient = "Simvastatin";
                    strength = "20";
                    manufacturer = "MSD";
                    break;

                case "Metformin_500mg":
                    ingredient = "Metformin";
                    manufacturer = "Stella";
                    break;

                case "Gliclazide_80mg":
                    ingredient = "Gliclazide";
                    strength = "80";
                    manufacturer = "Servier";
                    break;

                case "Insulin_Rapid":
                    ingredient = "Insulin Lispro";
                    dosageForm = "Injection";
                    strength = "100";
                    unit = "IU/ml";
                    manufacturer = "Novo Nordisk";
                    break;

                case "Insulin_Basal":
                    ingredient = "Insulin Glargine";
                    dosageForm = "Injection";
                    strength = "100";
                    unit = "IU/ml";
                    manufacturer = "Sanofi";
                    break;

                case "Levothyroxine_50mcg":
                    ingredient = "Levothyroxine";
                    strength = "50";
                    unit = "mcg";
                    manufacturer = "Abbott";
                    break;

                case "Allopurinol_300mg":
                    ingredient = "Allopurinol";
                    strength = "300";
                    manufacturer = "GSK";
                    break;

                case "Colchicine_1mg":
                    ingredient = "Colchicine";
                    strength = "1";
                    manufacturer = "Traphaco";
                    break;

                case "Vitamin_K2":
                    ingredient = "Menaquinone-7";
                    strength = "100";
                    unit = "mcg";
                    manufacturer = "DHG Pharma";
                    break;

                case "Ranitidine_150mg":
                    ingredient = "Ranitidine";
                    strength = "150";
                    manufacturer = "GSK";
                    break;

                case "Famotidine_20mg":
                    ingredient = "Famotidine";
                    strength = "20";
                    manufacturer = "Imexpharm";
                    break;

                case "Sucralfate_1g":
                    ingredient = "Sucralfate";
                    dosageForm = "Suspension";
                    strength = "1";
                    unit = "g";
                    manufacturer = "Abbott";
                    break;

                case "Bismuth_Subsalicylate":
                    ingredient = "Bismuth Subsalicylate";
                    dosageForm = "Suspension";
                    strength = "262";
                    manufacturer = "Procter & Gamble";
                    break;

                case "Montelukast_10mg":
                    ingredient = "Montelukast";
                    strength = "10";
                    manufacturer = "MSD";
                    break;

                case "Theophylline_200mg":
                    ingredient = "Theophylline";
                    strength = "200";
                    manufacturer = "Sanofi";
                    break;

                case "Spironolactone_25mg":
                    ingredient = "Spironolactone";
                    strength = "25";
                    manufacturer = "Pfizer";
                    break;

                case "Furosemide_40mg":
                    ingredient = "Furosemide";
                    strength = "40";
                    manufacturer = "Sanofi";
                    break;

                case "Hydrochlorothiazide_25mg":
                    ingredient = "Hydrochlorothiazide";
                    strength = "25";
                    manufacturer = "Sandoz";
                    break;

                case "Clopidogrel_75mg":
                    ingredient = "Clopidogrel";
                    strength = "75";
                    manufacturer = "Sanofi";
                    break;

                case "Aspirin_81mg":
                    ingredient = "Aspirin";
                    strength = "81";
                    manufacturer = "Bayer";
                    break;

                case "Warfarin_5mg":
                    ingredient = "Warfarin";
                    strength = "5";
                    manufacturer = "Bristol-Myers Squibb";
                    break;

                case "Enalapril_10mg":
                    ingredient = "Enalapril";
                    strength = "10";
                    manufacturer = "MSD";
                    break;

                case "Captopril_25mg":
                    ingredient = "Captopril";
                    strength = "25";
                    manufacturer = "Bristol-Myers Squibb";
                    break;

                case "Valsartan_80mg":
                    ingredient = "Valsartan";
                    strength = "80";
                    manufacturer = "Novartis";
                    break;

                case "Glimepiride_2mg":
                    ingredient = "Glimepiride";
                    strength = "2";
                    manufacturer = "Sanofi";
                    break;

                case "Sitagliptin_100mg":
                    ingredient = "Sitagliptin";
                    strength = "100";
                    manufacturer = "MSD";
                    break;

                case "Gabapentin_300mg":
                    ingredient = "Gabapentin";
                    dosageForm = "Capsule";
                    strength = "300";
                    manufacturer = "Pfizer";
                    break;

                case "Carbamazepine_200mg":
                    ingredient = "Carbamazepine";
                    strength = "200";
                    manufacturer = "Novartis";
                    break;

                case "Diazepam_5mg":
                    ingredient = "Diazepam";
                    strength = "5";
                    manufacturer = "Roche";
                    break;

                default:
                    ingredient = ingredientlist[r.nextInt(ingredientlist.length)];
                    dosageForm = dosageFormlist[r.nextInt(dosageFormlist.length)];
                    strength = strengthlist[r.nextInt(strengthlist.length)];
                    unit = unitlist[r.nextInt(unitlist.length)];
                    manufacturer = manufacturerlist[r.nextInt(manufacturerlist.length)];

                    // logic hợp lý: Syrup → ml, còn lại → mg
                    if (dosageForm.equals("Syrup"))
                        unit = "ml";
                    break;

            }

            int basePrice;
            if (errorprice) {
                basePrice = -r.nextInt(5000);
            } else {
                if (name.contains("Paracetamol") || name.contains("Hapacol")) {
                    basePrice = 700;
                } else if (name.contains("Ibuprofen") || name.contains("Aspirin")) {
                    basePrice = 1000;
                } else if (name.contains("Vitamin") || name.contains("Ginkgo")) {
                    basePrice = 1400;
                } else if (name.contains("Amoxicillin") || name.contains("Cef")) {
                    basePrice = 2000;
                } else if (name.contains("Augmentin")) {
                    basePrice = 2400;
                } else if (dosageForm.equals("Syrup")) {
                    basePrice = 50000;
                } else {
                    basePrice = pricePool[r.nextInt(pricePool.length)];
                }
            }

            String expiry = errorexpiry ? "invalid-date"
                    : LocalDate.now().plusDays(r.nextInt(500) - 200).toString();
            int qty = errorqty ? -r.nextInt(100) : r.nextInt(200) + 1;

            fw.write(id + "," + name + "," + batch + "," + ingredient + ","
                    + dosageForm + "," + strength + "," + unit + ","
                    + manufacturer + "," + expiry + "," + qty + "," + basePrice + "\n");

        }
        fw.close();

    }
}
