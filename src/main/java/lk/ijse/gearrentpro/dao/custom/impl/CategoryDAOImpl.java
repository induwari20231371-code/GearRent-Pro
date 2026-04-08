package lk.ijse.gearrentpro.dao.custom.impl;

import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.custom.CategoryDAO;
import lk.ijse.gearrentpro.entity.Category;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {
    
    @Override
    public boolean save(Category entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("INSERT INTO category (category_id, name, base_price_factor, weekend_multiplier, late_fee_per_day, is_active) VALUES (?,?,?,?,?,?)",
                entity.getCategoryId(), entity.getName(), entity.getBasePriceFactor(), 
                entity.getWeekendMultiplier(), entity.getLateFeePerDay(), entity.isActive() ? 1 : 0);
    }

    @Override
    public boolean update(Category entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE category SET name=?, base_price_factor=?, weekend_multiplier=?, late_fee_per_day=?, is_active=? WHERE category_id=?",
                entity.getName(), entity.getBasePriceFactor(), entity.getWeekendMultiplier(), 
                entity.getLateFeePerDay(), entity.isActive() ? 1 : 0, entity.getCategoryId());
    }

    @Override
    public boolean delete(String id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("DELETE FROM category WHERE category_id=?", id);
    }

    @Override
    public Category search(String id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM category WHERE category_id=?", id);
        if (rst.next()) {
            return new Category(
                rst.getString("category_id"),
                rst.getString("name"),
                rst.getDouble("base_price_factor"),
                rst.getDouble("weekend_multiplier"),
                rst.getDouble("late_fee_per_day"),
                rst.getInt("is_active") == 1
            );
        }
        return null;
    }

    @Override
    public List<Category> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM category WHERE is_active = 1");
        List<Category> categories = new ArrayList<>();
        while (rst.next()) {
            categories.add(new Category(
                rst.getString("category_id"),
                rst.getString("name"),
                rst.getDouble("base_price_factor"),
                rst.getDouble("weekend_multiplier"),
                rst.getDouble("late_fee_per_day"),
                rst.getInt("is_active") == 1
            ));
        }
        return categories;
    }

    @Override
    public Category getCategoryByEquipmentId(String equipmentId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute(
            "SELECT c.* FROM category c " +
            "INNER JOIN equipment e ON c.category_id = e.category_id " +
            "WHERE e.equipment_id = ?", equipmentId);
        if (rst.next()) {
            return new Category(
                rst.getString("category_id"),
                rst.getString("name"),
                rst.getDouble("base_price_factor"),
                rst.getDouble("weekend_multiplier"),
                rst.getDouble("late_fee_per_day"),
                rst.getInt("is_active") == 1
            );
        }
        return null;
    }
}
