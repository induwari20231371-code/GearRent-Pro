package lk.ijse.gearrentpro.dao.custom.impl;

import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.custom.BranchDAO;
import lk.ijse.gearrentpro.entity.Branch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchDAOImpl implements BranchDAO {
    @Override
    public boolean save(Branch entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("INSERT INTO branch (branch_id, name, address, contact) VALUES (?,?,?,?)",
                entity.getBranchId(), entity.getName(), entity.getAddress(), entity.getContact());
    }

    @Override
    public boolean update(Branch entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE branch SET name=?, address=?, contact=? WHERE branch_id=?",
                entity.getName(), entity.getAddress(), entity.getContact(), entity.getBranchId());
    }

    @Override
    public boolean delete(String id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("DELETE FROM branch WHERE branch_id=?", id);
    }

    @Override
    public Branch search(String id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM branch WHERE branch_id=?", id);
        if (rst.next()) {
            return new Branch(rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4));
        }
        return null;
    }

    @Override
    public List<Branch> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM branch");
        List<Branch> allBranches = new ArrayList<>();
        while (rst.next()) {
            allBranches.add(new Branch(rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4)));
        }
        return allBranches;
    }
}
