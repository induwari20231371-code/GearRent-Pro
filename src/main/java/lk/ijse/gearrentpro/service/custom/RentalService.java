package lk.ijse.gearrentpro.service.custom;

import lk.ijse.gearrentpro.entity.Rental;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing equipment rentals.
 * Business Rules:
 * - Maximum rental duration is 30 days
 * - Equipment with overlapping rentals cannot be rented
 * - Customer's total security deposits cannot exceed configurable limit (LKR 500,000)
 * - Pricing includes category factor, weekend multiplier
 * - Discounts: Long-term rental (>=7 days), Membership (Silver/Gold)
 */
public interface RentalService {
    
    /**
     * Place a new rental after validating all business rules:
     * - Equipment availability for the period
     * - No overlapping reservations or rentals
     * - Duration <= 30 days
     * - Customer's deposit limit not exceeded
     * 
     * @param rental The rental to place
     * @return Success/error message
     */
    String placeRental(Rental rental) throws SQLException, ClassNotFoundException, RuntimeException;
    
    /**
     * Process equipment return
     * @param rentalId The rental ID
     * @param actualReturnDate The actual return date
     * @return The updated rental with late fees if applicable
     */
    Rental processReturn(Integer rentalId, Date actualReturnDate) throws SQLException, ClassNotFoundException;
    
    /**
     * Get all rentals
     */
    List<Rental> getAllRentals() throws SQLException, ClassNotFoundException;
    
    /**
     * Get rentals by customer
     */
    List<Rental> getRentalsByCustomer(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Get active rentals for a customer
     */
    List<Rental> getActiveRentalsByCustomer(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Get rentals by branch
     */
    List<Rental> getRentalsByBranch(String branchId) throws SQLException, ClassNotFoundException;
    
    /**
     * Search rental by ID
     */
    Rental getRental(Integer rentalId) throws SQLException, ClassNotFoundException;
    
    /**
     * Generate next rental ID - not needed with AUTO_INCREMENT
     */
    Integer generateNextId() throws SQLException, ClassNotFoundException;
    
    /**
     * Check if customer can rent (deposit limit check)
     * @param customerId Customer ID
     * @param newDepositAmount New deposit amount to add
     * @return true if within limit, false otherwise
     */
    boolean canCustomerRent(String customerId, double newDepositAmount) throws SQLException, ClassNotFoundException;
    
    /**
     * Get total active deposits for a customer
     */
    double getCustomerActiveDeposits(String customerId) throws SQLException, ClassNotFoundException;
    
    /**
     * Cancel a rental
     */
    boolean cancelRental(Integer rentalId) throws SQLException, ClassNotFoundException;
    
    /**
     * Calculate and preview pricing for a potential rental
     */
    PricingService.PricingResult calculatePricing(String equipmentId, String customerId, Date startDate, Date endDate) 
            throws SQLException, ClassNotFoundException;
}
