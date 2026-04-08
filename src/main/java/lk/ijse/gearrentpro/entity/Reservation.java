package lk.ijse.gearrentpro.entity;

import java.sql.Date;

public class Reservation {
    private Integer reservationId;
    private String customerId;
    private String equipmentId;
    private String branchId;
    private Date startDate;
    private Date endDate;
    private String status; // PENDING, COMPLETED, CANCELLED

    public Reservation() {
    }

    public Reservation(Integer reservationId, String customerId, String equipmentId, String branchId, Date startDate, Date endDate, String status) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.branchId = branchId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
