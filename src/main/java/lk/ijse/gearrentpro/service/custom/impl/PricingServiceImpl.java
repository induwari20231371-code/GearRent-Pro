package lk.ijse.gearrentpro.service.custom.impl;

import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.custom.CategoryDAO;
import lk.ijse.gearrentpro.dao.custom.CustomerDAO;
import lk.ijse.gearrentpro.dao.custom.EquipmentDAO;
import lk.ijse.gearrentpro.entity.Category;
import lk.ijse.gearrentpro.entity.Customer;
import lk.ijse.gearrentpro.entity.Equipment;
import lk.ijse.gearrentpro.service.custom.PricingService;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Implementation of PricingService that calculates rental prices
 * according to the business rules specified in the coursework.
 * 
 * Formula: finalDailyPrice = equipmentBasePrice * categoryFactor * weekendMultiplier (if applicable)
 * 
 * Discounts:
 * - Long rental (>= 7 days): Apply configured percentage
 * - Membership (Silver/Gold): Apply membership discount percentage
 */
public class PricingServiceImpl implements PricingService {
    
    private final EquipmentDAO equipmentDAO = (EquipmentDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.EQUIPMENT);
    private final CategoryDAO categoryDAO = (CategoryDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CATEGORY);
    private final CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CUSTOMER);

    @Override
    public PricingResult calculateRentalPrice(String equipmentId, String customerId, Date startDate, Date endDate) 
            throws SQLException, ClassNotFoundException {
        
        Equipment equipment = equipmentDAO.search(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("Equipment not found: " + equipmentId);
        }
        
        Category category = categoryDAO.search(equipment.getCategoryId());
        if (category == null) {
            throw new RuntimeException("Category not found for equipment: " + equipmentId);
        }
        
        Customer customer = customerDAO.search(customerId);
        if (customer == null) {
            throw new RuntimeException("Customer not found: " + customerId);
        }
        
        return calculateRentalPrice(equipment, category, customer, startDate, endDate);
    }

    @Override
    public PricingResult calculateRentalPrice(Equipment equipment, Category category, Customer customer, Date startDate, Date endDate) {
        PricingResult result = new PricingResult();
        
        LocalDate start = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();
        
        // Calculate total days (inclusive)
        int totalDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        
        // Count weekend days
        int weekendDays = countWeekendDays(start, end);
        int weekdayDays = totalDays - weekendDays;
        
        // Base pricing
        double baseDailyPrice = equipment.getBaseDailyPrice();
        double categoryFactor = category.getBasePriceFactor();
        double weekendMultiplier = category.getWeekendMultiplier();
        
        // Calculate costs
        double effectiveDailyPrice = baseDailyPrice * categoryFactor;
        double weekdayCost = weekdayDays * effectiveDailyPrice;
        double weekendCost = weekendDays * effectiveDailyPrice * weekendMultiplier;
        double weekendCharges = weekendDays * effectiveDailyPrice * (weekendMultiplier - 1); // Extra cost for weekends
        double baseRentalCost = weekdayDays * effectiveDailyPrice;
        double totalBeforeDiscount = weekdayCost + weekendCost;
        
        // Long rental discount
        double longRentalDiscountPercent = 0;
        double longRentalDiscount = 0;
        try {
            int longRentalDays = Integer.parseInt(getConfigValue("LONG_RENTAL_DAYS"));
            if (totalDays >= longRentalDays) {
                longRentalDiscountPercent = Double.parseDouble(getConfigValue("LONG_RENTAL_DISCOUNT_PERCENT"));
                longRentalDiscount = totalBeforeDiscount * (longRentalDiscountPercent / 100);
            }
        } catch (Exception e) {
            // Use defaults if config not found
            if (totalDays >= 7) {
                longRentalDiscountPercent = 10;
                longRentalDiscount = totalBeforeDiscount * 0.10;
            }
        }
        
        // Membership discount (applied after long rental discount)
        double membershipDiscountPercent = 0;
        double membershipDiscount = 0;
        try {
            membershipDiscountPercent = getMembershipDiscountPercent(customer.getMembershipLevel());
            double afterLongRentalDiscount = totalBeforeDiscount - longRentalDiscount;
            membershipDiscount = afterLongRentalDiscount * (membershipDiscountPercent / 100);
        } catch (Exception e) {
            // Default membership discounts
            if ("GOLD".equals(customer.getMembershipLevel())) {
                membershipDiscountPercent = 10;
            } else if ("SILVER".equals(customer.getMembershipLevel())) {
                membershipDiscountPercent = 5;
            }
            double afterLongRentalDiscount = totalBeforeDiscount - longRentalDiscount;
            membershipDiscount = afterLongRentalDiscount * (membershipDiscountPercent / 100);
        }
        
        // Final payable
        double finalPayable = totalBeforeDiscount - longRentalDiscount - membershipDiscount;
        
        // Populate result
        result.setBaseDailyPrice(baseDailyPrice);
        result.setCategoryFactor(categoryFactor);
        result.setTotalDays(totalDays);
        result.setWeekendDays(weekendDays);
        result.setWeekendMultiplier(weekendMultiplier);
        result.setBaseRentalCost(baseRentalCost);
        result.setWeekendCharges(weekendCharges);
        result.setTotalBeforeDiscount(totalBeforeDiscount);
        result.setLongRentalDiscountPercent(longRentalDiscountPercent);
        result.setLongRentalDiscount(longRentalDiscount);
        result.setMembershipDiscountPercent(membershipDiscountPercent);
        result.setMembershipDiscount(membershipDiscount);
        result.setFinalPayable(finalPayable);
        result.setSecurityDeposit(equipment.getSecurityDeposit());
        
        return result;
    }

    @Override
    public double getMembershipDiscountPercent(String membershipLevel) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT discount_percentage FROM membership_config WHERE level_name=?", membershipLevel);
        if (rst.next()) {
            return rst.getDouble(1);
        }
        return 0.0;
    }

    @Override
    public String getConfigValue(String key) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT config_value FROM system_config WHERE config_key=?", key);
        if (rst.next()) {
            return rst.getString(1);
        }
        return null;
    }
    
    /**
     * Count the number of weekend days (Saturday and Sunday) in a date range
     */
    private int countWeekendDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
