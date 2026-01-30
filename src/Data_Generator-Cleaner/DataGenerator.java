
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
        String[] dosageFormlist = {"Tablet", "Capsule", "Syrup"};
        String[] strengthlist = {"500", "200", "10", "7.5", "300","250","400","20","40","2","5","120","650","4200","625","50","16","80","300","1"};
        String[] unitlist = {"mg", "ml"};
        String[] manufacturerlist = {"Sanofi", "Bayer", "GSK", "Abbott", "Imexpharm", "DHG Pharma", "Pfizer", "OPV Pharma", "Kingphar", "UPSA","UDI","ASDEX","Stella","AstraZeneca","United Pharma","Mega We Care","Traphaco","Choay Pharma","Sandoz"};

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
			if(errorbatch){
				batch = "???";
			} else {
				batch = (char) ('A' + r.nextInt(3)) + String.valueOf(r.nextInt(100));
			}

            // default values
            String ingredient = "unknown";
            String dosageForm = "Tablet";
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

default:
    ingredient = ingredientlist[r.nextInt(ingredientlist.length)];

    dosageForm = dosageFormlist[r.nextInt(dosageFormlist.length)];
    strength   = strengthlist[r.nextInt(strengthlist.length)];
    unit       = unitlist[r.nextInt(unitlist.length)];
    manufacturer = manufacturerlist[r.nextInt(manufacturerlist.length)];

    // logic hợp lý: Syrup → ml, còn lại → mg
    if (dosageForm.equals("Syrup"))
        unit = "ml";
    break;


}


    int basePrice;
	 if(errorprice){
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
    }else{
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
