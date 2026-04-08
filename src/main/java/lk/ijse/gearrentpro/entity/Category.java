package lk.ijse.gearrentpro.entity;

public class Category {
    private String categoryId;
    private String name;
    private double basePriceFactor;
    private double weekendMultiplier;
    private double lateFeePerDay;
    private boolean isActive;

    public Category() {
    }

    public Category(String categoryId, String name, double basePriceFactor, double weekendMultiplier, double lateFeePerDay, boolean isActive) {
        this.categoryId = categoryId;
        this.name = name;
        this.basePriceFactor = basePriceFactor;
        this.weekendMultiplier = weekendMultiplier;
        this.lateFeePerDay = lateFeePerDay;
        this.isActive = isActive;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBasePriceFactor() {
        return basePriceFactor;
    }

    public void setBasePriceFactor(double basePriceFactor) {
        this.basePriceFactor = basePriceFactor;
    }

    public double getWeekendMultiplier() {
        return weekendMultiplier;
    }

    public void setWeekendMultiplier(double weekendMultiplier) {
        this.weekendMultiplier = weekendMultiplier;
    }

    public double getLateFeePerDay() {
        return lateFeePerDay;
    }

    public void setLateFeePerDay(double lateFeePerDay) {
        this.lateFeePerDay = lateFeePerDay;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
