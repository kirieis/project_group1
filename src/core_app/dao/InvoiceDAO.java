package core_app.dao;

import core_app.model.Invoice;
import core_app.model.InvoiceDetail;
import core_app.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {
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

                    // ✅ Nếu đơn hàng được tạo với status PAID → trừ kho ngay
                    if ("PAID".equals(invoice.getStatus())) {
                        deductStock(invId);
                        System.out.println("📦 [STOCK] Auto-deducted for PAID invoice #" + invId);
                    }

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
        // Optimized: Single query using LEFT JOIN to fetch invoices and their details
        // together
        // This avoids the N+1 problem (70,000+ queries) which would crash the dashboard
        String sql = "SELECT i.*, c.full_name as customer_name, id.medicine_id, id.medicine_name, id.quantity, id.unit_price, id.import_price "
                +
                "FROM Invoice i " +
                "LEFT JOIN Customer c ON i.customer_id = c.customer_id " +
                "LEFT JOIN Invoice_Detail id ON i.invoice_id = id.invoice_id " +
                "ORDER BY i.invoice_id DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            Invoice currentInvoice = null;
            while (rs.next()) {
                int invId = rs.getInt("invoice_id");

                // If we hit a new invoice ID, add the previous one and create a new one
                if (currentInvoice == null || currentInvoice.getInvoiceId() != invId) {
                    currentInvoice = new Invoice();
                    currentInvoice.setInvoiceId(invId);
                    currentInvoice.setTotalAmount(rs.getDouble("total_amount"));
                    currentInvoice.setPaymentMethod(rs.getString("payment_method"));
                    currentInvoice.setStatus(rs.getString("status"));
                    currentInvoice.setPaymentProof(rs.getString("payment_proof"));
                    currentInvoice.setCustomerId(rs.getInt("customer_id"));
                    currentInvoice.setCustomerName(rs.getString("customer_name"));

                    Timestamp ts = rs.getTimestamp("invoice_date");
                    if (ts != null)
                        currentInvoice.setInvoiceDate(ts.toLocalDateTime());

                    currentInvoice.setInvoiceDetails(new ArrayList<>());
                    list.add(currentInvoice);
                }

                // Add the detail (if any) to the current invoice
                String medId = rs.getString("medicine_id");
                if (medId != null) {
                    InvoiceDetail det = new InvoiceDetail(
                            medId,
                            rs.getString("medicine_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"));
                    det.setImportPrice(rs.getDouble("import_price"));
                    currentInvoice.getInvoiceDetails().add(det);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ GET ALL ERROR: " + e.getMessage());
            e.printStackTrace();
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

            // ✅ STOCK DEDUCTION: Khi đơn hàng được duyệt (PAID) → trừ kho
            if (rows > 0 && "PAID".equals(status)) {
                deductStock(orderId);
            }

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
        // Lấy danh sách các đơn SEPAY đang PENDING trước khi approve
        String selectSql = "SELECT invoice_id FROM Invoice WHERE payment_method = 'SEPAY' AND status = 'PENDING'";
        String updateSql = "UPDATE Invoice SET status = 'PAID' WHERE payment_method = 'SEPAY' AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // 1. Lấy danh sách order IDs trước
            List<Integer> orderIds = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(selectSql);
            while (rs.next()) {
                orderIds.add(rs.getInt("invoice_id"));
            }

            // 2. Update status
            int rows = stmt.executeUpdate(updateSql);
            System.out.println("[InvoiceDAO] approveAllSepayOrders: updated " + rows + " rows");

            // 3. Trừ kho cho từng đơn
            for (int orderId : orderIds) {
                deductStock(orderId);
            }

            return rows;
        } catch (SQLException e) {
            System.err.println("[InvoiceDAO] approveAllSepayOrders ERROR: " + e.getMessage());
            return -1;
        }
    }

    private void deductStock(int invoiceId) {
        String sqlGetDetails = "SELECT medicine_id, quantity FROM Invoice_Detail WHERE invoice_id = ?";
        String sqlUpdateStock = "UPDATE Medicine SET quantity = CASE WHEN quantity - ? < 0 THEN 0 ELSE quantity - ? END WHERE medicine_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psGet = conn.prepareStatement(sqlGetDetails)) {
                psGet.setInt(1, invoiceId);
                ResultSet rs = psGet.executeQuery();

                try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStock)) {
                    while (rs.next()) {
                        String medId = rs.getString("medicine_id");
                        int qty = rs.getInt("quantity");

                        psUpdate.setInt(1, qty);
                        psUpdate.setInt(2, qty);
                        psUpdate.setString(3, medId);
                        psUpdate.addBatch();

                        System.out.println("📦 [STOCK] Deducting " + qty + " from medicine: " + medId);
                    }
                    psUpdate.executeBatch();
                }

                conn.commit();
                System.out.println("✅ [STOCK] Stock deduction completed for invoice #" + invoiceId);

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("🔴 [STOCK] Deduction FAILED for invoice #" + invoiceId + ": " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("🔴 [STOCK] Connection error: " + e.getMessage());
        }
    }

    /**
     * ✅ GET TOTAL STOCK - Tổng tồn kho toàn bộ hệ thống
     * Dùng để verify trên dashboard: SUM(quantity) FROM Medicine
     */
    public long getTotalStock() {
        String sql = "SELECT SUM(CAST(quantity AS BIGINT)) AS total_stock FROM Medicine";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong("total_stock");
            }
        } catch (SQLException e) {
            System.err.println("[InvoiceDAO] getTotalStock ERROR: " + e.getMessage());
        }
        return 0;
    }
}
