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
                if (p.length != 5) continue;
                if (p[0].isEmpty()) continue;
                if (Integer.parseInt(p[4]) <= 0) continue;
                LocalDate.parse(p[3]);
                fw.write(line + "\n");
            } catch (Exception ignored) {}
        }
        br.close();
        fw.close();
    }
}
