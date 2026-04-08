package lk.ijse.gearrentpro.dao.custom;

import lk.ijse.gearrentpro.dao.CrudDAO;
import lk.ijse.gearrentpro.entity.Rental;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface RentalDAO extends CrudDAO<Rental, Integer> {
    
    /**
     * Get all rentals for a customer
     */
    List<Rental> getRentalsByCustomerId(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Get active rentals for a customer (ACTIVE or OVERDUE status)
     */
    List<Rental> getActiveRentalsByCustomerId(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Calculate total security deposits for customer's active rentals
     */
    double getTotalActiveDepositsForCustomer(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Check if there are overlapping rentals for the equipment
     */
    boolean hasOverlappingRental(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException;
    
    /**
     * Update rental status
     */
    boolean updateStatus(Integer rentalId, String status) throws SQLException, ClassNotFoundException;
    
    /**
     * Update actual return date
     */
    boolean updateReturnDate(Integer rentalId, Date returnDate) throws SQLException, ClassNotFoundException;
    
    /**
     * Get rentals by branch
     */
    List<Rental> getByBranch(String branchId) throws SQLException, ClassNotFoundException;
    
    /**
     * Generate next rental ID - not needed with AUTO_INCREMENT but kept for compatibility
     */
    Integer generateNextId() throws SQLException, ClassNotFoundException;
}
