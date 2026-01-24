package core_app.model;

public class Customer {

    private int customerId;
    private String fullName;
    private String phoneNumber;
    private String address;

    public Customer() {
    }

    public Customer(int customerId, String fullName,
                    String phoneNumber, String address) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
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
}
