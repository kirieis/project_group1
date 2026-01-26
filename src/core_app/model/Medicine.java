package core_app.model;

public class Medicine {

    private int medicineId;
    private String medicineCode;
    private String medicineName;
    private String activeIngredient;
    private String registrationNumber; // Số đăng ký Bộ Y Tế
    private MedicineGroup group;        // DRUG, SUPPLEMENT, MEDICAL_DEVICE


    public Medicine(int medicineId, String medicineCode, String medicineName,
            String activeIngredient, String registrationNumber,
            MedicineGroup group) {
        this.medicineId = medicineId;
        this.medicineCode = medicineCode;
        this.medicineName = medicineName;
        this.activeIngredient = activeIngredient;
        this.registrationNumber = registrationNumber;
        this.group = group;
    }

    // ===== Getter & Setter =====
    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getActiveIngredient() {
        return activeIngredient;
    }

    public void setActiveIngredient(String activeIngredient) {
        this.activeIngredient = activeIngredient;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public MedicineGroup getGroup() {
        return group;
    }

    public void setGroup(MedicineGroup group) {
        this.group = group;
    }
}
