package lk.ijse.gearrentpro.dao.custom;

import lk.ijse.gearrentpro.dao.CrudDAO;
import lk.ijse.gearrentpro.entity.Reservation;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface ReservationDAO extends CrudDAO<Reservation, Integer> {
    
    /**
     * Get all reservations for a customer
     */
    List<Reservation> getByCustomerId(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Get all reservations for an equipment
     */
    List<Reservation> getByEquipmentId(String equipmentId) throws SQLException, ClassNotFoundException;
    
    /**
     * Check if there are overlapping reservations for the equipment
     */
    boolean hasOverlappingReservation(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException;
    
    /**
     * Check if there are overlapping reservations excluding a specific reservation
     */
    boolean hasOverlappingReservationExcluding(String equipmentId, Date startDate, Date endDate, Integer excludeReservationId) throws SQLException, ClassNotFoundException;
    
    /**
     * Update reservation status
     */
    boolean updateStatus(Integer reservationId, String status) throws SQLException, ClassNotFoundException;
    
    /**
     * Get pending/confirmed reservations for equipment
     */
    List<Reservation> getActiveReservationsForEquipment(String equipmentId) throws SQLException, ClassNotFoundException;
    
    /**
     * Generate next reservation ID - not needed with AUTO_INCREMENT but kept for compatibility
     */
    Integer generateNextId() throws SQLException, ClassNotFoundException;
}
