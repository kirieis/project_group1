package core_app.dao;

import core_app.model.Invoice;
import core_app.model.InvoiceDetail;
import core_app.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    static {
        core_app.util.SchemaUpdate.updateSchema();
    }

    private final MedicineDAO medicineDAO = new MedicineDAO();

    public int addInvoice(Invoice invoice) {
        String sqlInvoice = "INSERT INTO Invoice (customer_id, total_amount, payment_method, status, payment_proof) VALUES (?, ?, ?, ?, ?)";
        // Added import_price to track profit per sale
        String sqlDetail = "INSERT INTO Invoice_Detail (invoice_id, medicine_id, medicine_name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("[InvoiceDAO] DB Connection OK");
            conn.setAutoCommit(false);
            try (PreparedStatement psInvoice = conn.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                if (invoice.getCustomerId() > 0)
                    psInvoice.setInt(1, invoice.getCustomerId());
                else
                    psInvoice.setNull(1, Types.INTEGER);

                psInvoice.setDouble(2, invoice.getTotalAmount());
                psInvoice.setString(3, invoice.getPaymentMethod());
                psInvoice.setString(4, invoice.getStatus() != null ? invoice.getStatus() : "PENDING");
                psInvoice.setString(5, invoice.getPaymentProof());

                System.out.println("[InvoiceDAO] Inserting invoice: customerId=" + invoice.getCustomerId()
                        + " total=" + invoice.getTotalAmount() + " method=" + invoice.getPaymentMethod());
                psInvoice.executeUpdate();

                ResultSet rs = psInvoice.getGeneratedKeys();
                if (rs.next()) {
                    int invId = rs.getInt(1);
                    System.out.println("[InvoiceDAO] Invoice created with ID: " + invId);

                    // Đảm bảo tất cả medicine tồn tại TRƯỚC KHI insert detail (dùng connection
                    // riêng, auto-commit)
                    for (InvoiceDetail d : invoice.getInvoiceDetails()) {
                        String mId = d.getMedicineId();
                        if (mId == null || mId.trim().isEmpty())
                            mId = "UNKNOWN";
                        mId = mId.trim();
                        medicineDAO.ensureMedicineExists(mId, d.getMedicineName(), d.getUnitPrice());
                    }

                    // Insert detail (không có import_price để tránh lỗi cột thiếu)
                    try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                        for (InvoiceDetail d : invoice.getInvoiceDetails()) {
                            String mId = d.getMedicineId();
                            if (mId == null || mId.trim().isEmpty())
                                mId = "UNKNOWN";
                            mId = mId.trim();

                            psDetail.setInt(1, invId);
                            psDetail.setString(2, mId);
                            psDetail.setString(3, d.getMedicineName());
                            psDetail.setInt(4, d.getQuantity());
                            psDetail.setDouble(5, d.getUnitPrice());
                            psDetail.addBatch();
                        }
                        psDetail.executeBatch();
                    }

                    conn.commit();
                    System.out.println("[InvoiceDAO] Invoice committed successfully: " + invId);
                    return invId;
                }
                System.err.println("[InvoiceDAO] ERROR: No generated key returned");
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[InvoiceDAO] SQL ERROR: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Database Error: " + e.getMessage());
            }
        } catch (RuntimeException e) {
            throw e; // Re-throw
        } catch (Exception e) {
            System.err.println("[InvoiceDAO] CONNECTION ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public List<Invoice> getInvoicesByCustomerId(int customerId) {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM Invoice WHERE customer_id = ? ORDER BY invoice_id DESC";
        String sqlDetail = "SELECT * FROM Invoice_Detail WHERE invoice_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invoice inv = new Invoice();
                inv.setInvoiceId(rs.getInt("invoice_id"));
                inv.setTotalAmount(rs.getDouble("total_amount"));
                inv.setPaymentMethod(rs.getString("payment_method"));
                inv.setStatus(rs.getString("status"));
                inv.setPaymentProof(rs.getString("payment_proof"));
                Timestamp ts = rs.getTimestamp("invoice_date");
                if (ts != null)
                    inv.setInvoiceDate(ts.toLocalDateTime());

                try (PreparedStatement psD = conn.prepareStatement(sqlDetail)) {
                    psD.setInt(1, inv.getInvoiceId());
                    ResultSet rsD = psD.executeQuery();
                    List<InvoiceDetail> details = new ArrayList<>();
                    while (rsD.next()) {
                        InvoiceDetail det = new InvoiceDetail(
                                rsD.getString("medicine_id"),
                                rsD.getString("medicine_name"),
                                rsD.getInt("quantity"),
                                rsD.getDouble("unit_price"));
                        try {
                            det.setImportPrice(rsD.getDouble("import_price"));
                        } catch (SQLException e) {
                            // Column might not exist or verify logic
                        }
                        details.add(det);
                    }
                    inv.setInvoiceDetails(details);
                }
                list.add(inv);
            }
        } catch (SQLException e) {
            System.err.println("❌ GET ERROR: " + e.getMessage());
        }
        return list;
    }

    public List<Invoice> getAllInvoices() {
        List<Invoice> list = new ArrayList<>();
        // Query joining with Customer to get customer names for sales history
        String sql = "SELECT i.*, c.full_name as customer_name FROM Invoice i " +
                "LEFT JOIN Customer c ON i.customer_id = c.customer_id " +
                "ORDER BY i.invoice_id DESC";
        String sqlDetail = "SELECT * FROM Invoice_Detail WHERE invoice_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invoice inv = new Invoice();
                inv.setInvoiceId(rs.getInt("invoice_id"));
                inv.setTotalAmount(rs.getDouble("total_amount"));
                inv.setPaymentMethod(rs.getString("payment_method"));
                inv.setStatus(rs.getString("status"));
                inv.setPaymentProof(rs.getString("payment_proof"));
                inv.setCustomerId(rs.getInt("customer_id"));
                inv.setCustomerName(rs.getString("customer_name"));
                // We'll use a temporary hack or extend Invoice model to store customer name if
                // needed,
                // but let's just stick to the model for now.

                Timestamp ts = rs.getTimestamp("invoice_date");
                if (ts != null)
                    inv.setInvoiceDate(ts.toLocalDateTime());

                try (PreparedStatement psD = conn.prepareStatement(sqlDetail)) {
                    psD.setInt(1, inv.getInvoiceId());
                    ResultSet rsD = psD.executeQuery();
                    List<InvoiceDetail> details = new ArrayList<>();
                    while (rsD.next()) {
                        InvoiceDetail det = new InvoiceDetail(
                                rsD.getString("medicine_id"),
                                rsD.getString("medicine_name"),
                                rsD.getInt("quantity"),
                                rsD.getDouble("unit_price"));
                        try {
                            det.setImportPrice(rsD.getDouble("import_price"));
                        } catch (SQLException e) {
                        }
                        details.add(det);
                    }
                    inv.setInvoiceDetails(details);
                }
                list.add(inv);
            }
        } catch (SQLException e) {
            System.err.println("❌ GET ALL ERROR: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int orderId, String status) {
        String sql = "UPDATE Invoice SET status = ? WHERE invoice_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            int rows = ps.executeUpdate();
            System.out.println("[InvoiceDAO] updateStatus: orderId=" + orderId + " status=" + status + " rows=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[InvoiceDAO] updateStatus ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public String getInvoiceStatus(int invoiceId) {
        String sql = "SELECT status FROM Invoice WHERE invoice_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            System.err.println("[InvoiceDAO] getInvoiceStatus ERROR: " + e.getMessage());
        }
        return null;
    }

    public int approveAllSepayOrders() {
        String sql = "UPDATE Invoice SET status = 'PAID' WHERE payment_method = 'SEPAY' AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            System.out.println("[InvoiceDAO] approveAllSepayOrders: updated " + rows + " rows");
            return rows;
        } catch (SQLException e) {
            System.err.println("[InvoiceDAO] approveAllSepayOrders ERROR: " + e.getMessage());
            return -1;
        }
    }
}
