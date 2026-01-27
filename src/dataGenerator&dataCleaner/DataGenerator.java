
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Random;

public class DataGenerator {

    public static void main(String[] args) throws Exception {
        FileWriter fw = new FileWriter("medicines_raw.csv");
        fw.write("medicine_id,name,batch,ingredient,dosage_form,strength,unit,manufacturer,price,requires_prescription,expiry,quantity\n");

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
        };

        Random r = new Random();

        for (int i = 1; i <= 15000; i++) {
            boolean error = r.nextInt(100) < 8;

            String id = error ? "" : "MED" + i;
            String name = error ? "###" : medicines[r.nextInt(medicines.length)];

            String batch = (char) ('A' + r.nextInt(26)) + String.valueOf(r.nextInt(100));

            // default values
            String ingredient = "unknown";
            String dosageForm = "Tablet";
            String strength = "500";
            String unit = "mg";
            String manufacturer = "Unknown";

		if (name.equals("Paracetamol_500mg")) {
				ingredient = "Paracetamol";
				manufacturer = "Sanofi";

		} else if (name.equals("Aspirin_500mg")) {
				ingredient = "Aspirin";
				manufacturer = "Bayer";

		} else if (name.equals("Amoxicillin_500mg")) {
				ingredient = "Amoxicillin";
				dosageForm = "Capsule";
				manufacturer = "GSK";

		} else if (name.equals("Ibuprofen_200mg")) {
				ingredient = "Ibuprofen";
				strength = "200";
				manufacturer = "Abbott";

		} else if (name.equals("Cefixime_200mg")) {
				ingredient = "Cefixime";
				dosageForm = "Capsule";
				strength = "200";
				manufacturer = "Imexpharm";

		} else if (name.equals("Vitamin_C")) {
				ingredient = "Ascorbic Acid";
				manufacturer = "DHG Pharma";

		} else if (name.equals("Meloxicam_7.5mg")) {
				ingredient = "Meloxicam";
				strength = "7.5";
				manufacturer = "Sanofi";

		} else if (name.equals("Cefdinir_300mg")) {
				ingredient = "Cefdinir";
				dosageForm = "Capsule";
				strength = "300";
				manufacturer = "Pfizer";

		} else if (name.equals("SkillMax_Ocavill")) {
				ingredient = "Multivitamin";
				dosageForm = "Syrup";
				strength = "10";
				unit = "ml";
				manufacturer = "OPV Pharma";

		} else if (name.equals("Siro_Ginkid_ZinC")) {
				ingredient = "Zinc + Vitamin";
				dosageForm = "Syrup";
				strength = "10";
				unit = "ml";
				manufacturer = "OPV Pharma";

		} else if (name.equals("Echina_Kingphar")) {
				ingredient = "Echinacea";
				manufacturer = "Kingphar";

		} else if (name.equals("Panadol_Extra")) {
				ingredient = "Paracetamol + Caffeine";
				manufacturer = "GSK";

		} else if (name.equals("Efferalgan_500mg")) {
				ingredient = "Paracetamol";
				manufacturer = "UPSA";

		} else if (name.equals("Hapacol_650")) {
				ingredient = "Paracetamol";
				strength = "650";
				manufacturer = "DHG Pharma";

		} else if (name.equals("Alphachoay")) {
				ingredient = "Alpha chymotrypsin";
				unit = "IU";
				strength = "4200";
				manufacturer = "Choay Pharma";

		} else if (name.equals("Augmentin_625mg")) {
				ingredient = "Amoxicillin + Clavulanate";
				strength = "625";
				manufacturer = "GSK";

		} else if (name.equals("Cefuroxim_500mg")) {
				ingredient = "Cefuroxime";
				manufacturer = "Sandoz";

		} else if (name.equals("Acyclovir_400mg")) {
				ingredient = "Acyclovir";
				strength = "400";
				manufacturer = "Stella";

		} else if (name.equals("Nexium_mups_20mg")) {
				ingredient = "Esomeprazole";
				strength = "20";
				manufacturer = "AstraZeneca";

		} else if (name.equals("Loperamid_2mg")) {
				ingredient = "Loperamide";
				strength = "2";
				manufacturer = "Imexpharm";

		} else if (name.equals("Enterogermina")) {
				ingredient = "Bacillus clausii";
				dosageForm = "Suspension";
				strength = "5";
				unit = "ml";
				manufacturer = "Sanofi";

		} else if (name.equals("Tiffy_Dey")) {
				ingredient = "Paracetamol + Chlorpheniramine";
				manufacturer = "United Pharma";

		} else if (name.equals("Telfast_HD_180mg")) {
				ingredient = "Fexofenadine";
				strength = "180";
				manufacturer = "Sanofi";

		} else if (name.equals("Eugica")) {
				ingredient = "Herbal Extract";
				dosageForm = "Syrup";
				strength = "10";
				unit = "ml";
				manufacturer = "Mega We Care";

		} else if (name.equals("Enat_400")) {
				ingredient = "Vitamin E";
				strength = "400";
				unit = "IU";
				manufacturer = "Mega We Care";

		} else if (name.equals("Ginkgo_Biloba_120mg")) {
				ingredient = "Ginkgo Biloba";
				strength = "120";
				manufacturer = "Traphaco";
		}


     double basePrice = 2500;

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
    }

            String expiry = error ? "invalid-date"
                    : LocalDate.now().plusDays(r.nextInt(500) - 200).toString();
            int qty = error ? -r.nextInt(100) : r.nextInt(200) + 1;

            fw.write(id + "," + name + "," + batch + "," + ingredient + ","
                    + dosageForm + "," + strength + "," + unit + ","
                    + manufacturer + "," + expiry + "," + qty + "," + basePrice + "\n");

        }
        fw.close();

    }
}
