import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        String[] urls = {
            "jdbc:sqlserver://localhost:1433;databaseName=PharmacyDB;encrypt=false;trustServerCertificate=true",
            "jdbc:sqlserver://localhost:1433;databaseName=PharmacyDB;integratedSecurity=true;encrypt=false;trustServerCertificate=true"
        };
        String user = "sa";
        String password = "123456"; // Try a common password or blank
        
        System.out.println("Testing sa/123456...");
        try {
            Connection conn = DriverManager.getConnection(urls[0], user, "123456");
            System.out.println("SUCCESS with sa/123456");
            return;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        System.out.println("Testing sa/1...");
        try {
            Connection conn = DriverManager.getConnection(urls[0], user, "1");
            System.out.println("SUCCESS with sa/1");
            return;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        System.out.println("Testing integratedSecurity=true...");
        try {
            Connection conn = DriverManager.getConnection(urls[1]);
            System.out.println("SUCCESS with Windows Auth");
            return;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}
