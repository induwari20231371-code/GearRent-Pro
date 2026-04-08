package lk.ijse.gearrentpro.dao.impl;

import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.UserDAO;
import lk.ijse.gearrentpro.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean save(User entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute(
            "INSERT INTO user (username, password, role, branch_id) VALUES (?, ?, ?, ?)",
            entity.getUsername(), entity.getPassword(), entity.getRole(), entity.getBranchId()
        );
    }

    @Override
    public boolean update(User entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute(
            "UPDATE user SET username=?, password=?, role=?, branch_id=? WHERE user_id=?",
            entity.getUsername(), entity.getPassword(), entity.getRole(), entity.getBranchId(), entity.getUserId()
        );
    }

    @Override
    public boolean delete(Integer id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("DELETE FROM user WHERE user_id=?", id);
    }

    @Override
    public User search(Integer id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM user WHERE user_id=?", id);
        if (rst.next()) {
            return mapResultSetToUser(rst);
        }
        return null;
    }

    @Override
    public List<User> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM user ORDER BY user_id");
        List<User> users = new ArrayList<>();
        while (rst.next()) {
            users.add(mapResultSetToUser(rst));
        }
        return users;
    }

    @Override
    public User findByUsername(String username) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM user WHERE username = ?", username);
        if (rst.next()) {
            return mapResultSetToUser(rst);
        }
        return null;
    }
    
    public List<User> getByRole(String role) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM user WHERE role=?", role);
        List<User> users = new ArrayList<>();
        while (rst.next()) {
            users.add(mapResultSetToUser(rst));
        }
        return users;
    }
    
    public List<User> getByBranch(String branchId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM user WHERE branch_id=?", branchId);
        List<User> users = new ArrayList<>();
        while (rst.next()) {
            users.add(mapResultSetToUser(rst));
        }
        return users;
    }
    
    public boolean existsByUsername(String username) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT COUNT(*) FROM user WHERE username=?", username);
        if (rst.next()) {
            return rst.getInt(1) > 0;
        }
        return false;
    }
    
    private User mapResultSetToUser(ResultSet rst) throws SQLException {
        return new User(
            rst.getInt("user_id"),
            rst.getString("username"),
            rst.getString("password"),
            rst.getString("role"),
            rst.getString("branch_id")
        );
    }
}
