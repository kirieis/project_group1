package core_app.dao;

import core_app.model.Customer;
import core_app.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public Customer authenticate(String username, String password) {
        String sql = "SELECT * FROM Customer WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            System.out.println("[DAO AUTH] Looking up user: '" + username + "'");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String role = rs.getString("role");
                    System.out.println("[DAO AUTH] Found user! Role=" + role);
                    System.out.println("[DAO AUTH] Stored password starts with: '"
                            + (storedHash != null ? storedHash.substring(0, Math.min(10, storedHash.length())) : "NULL")
                            + "...'");
                    System.out.println("[DAO AUTH] Input password: '" + password + "'");

                    if (storedHash != null && (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$"))) {
                        System.out.println("[DAO AUTH] Mode: BCrypt comparison");
                        if (BCrypt.checkpw(password, storedHash)) {
                            System.out.println("[DAO AUTH] BCrypt match SUCCESS!");
                            return mapResultSetToCustomer(rs);
                        } else {
                            System.out.println("[DAO AUTH] BCrypt match FAILED!");
                        }
                    } else {
                        System.out.println("[DAO AUTH] Mode: Plain text comparison");
                        if (password.equals(storedHash)) {
                            System.out.println("[DAO AUTH] Plain text match SUCCESS!");
                            return mapResultSetToCustomer(rs);
                        } else {
                            System.out.println("[DAO AUTH] Plain text match FAILED! stored='" + storedHash
                                    + "' vs input='" + password + "'");
                        }
                    }
                } else {
                    System.out.println("[DAO AUTH] User '" + username + "' NOT FOUND in database!");
                }
            }
        } catch (SQLException e) {
            System.out.println("[DAO AUTH] SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean add(Customer c) {
        String sql = "INSERT INTO Customer (full_name, phone, address, email, username, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPhoneNumber());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getUsername());

            // Băm mặt khẩu trước khi lưu (Siêu VIP)
            String hashedPassword = BCrypt.hashpw(c.getPassword(), BCrypt.gensalt(12));
            ps.setString(6, hashedPassword);

            ps.setString(7, c.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM Customer WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRole(int id, String newRole) {
        String sql = "UPDATE Customer SET role = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"));
    }
}
