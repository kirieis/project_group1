import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;

public class DataCleaner {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("medicines_raw.csv"));
        FileWriter fw = new FileWriter("medicines_clean.csv"); // cleaned records -> about 9100 records

        fw.write(br.readLine() + "\n");
        String line;

        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            try {
                if (p.length != 6) continue;
                if (p[0].isEmpty()) continue;
                if (p[1].equals("###")) continue;
                if (p[3].equals("unknown ingredient")) continue;
                if (Integer.parseInt(p[5]) <= 0) continue;
                LocalDate.parse(p[4]);
                fw.write(line + "\n");
            } catch (Exception ignored) {}
        }
        br.close();
        fw.close();
    }
}
