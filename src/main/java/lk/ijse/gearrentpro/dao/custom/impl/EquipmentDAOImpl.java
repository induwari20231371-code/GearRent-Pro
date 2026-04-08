package lk.ijse.gearrentpro.dao.custom.impl;

import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.custom.EquipmentDAO;
import lk.ijse.gearrentpro.entity.Equipment;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAOImpl implements EquipmentDAO {
    
    @Override
    public boolean save(Equipment entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("INSERT INTO equipment (equipment_id, name, brand, model, purchase_year, base_daily_price, security_deposit, status, branch_id, category_id) VALUES (?,?,?,?,?,?,?,?,?,?)",
                entity.getEquipmentId(), entity.getName(), entity.getBrand(), entity.getModel(), 
                entity.getPurchaseYear(), entity.getBaseDailyPrice(), entity.getSecurityDeposit(), 
                entity.getStatus(), entity.getBranchId(), entity.getCategoryId());
    }

    @Override
    public boolean update(Equipment entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE equipment SET name=?, brand=?, model=?, purchase_year=?, base_daily_price=?, security_deposit=?, status=?, branch_id=?, category_id=? WHERE equipment_id=?",
                entity.getName(), entity.getBrand(), entity.getModel(), entity.getPurchaseYear(), 
                entity.getBaseDailyPrice(), entity.getSecurityDeposit(), entity.getStatus(), 
                entity.getBranchId(), entity.getCategoryId(), entity.getEquipmentId());
    }

    @Override
    public boolean delete(String id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("DELETE FROM equipment WHERE equipment_id=?", id);
    }

    @Override
    public Equipment search(String id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM equipment WHERE equipment_id=?", id);
        if (rst.next()) {
            return mapResultSetToEquipment(rst);
        }
        return null;
    }

    @Override
    public List<Equipment> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM equipment");
        List<Equipment> allItems = new ArrayList<>();
        while (rst.next()) {
            allItems.add(mapResultSetToEquipment(rst));
        }
        return allItems;
    }

    @Override
    public List<Equipment> getByBranch(String branchId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM equipment WHERE branch_id=?", branchId);
        List<Equipment> items = new ArrayList<>();
        while (rst.next()) {
            items.add(mapResultSetToEquipment(rst));
        }
        return items;
    }

    @Override
    public List<Equipment> getAvailable() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM equipment WHERE status='AVAILABLE'");
        List<Equipment> items = new ArrayList<>();
        while (rst.next()) {
            items.add(mapResultSetToEquipment(rst));
        }
        return items;
    }

    @Override
    public List<Equipment> getAvailableByBranch(String branchId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM equipment WHERE status='AVAILABLE' AND branch_id=?", branchId);
        List<Equipment> items = new ArrayList<>();
        while (rst.next()) {
            items.add(mapResultSetToEquipment(rst));
        }
        return items;
    }

    @Override
    public boolean updateStatus(String equipmentId, String status) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE equipment SET status=? WHERE equipment_id=?", status, equipmentId);
    }

    @Override
    public boolean isAvailableForPeriod(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException {
        // Check for overlapping rentals (ACTIVE or OVERDUE status)
        ResultSet rentalCheck = SQLUtil.execute(
            "SELECT COUNT(*) FROM rental WHERE equipment_id=? AND rental_status IN ('ACTIVE', 'OVERDUE') " +
            "AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))",
            equipmentId, endDate, startDate, startDate, startDate, startDate, endDate);
        
        if (rentalCheck.next() && rentalCheck.getInt(1) > 0) {
            return false;
        }
        
        // Check for overlapping reservations (PENDING or CONFIRMED status)
        ResultSet reservationCheck = SQLUtil.execute(
            "SELECT COUNT(*) FROM reservation WHERE equipment_id=? AND status IN ('PENDING', 'CONFIRMED') " +
            "AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))",
            equipmentId, endDate, startDate, startDate, startDate, startDate, endDate);
        
        if (reservationCheck.next() && reservationCheck.getInt(1) > 0) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean isAvailableForPeriodExcludingReservation(String equipmentId, Date startDate, Date endDate, Integer excludeReservationId) throws SQLException, ClassNotFoundException {
        // Check for overlapping rentals (ACTIVE or OVERDUE status)
        ResultSet rentalCheck = SQLUtil.execute(
            "SELECT COUNT(*) FROM rental WHERE equipment_id=? AND rental_status IN ('ACTIVE', 'OVERDUE') " +
            "AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))",
            equipmentId, endDate, startDate, startDate, startDate, startDate, endDate);
        
        if (rentalCheck.next() && rentalCheck.getInt(1) > 0) {
            return false;
        }
        
        // Check for overlapping reservations excluding the specified one
        ResultSet reservationCheck = SQLUtil.execute(
            "SELECT COUNT(*) FROM reservation WHERE equipment_id=? AND status IN ('PENDING', 'CONFIRMED') " +
            "AND reservation_id != ? " +
            "AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))",
            equipmentId, excludeReservationId, endDate, startDate, startDate, startDate, startDate, endDate);
        
        if (reservationCheck.next() && reservationCheck.getInt(1) > 0) {
            return false;
        }
        
        return true;
    }

    private Equipment mapResultSetToEquipment(ResultSet rst) throws SQLException {
        return new Equipment(
            rst.getString("equipment_id"),
            rst.getString("name"),
            rst.getString("brand"),
            rst.getString("model"),
            rst.getInt("purchase_year"),
            rst.getDouble("base_daily_price"),
            rst.getDouble("security_deposit"),
            rst.getString("status"),
            rst.getString("branch_id"),
            rst.getString("category_id")
        );
    }
}
