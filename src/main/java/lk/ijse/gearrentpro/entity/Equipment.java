package lk.ijse.gearrentpro.entity;

public class Equipment {
    private String equipmentId;
    private String name;
    private String brand;
    private String model;
    private int purchaseYear;
    private double baseDailyPrice;
    private double securityDeposit;
    private String status; // AVAILABLE, RESERVED, RENTED, MAINTENANCE
    private String branchId;
    private String categoryId;

    public Equipment() {
    }

    public Equipment(String equipmentId, String name, String brand, String model, int purchaseYear, double baseDailyPrice, double securityDeposit, String status, String branchId, String categoryId) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.purchaseYear = purchaseYear;
        this.baseDailyPrice = baseDailyPrice;
        this.securityDeposit = securityDeposit;
        this.status = status;
        this.branchId = branchId;
        this.categoryId = categoryId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPurchaseYear() {
        return purchaseYear;
    }

    public void setPurchaseYear(int purchaseYear) {
        this.purchaseYear = purchaseYear;
    }

    public double getBaseDailyPrice() {
        return baseDailyPrice;
    }

    public void setBaseDailyPrice(double baseDailyPrice) {
        this.baseDailyPrice = baseDailyPrice;
    }

    public double getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
