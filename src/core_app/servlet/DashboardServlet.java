package core_app.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import core_app.dao.BatchDAO;
import core_app.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Inject threshold constants vào SQL để đồng bộ với BatchDAO
        int removeThreshold = BatchDAO.DAYS_REMOVE_THRESHOLD;
        int saleThreshold = BatchDAO.DAYS_SALE_THRESHOLD;
        int discount = BatchDAO.NEAR_EXPIRY_DISCOUNT;

        String sql = """
                    SELECT
                        m.medicine_id,
                        m.name,
                        b.batch_id,
                        b.batch_number,
                        b.expiry_date,
                        b.quantity_available,
                        b.import_price,
                        DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) AS days_until_expiry,
                        CASE
                            WHEN b.expiry_date <= CAST(GETDATE() AS DATE) THEN 'EXPIRED'
                            WHEN DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) <= %d THEN 'REMOVED'
                            WHEN DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) <= %d THEN 'NEAR_EXPIRY'
                            WHEN DATEDIFF(DAY, CAST(GETDATE() AS DATE), b.expiry_date) <= 90 THEN 'WARNING'
                            ELSE 'OK'
                        END AS expiry_status
                    FROM Medicine m
                    JOIN Batch b ON m.medicine_id = b.medicine_id
                    WHERE b.quantity_available > 0
                    ORDER BY b.expiry_date ASC
                """.formatted(removeThreshold, saleThreshold);

        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            out.print("[");
            boolean first = true;

            while (rs.next()) {
                if (!first)
                    out.print(",");
                first = false;

                String status = rs.getString("expiry_status");
                int daysLeft = rs.getInt("days_until_expiry");
                // Assign discount: 30% nếu NEAR_EXPIRY (16–30 ngày), 0 cho các trạng thái khác
                int discountValue = "NEAR_EXPIRY".equals(status) ? discount : 0;

                out.print("""
                            {
                              "id":"%s",
                              "name":"%s",
                              "batchId":%d,
                              "batchNumber":"%s",
                              "expiry":"%s",
                              "daysUntilExpiry":%d,
                              "expiryStatus":"%s",
                              "quantity":%d,
                              "price":%f,
                              "discount":%d
                            }
                        """.formatted(
                        rs.getString("medicine_id"),
                        rs.getString("name"),
                        rs.getInt("batch_id"),
                        rs.getString("batch_number"),
                        rs.getDate("expiry_date"),
                        daysLeft,
                        status,
                        rs.getInt("quantity_available"),
                        rs.getDouble("import_price"),
                        discountValue));
            }
            out.print("]");

        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
