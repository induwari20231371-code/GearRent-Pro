package lk.ijse.gearrentpro.dao.custom;

import lk.ijse.gearrentpro.dao.CrudDAO;
import lk.ijse.gearrentpro.entity.Category;

import java.sql.SQLException;

public interface CategoryDAO extends CrudDAO<Category, String> {
    /**
     * Get category by equipment ID
     * @param equipmentId the equipment ID
     * @return the Category object
     */
    Category getCategoryByEquipmentId(String equipmentId) throws SQLException, ClassNotFoundException;
}
