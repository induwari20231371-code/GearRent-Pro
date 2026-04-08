package lk.ijse.gearrentpro.entity;

import java.sql.Date;

public class Rental {
    private Integer rentalId;
    private String equipmentId;
    private String customerId;
    private String branchId;
    private Integer reservationId; // Link to reservation if converted
    
    private Date startDate;
    private Date endDate;
    private Date actualReturnDate;
    
    // Pricing breakdown
    private double baseRentalCost;
    private double securityDepositHeld;
    private double weekendCharges;
    private double totalBeforeDiscount;
    
    // Discounts
    private double longRentalDiscount;
    private double membershipDiscount;
    private double finalPayableAmount;
    
    // Status
    private String paymentStatus; // PAID, PARTIALLY_PAID, UNPAID
    private String rentalStatus; // ACTIVE, RETURNED, OVERDUE, CANCELLED

    public Rental() {
    }

    public Rental(Integer rentalId, String equipmentId, String customerId, String branchId, Integer reservationId,
                  Date startDate, Date endDate, Date actualReturnDate,
                  double baseRentalCost, double securityDepositHeld, double weekendCharges, double totalBeforeDiscount,
                  double longRentalDiscount, double membershipDiscount, double finalPayableAmount,
                  String paymentStatus, String rentalStatus) {
        this.rentalId = rentalId;
        this.equipmentId = equipmentId;
        this.customerId = customerId;
        this.branchId = branchId;
        this.reservationId = reservationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.actualReturnDate = actualReturnDate;
        this.baseRentalCost = baseRentalCost;
        this.securityDepositHeld = securityDepositHeld;
        this.weekendCharges = weekendCharges;
        this.totalBeforeDiscount = totalBeforeDiscount;
        this.longRentalDiscount = longRentalDiscount;
        this.membershipDiscount = membershipDiscount;
        this.finalPayableAmount = finalPayableAmount;
        this.paymentStatus = paymentStatus;
        this.rentalStatus = rentalStatus;
    }

    // Convenience constructor without reservationId
    public Rental(Integer rentalId, String equipmentId, String customerId, String branchId,
                  Date startDate, Date endDate, Date actualReturnDate,
                  double baseRentalCost, double securityDepositHeld, double weekendCharges, double totalBeforeDiscount,
                  double longRentalDiscount, double membershipDiscount, double finalPayableAmount,
                  String paymentStatus, String rentalStatus) {
        this(rentalId, equipmentId, customerId, branchId, null, startDate, endDate, actualReturnDate,
             baseRentalCost, securityDepositHeld, weekendCharges, totalBeforeDiscount,
             longRentalDiscount, membershipDiscount, finalPayableAmount, paymentStatus, rentalStatus);
    }

    // Getters and Setters
    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(Date actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public double getBaseRentalCost() {
        return baseRentalCost;
    }

    public void setBaseRentalCost(double baseRentalCost) {
        this.baseRentalCost = baseRentalCost;
    }

    public double getSecurityDepositHeld() {
        return securityDepositHeld;
    }

    public void setSecurityDepositHeld(double securityDepositHeld) {
        this.securityDepositHeld = securityDepositHeld;
    }

    public double getWeekendCharges() {
        return weekendCharges;
    }

    public void setWeekendCharges(double weekendCharges) {
        this.weekendCharges = weekendCharges;
    }

    public double getTotalBeforeDiscount() {
        return totalBeforeDiscount;
    }

    public void setTotalBeforeDiscount(double totalBeforeDiscount) {
        this.totalBeforeDiscount = totalBeforeDiscount;
    }

    public double getLongRentalDiscount() {
        return longRentalDiscount;
    }

    public void setLongRentalDiscount(double longRentalDiscount) {
        this.longRentalDiscount = longRentalDiscount;
    }

    public double getMembershipDiscount() {
        return membershipDiscount;
    }

    public void setMembershipDiscount(double membershipDiscount) {
        this.membershipDiscount = membershipDiscount;
    }

    public double getFinalPayableAmount() {
        return finalPayableAmount;
    }

    public void setFinalPayableAmount(double finalPayableAmount) {
        this.finalPayableAmount = finalPayableAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
    }
}
