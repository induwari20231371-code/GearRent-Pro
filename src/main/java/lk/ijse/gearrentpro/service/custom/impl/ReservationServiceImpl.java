package lk.ijse.gearrentpro.service.custom.impl;

import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.custom.*;
import lk.ijse.gearrentpro.entity.*;
import lk.ijse.gearrentpro.service.custom.PricingService;
import lk.ijse.gearrentpro.service.custom.ReservationService;

import java.sql.Date;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation of ReservationService.
 * Handles all reservation-related business logic including:
 * - Creating reservations with availability validation
 * - Converting reservations to rentals
 * - Managing reservation lifecycle
 */
public class ReservationServiceImpl implements ReservationService {
    
    private final ReservationDAO reservationDAO = (ReservationDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RESERVATION);
    private final EquipmentDAO equipmentDAO = (EquipmentDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.EQUIPMENT);
    private final RentalDAO rentalDAO = (RentalDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RENTAL);
    private final CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CUSTOMER);
    private final CategoryDAO categoryDAO = (CategoryDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CATEGORY);
    
    private final PricingService pricingService = new PricingServiceImpl();
    
    private static final int MAX_RENTAL_DAYS = 30;
    private static final double MAX_DEPOSIT_LIMIT = 500000.00;

    @Override
    public String createReservation(Reservation reservation) throws SQLException, ClassNotFoundException, RuntimeException {
        // Validate customer exists
        Customer customer = customerDAO.search(reservation.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("Customer not found: " + reservation.getCustomerId());
        }
        
        // Validate equipment exists
        Equipment equipment = equipmentDAO.search(reservation.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("Equipment not found: " + reservation.getEquipmentId());
        }
        
        // Check equipment is not under maintenance
        if ("MAINTENANCE".equals(equipment.getStatus())) {
            throw new RuntimeException("Equipment is currently under maintenance and cannot be reserved.");
        }
        
        // Validate dates
        if (reservation.getStartDate().after(reservation.getEndDate())) {
            throw new RuntimeException("Start date must be before or equal to end date.");
        }
        
        // Check maximum rental duration (30 days)
        long days = ChronoUnit.DAYS.between(
            reservation.getStartDate().toLocalDate(), 
            reservation.getEndDate().toLocalDate()) + 1;
        
        if (days > MAX_RENTAL_DAYS) {
            throw new RuntimeException("Reservation duration exceeds maximum allowed limit of " + MAX_RENTAL_DAYS + " days. Requested: " + days + " days.");
        }
        
        // Check for overlapping reservations and rentals
        if (!isEquipmentAvailable(reservation.getEquipmentId(), reservation.getStartDate(), reservation.getEndDate())) {
            throw new RuntimeException("Equipment is not available for the selected period. There is an overlapping reservation or rental.");
        }
        
        // Check customer's deposit limit
        double currentDeposits = rentalDAO.getTotalActiveDepositsForCustomer(reservation.getCustomerId());
        double newDeposit = equipment.getSecurityDeposit();
        if ((currentDeposits + newDeposit) > MAX_DEPOSIT_LIMIT) {
            throw new RuntimeException(
                String.format("Cannot create reservation. Customer's total security deposits would exceed limit.\n" +
                    "Current deposits: LKR %.2f\nNew deposit: LKR %.2f\nLimit: LKR %.2f",
                    currentDeposits, newDeposit, MAX_DEPOSIT_LIMIT));
        }
        
        // Set default status
        if (reservation.getStatus() == null) {
            reservation.setStatus("PENDING");
        }
        
        // Set branch from equipment if not provided
        if (reservation.getBranchId() == null || reservation.getBranchId().isEmpty()) {
            reservation.setBranchId(equipment.getBranchId());
        }
        
        // Save reservation - ID will be auto-generated
        if (reservationDAO.save(reservation)) {
            // Update equipment status to RESERVED
            equipmentDAO.updateStatus(reservation.getEquipmentId(), "RESERVED");
            return "Reservation created successfully!";
        } else {
            throw new RuntimeException("Failed to save reservation.");
        }
    }

    @Override
    public boolean cancelReservation(Integer reservationId) throws SQLException, ClassNotFoundException {
        Reservation reservation = reservationDAO.search(reservationId);
        if (reservation == null) {
            throw new RuntimeException("Reservation not found: " + reservationId);
        }
        
        // Update status to cancelled
        boolean updated = reservationDAO.updateStatus(reservationId, "CANCELLED");
        
        if (updated) {
            // Check if equipment has other active reservations
            List<Reservation> activeReservations = reservationDAO.getActiveReservationsForEquipment(reservation.getEquipmentId());
            if (activeReservations.isEmpty()) {
                // No other reservations, set equipment back to available
                equipmentDAO.updateStatus(reservation.getEquipmentId(), "AVAILABLE");
            }
        }
        
        return updated;
    }

    @Override
    public Rental convertToRental(Integer reservationId, String branchId) throws SQLException, ClassNotFoundException, RuntimeException {
        Reservation reservation = reservationDAO.search(reservationId);
        if (reservation == null) {
            throw new RuntimeException("Reservation not found: " + reservationId);
        }
        
        if ("CONVERTED".equals(reservation.getStatus()) || "CANCELLED".equals(reservation.getStatus())) {
            throw new RuntimeException("Reservation cannot be converted. Status: " + reservation.getStatus());
        }
        
        // Re-validate equipment availability (excluding this reservation)
        if (!equipmentDAO.isAvailableForPeriodExcludingReservation(
                reservation.getEquipmentId(), 
                reservation.getStartDate(), 
                reservation.getEndDate(), 
                reservationId)) {
            throw new RuntimeException("Equipment is no longer available for this period. Another rental may have been placed.");
        }
        
        // Get equipment and customer for pricing
        Equipment equipment = equipmentDAO.search(reservation.getEquipmentId());
        Customer customer = customerDAO.search(reservation.getCustomerId());
        Category category = categoryDAO.search(equipment.getCategoryId());
        
        // Check deposit limit again
        double currentDeposits = rentalDAO.getTotalActiveDepositsForCustomer(reservation.getCustomerId());
        if ((currentDeposits + equipment.getSecurityDeposit()) > MAX_DEPOSIT_LIMIT) {
            throw new RuntimeException(
                String.format("Cannot convert to rental. Customer's total security deposits would exceed limit of LKR %.2f",
                    MAX_DEPOSIT_LIMIT));
        }
        
        // Calculate pricing
        PricingService.PricingResult pricing = pricingService.calculateRentalPrice(
            equipment, category, customer, reservation.getStartDate(), reservation.getEndDate());
        
        // Create rental
        Rental rental = new Rental();
        rental.setEquipmentId(reservation.getEquipmentId());
        rental.setCustomerId(reservation.getCustomerId());
        rental.setBranchId(branchId != null ? branchId : reservation.getBranchId());
        rental.setReservationId(reservationId);
        rental.setStartDate(reservation.getStartDate());
        rental.setEndDate(reservation.getEndDate());
        rental.setBaseRentalCost(pricing.getBaseRentalCost());
        rental.setSecurityDepositHeld(pricing.getSecurityDeposit());
        rental.setWeekendCharges(pricing.getWeekendCharges());
        rental.setTotalBeforeDiscount(pricing.getTotalBeforeDiscount());
        rental.setLongRentalDiscount(pricing.getLongRentalDiscount());
        rental.setMembershipDiscount(pricing.getMembershipDiscount());
        rental.setFinalPayableAmount(pricing.getFinalPayable());
        rental.setPaymentStatus("UNPAID");
        rental.setRentalStatus("ACTIVE");
        
        // Save rental
        if (!rentalDAO.save(rental)) {
            throw new RuntimeException("Failed to create rental from reservation.");
        }
        
        // Update reservation status
        reservationDAO.updateStatus(reservationId, "CONVERTED");
        
        // Update equipment status
        equipmentDAO.updateStatus(reservation.getEquipmentId(), "RENTED");
        
        return rental;
    }

    @Override
    public boolean isEquipmentAvailable(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException {
        return equipmentDAO.isAvailableForPeriod(equipmentId, startDate, endDate);
    }

    @Override
    public List<Reservation> getAllReservations() throws SQLException, ClassNotFoundException {
        return reservationDAO.getAll();
    }

    @Override
    public List<Reservation> getReservationsByCustomer(String customerId) throws SQLException, ClassNotFoundException {
        return reservationDAO.getByCustomerId(customerId);
    }

    @Override
    public List<Reservation> getActiveReservationsForEquipment(String equipmentId) throws SQLException, ClassNotFoundException {
        return reservationDAO.getActiveReservationsForEquipment(equipmentId);
    }

    @Override
    public Reservation getReservation(Integer reservationId) throws SQLException, ClassNotFoundException {
        return reservationDAO.search(reservationId);
    }

    @Override
    public Integer generateNextId() throws SQLException, ClassNotFoundException {
        return reservationDAO.generateNextId();
    }
}
