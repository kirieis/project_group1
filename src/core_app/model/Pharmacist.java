package core_app.model;
public class Pharmacist {

    private int pharmacistId;
    private String fullName;
    private String licenseNumber;
    private int branchId;
    private String role;

    public Pharmacist() {
    }

    public Pharmacist(int pharmacistId, String fullName, String licenseNumber,
                      int branchId, String role) {
        this.pharmacistId = pharmacistId;
        this.fullName = fullName;
        this.licenseNumber = licenseNumber;
        this.branchId = branchId;
        this.role = role;
    }

    // Getters & Setters
    public int getPharmacistId() {
        return pharmacistId;
    }

    public void setPharmacistId(int pharmacistId) {
        this.pharmacistId = pharmacistId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
