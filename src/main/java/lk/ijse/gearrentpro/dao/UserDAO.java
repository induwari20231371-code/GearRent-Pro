package lk.ijse.gearrentpro.dao;

import lk.ijse.gearrentpro.entity.User;
import java.sql.SQLException;

public interface UserDAO extends CrudDAO<User, Integer> {
    User findByUsername(String username) throws SQLException, ClassNotFoundException;
}
