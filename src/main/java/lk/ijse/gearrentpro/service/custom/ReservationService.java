package lk.ijse.gearrentpro.service.custom;

import lk.ijse.gearrentpro.entity.Reservation;
import lk.ijse.gearrentpro.entity.Rental;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing equipment reservations.
 * Business Rules:
 * - A reservation blocks the equipment for the specified period
 * - Equipment with overlapping reservation or rental periods cannot be reserved
 * - Maximum rental duration is 30 days
 */
public interface ReservationService {
    
    /**
     * Create a new reservation after validating:
     * - Equipment availability for the period
     * - No overlapping reservations or rentals
     * - Duration <= 30 days
     */
    String createReservation(Reservation reservation) throws SQLException, ClassNotFoundException, RuntimeException;
    
    /**
     * Cancel an existing reservation
     */
    boolean cancelReservation(Integer reservationId) throws SQLException, ClassNotFoundException;
    
    /**
     * Convert a reservation to a rental
     * - Re-validates equipment availability
     * - Creates the rental record
     * - Updates reservation status to CONVERTED
     */
    Rental convertToRental(Integer reservationId, String branchId) throws SQLException, ClassNotFoundException, RuntimeException;
    
    /**
     * Check if equipment is available for the given period
     */
    boolean isEquipmentAvailable(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException;
    
    /**
     * Get all reservations
     */
    List<Reservation> getAllReservations() throws SQLException, ClassNotFoundException;
    
    /**
     * Get reservations by customer
     */
    List<Reservation> getReservationsByCustomer(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Get pending/confirmed reservations for an equipment
     */
    List<Reservation> getActiveReservationsForEquipment(String equipmentId) throws SQLException, ClassNotFoundException;
    
    /**
     * Search reservation by ID
     */
    Reservation getReservation(Integer reservationId) throws SQLException, ClassNotFoundException;
    
    /**
     * Generate next reservation ID - not needed with AUTO_INCREMENT
     */
    Integer generateNextId() throws SQLException, ClassNotFoundException;
}
