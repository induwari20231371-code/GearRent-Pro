package lk.ijse.gearrentpro.dao.custom;

import lk.ijse.gearrentpro.dao.CrudDAO;
import lk.ijse.gearrentpro.entity.Equipment;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface EquipmentDAO extends CrudDAO<Equipment, String> {
    
    /**
     * Get all equipment by branch
     */
    List<Equipment> getByBranch(String branchId) throws SQLException, ClassNotFoundException;
    
    /**
     * Get available equipment (status = AVAILABLE)
     */
    List<Equipment> getAvailable() throws SQLException, ClassNotFoundException;
    
    /**
     * Get available equipment by branch
     */
    List<Equipment> getAvailableByBranch(String branchId) throws SQLException, ClassNotFoundException;
    
    /**
     * Update equipment status
     */
    boolean updateStatus(String equipmentId, String status) throws SQLException, ClassNotFoundException;
    
    /**
     * Check if equipment is available for the given date range
     * (No overlapping rentals or reservations)
     */
    boolean isAvailableForPeriod(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException;
    
    /**
     * Check if equipment is available for the given date range excluding a specific reservation
     * (Used when converting reservation to rental)
     */
    boolean isAvailableForPeriodExcludingReservation(String equipmentId, Date startDate, Date endDate, Integer excludeReservationId) throws SQLException, ClassNotFoundException;
}
