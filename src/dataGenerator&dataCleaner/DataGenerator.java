import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Random;

public class DataGenerator {

    public static void main(String[] args) throws Exception {
        FileWriter fw = new FileWriter("medicines_raw.csv"); // 10000 raw records with bugs
        fw.write("medicine_id,name,batch,expiry,quantity\n");

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
            boolean error = r.nextInt(100) < 8; // ~8% lỗi
            String id = error ? "" : "MED" + i;
            String name = error ? "###" : medicines[r.nextInt(medicines.length)];
            char letter = (char) ('A' + r.nextInt(26));
            int num = r.nextInt(100); // 0–99
            String batch = letter + String.valueOf(num);
            String ingredient;

            if(name.equals("Paracetamol_500mg"))
                ingredient = "Paracetamol";
            else if(name.equals("Aspirin_500mg"))
                ingredient = "Aspirin";
            else if(name.equals("Amoxicillin_500mg"))
                ingredient = "Amoxicillin";
            else if(name.equals("Ibuprofen_200mg"))
                ingredient = "Ibuprofen";
            else if(name.equals("Cefixime_200mg"))
                ingredient = "Cefixime";
            else if(name.equals("Vitamin_C"))
                ingredient = "Vitamin C";
            else if(name.equals("Meloxicam_7.5mg"))
                ingredient = "Meloxicam";
            else if(name.equals("Cefdinir_300mg"))
                ingredient = "Cefdinir";
            else if(name.equals("SkillMax_Ocavill"))
                ingredient = "SkillMax Ocavill";
            else if(name.equals("Siro_Ginkid_ZinC"))
                ingredient = "Siro Ginkid ZinC";
            else if(name.equals("Echina_Kingphar"))
                ingredient = "Echina_Kingphar";
            else if(name.equals("Panadol_Extra"))
                ingredient = "Panadol_Extra";
            else if(name.equals("Efferalgan_500mg"))
                ingredient = "Efferalgan_500mg";
            else if(name.equals("Hapacol_650"))
                ingredient = "Hapacol_650";
            else if(name.equals("Alphachoay"))
                ingredient = "Alphachoay";
            else if(name.equals("Augmentin_625mg"))
                ingredient = "Augmentin 625mg";
            else if(name.equals("Cefuroxim_500mg"))
                ingredient = "Cefuroxim 500mg";
            else if(name.equals("Acyclovir_400mg"))
                ingredient = "Acyclovir_400mg";
            else if(name.equals("Nexium_mups_20mg"))
                ingredient = "Nexium mups 20mg";
            else if(name.equals("Loperamid_2mg"))
                ingredient = "Loperamid_2mg";
            else if(name.equals("Enterogermina"))
                ingredient = "Enterogermina";
            else if(name.equals("Tiffy_Dey"))
                ingredient = "Tiffy_Dey";
            else if(name.equals("Telfast_HD_180mg"))    
                ingredient = "Telfast_HD_180mg";
            else if(name.equals("Eugica"))
                ingredient = "Eugica";
            else if(name.equals("Enat_400"))
                ingredient = "Enat_400";
            else if(name.equals("Ginkgo_Biloba_120mg"))
                ingredient = "Ginkgo_Biloba";
            else
                ingredient = "unknown ingredient";
            String expiry = error ? "invalid-date"
                    : LocalDate.now().plusDays(r.nextInt(500) - 200).toString();
            int qty = error ? -r.nextInt(100) : r.nextInt(200) + 1;

            fw.write(id + "," + name + "," + batch + "," + ingredient + "," + expiry + "," + qty + "\n");
        }
        fw.close();
    }
}
