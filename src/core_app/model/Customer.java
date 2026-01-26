package core_app.model;

public class Customer {

    private int customerId;
    private String fullName;
    private String phoneNumber;
    private String address;

    private String username;
    private String password;
    private String role; // "ADMIN" or "CUSTOMER"

    public Customer() {
    }

    public Customer(int customerId, String fullName,
                    String phoneNumber, String address, String username, String password, String role) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ===== Getter & Setter =====

    public int getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
