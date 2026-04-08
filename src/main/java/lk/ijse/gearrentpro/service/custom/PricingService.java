package lk.ijse.gearrentpro.service.custom;

import lk.ijse.gearrentpro.entity.Category;
import lk.ijse.gearrentpro.entity.Customer;
import lk.ijse.gearrentpro.entity.Equipment;

import java.sql.Date;
import java.sql.SQLException;

/**
 * Service for calculating rental prices based on business rules:
 * - Equipment base daily price
 * - Category factor
 * - Weekend multiplier
 * - Long rental discount (>= 7 days)
 * - Membership discount (Silver/Gold)
 */
public interface PricingService {
    
    /**
     * Calculate the complete pricing breakdown for a rental
     */
    PricingResult calculateRentalPrice(String equipmentId, String customerId, Date startDate, Date endDate) 
            throws SQLException, ClassNotFoundException;
    
    /**
     * Calculate pricing using entity objects directly
     */
    PricingResult calculateRentalPrice(Equipment equipment, Category category, Customer customer, Date startDate, Date endDate);
    
    /**
     * Get membership discount percentage for a customer level
     */
    double getMembershipDiscountPercent(String membershipLevel) throws SQLException, ClassNotFoundException;
    
    /**
     * Get system configuration value
     */
    String getConfigValue(String key) throws SQLException, ClassNotFoundException;
    
    /**
     * Data class to hold pricing breakdown
     */
    class PricingResult {
        private double baseDailyPrice;
        private double categoryFactor;
        private int totalDays;
        private int weekendDays;
        private double weekendMultiplier;
        private double baseRentalCost;
        private double weekendCharges;
        private double totalBeforeDiscount;
        private double longRentalDiscountPercent;
        private double longRentalDiscount;
        private double membershipDiscountPercent;
        private double membershipDiscount;
        private double finalPayable;
        private double securityDeposit;

        // Getters and Setters
        public double getBaseDailyPrice() { return baseDailyPrice; }
        public void setBaseDailyPrice(double baseDailyPrice) { this.baseDailyPrice = baseDailyPrice; }
        
        public double getCategoryFactor() { return categoryFactor; }
        public void setCategoryFactor(double categoryFactor) { this.categoryFactor = categoryFactor; }
        
        public int getTotalDays() { return totalDays; }
        public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
        
        public int getWeekendDays() { return weekendDays; }
        public void setWeekendDays(int weekendDays) { this.weekendDays = weekendDays; }
        
        public double getWeekendMultiplier() { return weekendMultiplier; }
        public void setWeekendMultiplier(double weekendMultiplier) { this.weekendMultiplier = weekendMultiplier; }
        
        public double getBaseRentalCost() { return baseRentalCost; }
        public void setBaseRentalCost(double baseRentalCost) { this.baseRentalCost = baseRentalCost; }
        
        public double getWeekendCharges() { return weekendCharges; }
        public void setWeekendCharges(double weekendCharges) { this.weekendCharges = weekendCharges; }
        
        public double getTotalBeforeDiscount() { return totalBeforeDiscount; }
        public void setTotalBeforeDiscount(double totalBeforeDiscount) { this.totalBeforeDiscount = totalBeforeDiscount; }
        
        public double getLongRentalDiscountPercent() { return longRentalDiscountPercent; }
        public void setLongRentalDiscountPercent(double longRentalDiscountPercent) { this.longRentalDiscountPercent = longRentalDiscountPercent; }
        
        public double getLongRentalDiscount() { return longRentalDiscount; }
        public void setLongRentalDiscount(double longRentalDiscount) { this.longRentalDiscount = longRentalDiscount; }
        
        public double getMembershipDiscountPercent() { return membershipDiscountPercent; }
        public void setMembershipDiscountPercent(double membershipDiscountPercent) { this.membershipDiscountPercent = membershipDiscountPercent; }
        
        public double getMembershipDiscount() { return membershipDiscount; }
        public void setMembershipDiscount(double membershipDiscount) { this.membershipDiscount = membershipDiscount; }
        
        public double getFinalPayable() { return finalPayable; }
        public void setFinalPayable(double finalPayable) { this.finalPayable = finalPayable; }
        
        public double getSecurityDeposit() { return securityDeposit; }
        public void setSecurityDeposit(double securityDeposit) { this.securityDeposit = securityDeposit; }
        
        public double getTotalDiscount() { return longRentalDiscount + membershipDiscount; }
        
        @Override
        public String toString() {
            return String.format(
                "Pricing Breakdown:\n" +
                "Base Daily Price: LKR %.2f x Category Factor: %.2f\n" +
                "Total Days: %d (Weekend Days: %d)\n" +
                "Base Rental Cost: LKR %.2f\n" +
                "Weekend Charges: LKR %.2f\n" +
                "Total Before Discount: LKR %.2f\n" +
                "Long Rental Discount (%.0f%%): - LKR %.2f\n" +
                "Membership Discount (%.0f%%): - LKR %.2f\n" +
                "Final Payable: LKR %.2f\n" +
                "Security Deposit: LKR %.2f",
                baseDailyPrice, categoryFactor, totalDays, weekendDays,
                baseRentalCost, weekendCharges, totalBeforeDiscount,
                longRentalDiscountPercent, longRentalDiscount,
                membershipDiscountPercent, membershipDiscount,
                finalPayable, securityDeposit
            );
        }
    }
}
