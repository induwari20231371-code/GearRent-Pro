package lk.ijse.gearrentpro.service.custom.impl;

import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.custom.*;
import lk.ijse.gearrentpro.entity.*;
import lk.ijse.gearrentpro.service.custom.PricingService;
import lk.ijse.gearrentpro.service.custom.RentalService;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation of RentalService.
 * Handles all rental-related business logic including:
 * - Creating rentals with full validation
 * - Processing returns with late fees
 * - Deposit limit management
 */
public class RentalServiceImpl implements RentalService {

    private final RentalDAO rentalDAO = (RentalDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RENTAL);
    private final EquipmentDAO equipmentDAO = (EquipmentDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.EQUIPMENT);
    private final CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CUSTOMER);
    private final CategoryDAO categoryDAO = (CategoryDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CATEGORY);
    private final ReservationDAO reservationDAO = (ReservationDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RESERVATION);
    
    private final PricingService pricingService = new PricingServiceImpl();
    
    private static final double MAX_DEPOSIT_LIMIT = 500000.00;
    private static final int MAX_RENTAL_DAYS = 30;

    @Override
    public String placeRental(Rental rental) throws SQLException, ClassNotFoundException, RuntimeException {
        
        // Validate customer exists
        Customer customer = customerDAO.search(rental.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("Customer not found: " + rental.getCustomerId());
        }
        
        // Validate equipment exists
        Equipment equipment = equipmentDAO.search(rental.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("Equipment not found: " + rental.getEquipmentId());
        }
        
        // Check equipment is not under maintenance
        if ("MAINTENANCE".equals(equipment.getStatus())) {
            throw new RuntimeException("Equipment is currently under maintenance and cannot be rented.");
        }
        
        // Validate dates
        if (rental.getStartDate().after(rental.getEndDate())) {
            throw new RuntimeException("Start date must be before or equal to end date.");
        }
        
        // Rule 1: Max Rental Duration Check (30 days)
        long days = ChronoUnit.DAYS.between(
            rental.getStartDate().toLocalDate(), 
            rental.getEndDate().toLocalDate()) + 1;
        
        if (days > MAX_RENTAL_DAYS) {
            throw new RuntimeException("Rental duration exceeds maximum allowed limit of " + MAX_RENTAL_DAYS + " days. Requested: " + days + " days.");
        }

        // Rule 2: Check for overlapping reservations and rentals
        if (!equipmentDAO.isAvailableForPeriod(rental.getEquipmentId(), rental.getStartDate(), rental.getEndDate())) {
            throw new RuntimeException("Equipment is not available for the selected period. There is an overlapping reservation or rental.");
        }
        
        // Rule 3: Security Deposit Limit Check
        double currentTotalDeposit = rentalDAO.getTotalActiveDepositsForCustomer(rental.getCustomerId());
        double newDeposit = equipment.getSecurityDeposit();
        
        if ((currentTotalDeposit + newDeposit) > MAX_DEPOSIT_LIMIT) {
            throw new RuntimeException(
                String.format("Transaction Blocked: Customer's total security deposits would exceed limit.\n" +
                    "Current Active Deposits: LKR %.2f\n" +
                    "New Deposit Required: LKR %.2f\n" +
                    "Maximum Allowed: LKR %.2f\n" +
                    "Please return some equipment first.",
                    currentTotalDeposit, newDeposit, MAX_DEPOSIT_LIMIT));
        }

        // Calculate pricing
        Category category = categoryDAO.search(equipment.getCategoryId());
        PricingService.PricingResult pricing = pricingService.calculateRentalPrice(
            equipment, category, customer, rental.getStartDate(), rental.getEndDate());
        
        // Set calculated values
        rental.setBaseRentalCost(pricing.getBaseRentalCost());
        rental.setSecurityDepositHeld(pricing.getSecurityDeposit());
        rental.setWeekendCharges(pricing.getWeekendCharges());
        rental.setTotalBeforeDiscount(pricing.getTotalBeforeDiscount());
        rental.setLongRentalDiscount(pricing.getLongRentalDiscount());
        rental.setMembershipDiscount(pricing.getMembershipDiscount());
        rental.setFinalPayableAmount(pricing.getFinalPayable());
        
        // Set default statuses if not provided
        if (rental.getPaymentStatus() == null) {
            rental.setPaymentStatus("UNPAID");
        }
        if (rental.getRentalStatus() == null) {
            rental.setRentalStatus("ACTIVE");
        }
        
        // Set branch from equipment if not provided
        if (rental.getBranchId() == null || rental.getBranchId().isEmpty()) {
            rental.setBranchId(equipment.getBranchId());
        }

        // Save the rental - ID will be auto-generated
        if (rentalDAO.save(rental)) {
            // Update equipment status to RENTED
            equipmentDAO.updateStatus(rental.getEquipmentId(), "RENTED");
            
            return String.format("Rental Placed Successfully!\n" +
                "Duration: %d days\n" +
                "Total: LKR %.2f\n" +
                "Deposit Held: LKR %.2f\n" +
                "Discounts Applied: LKR %.2f",
                days, pricing.getFinalPayable(), 
                pricing.getSecurityDeposit(), pricing.getTotalDiscount());
        } else {
            return "Failed to save rental record.";
        }
    }

    @Override
    public Rental processReturn(Integer rentalId, Date actualReturnDate) throws SQLException, ClassNotFoundException {
        Rental rental = rentalDAO.search(rentalId);
        if (rental == null) {
            throw new RuntimeException("Rental not found: " + rentalId);
        }
        
        if ("RETURNED".equals(rental.getRentalStatus()) || "CANCELLED".equals(rental.getRentalStatus())) {
            throw new RuntimeException("Rental already returned or cancelled.");
        }
        
        // Update rental with return info
        rental.setActualReturnDate(actualReturnDate);
        rental.setRentalStatus("RETURNED");
        
        // Calculate late fees if applicable
        LocalDate expectedReturn = rental.getEndDate().toLocalDate();
        LocalDate actualReturn = actualReturnDate.toLocalDate();
        
        if (actualReturn.isAfter(expectedReturn)) {
            long lateDays = ChronoUnit.DAYS.between(expectedReturn, actualReturn);
            // Get late fee from category
            Equipment equipment = equipmentDAO.search(rental.getEquipmentId());
            Category category = categoryDAO.search(equipment.getCategoryId());
            double lateFee = lateDays * category.getLateFeePerDay();
            // Late fee would be stored in rental_return_info table
        }
        
        // Update rental
        rentalDAO.update(rental);
        
        // Update equipment status back to AVAILABLE
        equipmentDAO.updateStatus(rental.getEquipmentId(), "AVAILABLE");
        
        return rental;
    }

    @Override
    public List<Rental> getAllRentals() throws SQLException, ClassNotFoundException {
        return rentalDAO.getAll();
    }

    @Override
    public List<Rental> getRentalsByCustomer(String customerId) throws SQLException, ClassNotFoundException {
        return rentalDAO.getRentalsByCustomerId(customerId);
    }

    @Override
    public List<Rental> getActiveRentalsByCustomer(String customerId) throws SQLException, ClassNotFoundException {
        return rentalDAO.getActiveRentalsByCustomerId(customerId);
    }

    @Override
    public List<Rental> getRentalsByBranch(String branchId) throws SQLException, ClassNotFoundException {
        return rentalDAO.getByBranch(branchId);
    }

    @Override
    public Rental getRental(Integer rentalId) throws SQLException, ClassNotFoundException {
        return rentalDAO.search(rentalId);
    }

    @Override
    public Integer generateNextId() throws SQLException, ClassNotFoundException {
        return rentalDAO.generateNextId();
    }

    @Override
    public boolean canCustomerRent(String customerId, double newDepositAmount) throws SQLException, ClassNotFoundException {
        double currentDeposits = getCustomerActiveDeposits(customerId);
        return (currentDeposits + newDepositAmount) <= MAX_DEPOSIT_LIMIT;
    }

    @Override
    public double getCustomerActiveDeposits(String customerId) throws SQLException, ClassNotFoundException {
        return rentalDAO.getTotalActiveDepositsForCustomer(customerId);
    }

    @Override
    public boolean cancelRental(Integer rentalId) throws SQLException, ClassNotFoundException {
        Rental rental = rentalDAO.search(rentalId);
        if (rental == null) {
            throw new RuntimeException("Rental not found: " + rentalId);
        }
        
        boolean updated = rentalDAO.updateStatus(rentalId, "CANCELLED");
        
        if (updated) {
            // Set equipment back to available
            equipmentDAO.updateStatus(rental.getEquipmentId(), "AVAILABLE");
        }
        
        return updated;
    }

    @Override
    public PricingService.PricingResult calculatePricing(String equipmentId, String customerId, Date startDate, Date endDate) 
            throws SQLException, ClassNotFoundException {
        return pricingService.calculateRentalPrice(equipmentId, customerId, startDate, endDate);
    }
}
