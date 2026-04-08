package lk.ijse.gearrentpro.entity;

import java.sql.Date;

public class RentalReturnInfo {
    private int returnId;
    private int rentalId;
    private Date returnDate;
    private double lateFee;
    private double damageCharge;
    private String damageDescription;
    private double totalCharges;
    private String finalSettlementStatus;

    public RentalReturnInfo() {
    }

    public RentalReturnInfo(int returnId, int rentalId, Date returnDate, double lateFee, double damageCharge, String damageDescription, double totalCharges, String finalSettlementStatus) {
        this.returnId = returnId;
        this.rentalId = rentalId;
        this.returnDate = returnDate;
        this.lateFee = lateFee;
        this.damageCharge = damageCharge;
        this.damageDescription = damageDescription;
        this.totalCharges = totalCharges;
        this.finalSettlementStatus = finalSettlementStatus;
    }

    public int getReturnId() {
        return returnId;
    }

    public void setReturnId(int returnId) {
        this.returnId = returnId;
    }

    public int getRentalId() {
        return rentalId;
    }

    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public double getLateFee() {
        return lateFee;
    }

    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public double getDamageCharge() {
        return damageCharge;
    }

    public void setDamageCharge(double damageCharge) {
        this.damageCharge = damageCharge;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public String getFinalSettlementStatus() {
        return finalSettlementStatus;
    }

    public void setFinalSettlementStatus(String finalSettlementStatus) {
        this.finalSettlementStatus = finalSettlementStatus;
    }
}
