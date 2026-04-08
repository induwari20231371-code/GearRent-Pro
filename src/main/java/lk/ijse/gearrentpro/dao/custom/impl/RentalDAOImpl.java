package lk.ijse.gearrentpro.dao.custom.impl;

import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.custom.RentalDAO;
import lk.ijse.gearrentpro.entity.Rental;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RentalDAOImpl implements RentalDAO {

    @Override
    public boolean save(Rental entity) throws SQLException, ClassNotFoundException {
        // Don't include rental_id - it's AUTO_INCREMENT
        return SQLUtil.execute(
            "INSERT INTO rental (equipment_id, customer_id, branch_id, reservation_id, " +
            "start_date, end_date, actual_return_date, base_rental_cost, security_deposit_held, " +
            "weekend_charges, long_rental_discount, membership_discount, total_before_discount, " +
            "final_payable_amount, payment_status, rental_status) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            entity.getEquipmentId(), entity.getCustomerId(), entity.getBranchId(),
            entity.getReservationId(), entity.getStartDate(), entity.getEndDate(), entity.getActualReturnDate(),
            entity.getBaseRentalCost(), entity.getSecurityDepositHeld(), entity.getWeekendCharges(),
            entity.getLongRentalDiscount(), entity.getMembershipDiscount(), entity.getTotalBeforeDiscount(),
            entity.getFinalPayableAmount(), entity.getPaymentStatus(), entity.getRentalStatus());
    }

    @Override
    public boolean update(Rental entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute(
            "UPDATE rental SET equipment_id=?, customer_id=?, branch_id=?, reservation_id=?, " +
            "start_date=?, end_date=?, actual_return_date=?, base_rental_cost=?, security_deposit_held=?, " +
            "weekend_charges=?, long_rental_discount=?, membership_discount=?, total_before_discount=?, " +
            "final_payable_amount=?, payment_status=?, rental_status=? WHERE rental_id=?",
            entity.getEquipmentId(), entity.getCustomerId(), entity.getBranchId(), entity.getReservationId(),
            entity.getStartDate(), entity.getEndDate(), entity.getActualReturnDate(),
            entity.getBaseRentalCost(), entity.getSecurityDepositHeld(), entity.getWeekendCharges(),
            entity.getLongRentalDiscount(), entity.getMembershipDiscount(), entity.getTotalBeforeDiscount(),
            entity.getFinalPayableAmount(), entity.getPaymentStatus(), entity.getRentalStatus(),
            entity.getRentalId());
    }

    @Override
    public boolean delete(Integer id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("DELETE FROM rental WHERE rental_id=?", id);
    }

    @Override
    public Rental search(Integer id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM rental WHERE rental_id=?", id);
        if (rst.next()) {
            return mapResultSetToRental(rst);
        }
        return null;
    }

    @Override
    public List<Rental> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM rental ORDER BY start_date DESC");
        List<Rental> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToRental(rst));
        }
        return list;
    }

    @Override
    public List<Rental> getRentalsByCustomerId(String customerId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM rental WHERE customer_id=? ORDER BY start_date DESC", customerId);
        List<Rental> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToRental(rst));
        }
        return list;
    }

    @Override
    public List<Rental> getActiveRentalsByCustomerId(String customerId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute(
            "SELECT * FROM rental WHERE customer_id=? AND rental_status IN ('ACTIVE', 'OVERDUE') ORDER BY start_date DESC",
            customerId);
        List<Rental> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToRental(rst));
        }
        return list;
    }

    @Override
    public double getTotalActiveDepositsForCustomer(String customerId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute(
            "SELECT COALESCE(SUM(security_deposit_held), 0) FROM rental WHERE customer_id=? AND rental_status IN ('ACTIVE', 'OVERDUE')",
            customerId);
        if (rst.next()) {
            return rst.getDouble(1);
        }
        return 0.0;
    }

    @Override
    public boolean hasOverlappingRental(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute(
            "SELECT COUNT(*) FROM rental WHERE equipment_id=? AND rental_status IN ('ACTIVE', 'OVERDUE') " +
            "AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))",
            equipmentId, endDate, startDate, startDate, startDate, startDate, endDate);
        
        if (rst.next()) {
            return rst.getInt(1) > 0;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Integer rentalId, String status) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE rental SET rental_status=? WHERE rental_id=?", status, rentalId);
    }

    @Override
    public boolean updateReturnDate(Integer rentalId, Date returnDate) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE rental SET actual_return_date=?, rental_status='RETURNED' WHERE rental_id=?", 
            returnDate, rentalId);
    }

    @Override
    public List<Rental> getByBranch(String branchId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM rental WHERE branch_id=? ORDER BY start_date DESC", branchId);
        List<Rental> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToRental(rst));
        }
        return list;
    }

    @Override
    public Integer generateNextId() throws SQLException, ClassNotFoundException {
        // Not needed with AUTO_INCREMENT, but return the next expected ID
        ResultSet rst = SQLUtil.execute("SELECT MAX(rental_id) FROM rental");
        if (rst.next()) {
            return rst.getInt(1) + 1;
        }
        return 1;
    }

    private Rental mapResultSetToRental(ResultSet rst) throws SQLException {
        Integer reservationId = rst.getInt("reservation_id");
        if (rst.wasNull()) {
            reservationId = null;
        }
        return new Rental(
            rst.getInt("rental_id"),
            rst.getString("equipment_id"),
            rst.getString("customer_id"),
            rst.getString("branch_id"),
            reservationId,
            rst.getDate("start_date"),
            rst.getDate("end_date"),
            rst.getDate("actual_return_date"),
            rst.getDouble("base_rental_cost"),
            rst.getDouble("security_deposit_held"),
            rst.getDouble("weekend_charges"),
            rst.getDouble("total_before_discount"),
            rst.getDouble("long_rental_discount"),
            rst.getDouble("membership_discount"),
            rst.getDouble("final_payable_amount"),
            rst.getString("payment_status"),
            rst.getString("rental_status")
        );
    }
}
