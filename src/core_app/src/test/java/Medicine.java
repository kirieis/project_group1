
public class Medicine {
    private String id;
    private String name;
    private String unit; // VIEN
    private int vienPerVi;
    private int viPerHop;
    private int hopPerThung;

    public Medicine(String id, String name, int vienPerVi, int viPerHop, int hopPerThung) {
        this.id = id;
        this.name = name;
        this.unit = "VIEN";
        this.vienPerVi = vienPerVi;
        this.viPerHop = viPerHop;
        this.hopPerThung = hopPerThung;
    }

    public String getId() {
        return id;
    }

    public int thungToVien(int thung) {
        return thung * hopPerThung * viPerHop * vienPerVi;
    }
}
