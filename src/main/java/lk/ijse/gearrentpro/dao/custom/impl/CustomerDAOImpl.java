package lk.ijse.gearrentpro.dao.custom.impl;

import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.custom.CustomerDAO;
import lk.ijse.gearrentpro.entity.Customer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {
    
    @Override
    public boolean save(Customer entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("INSERT INTO customer (customer_id, name, nic_passport, contact, email, address, membership_level) VALUES (?,?,?,?,?,?,?)",
                entity.getCustomerId(), entity.getName(), entity.getNicPassport(), entity.getContact(), entity.getEmail(), entity.getAddress(), entity.getMembershipLevel());
    }

    @Override
    public boolean update(Customer entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE customer SET name=?, nic_passport=?, contact=?, email=?, address=?, membership_level=? WHERE customer_id=?",
                entity.getName(), entity.getNicPassport(), entity.getContact(), entity.getEmail(), entity.getAddress(), entity.getMembershipLevel(), entity.getCustomerId());
    }

    @Override
    public boolean delete(String id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("DELETE FROM customer WHERE customer_id=?", id);
    }

    @Override
    public Customer search(String id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM customer WHERE customer_id=?", id);
        if (rst.next()) {
            return mapResultSetToCustomer(rst);
        }
        return null;
    }

    @Override
    public List<Customer> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM customer ORDER BY name");
        List<Customer> allCustomers = new ArrayList<>();
        while (rst.next()) {
            allCustomers.add(mapResultSetToCustomer(rst));
        }
        return allCustomers;
    }

    @Override
    public Customer searchByNic(String nic) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM customer WHERE nic_passport=?", nic);
        if (rst.next()) {
            return mapResultSetToCustomer(rst);
        }
        return null;
    }

    @Override
    public List<Customer> getByMembershipLevel(String level) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM customer WHERE membership_level=? ORDER BY name", level);
        List<Customer> customers = new ArrayList<>();
        while (rst.next()) {
            customers.add(mapResultSetToCustomer(rst));
        }
        return customers;
    }

    @Override
    public String generateNextId() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT customer_id FROM customer ORDER BY customer_id DESC LIMIT 1");
        if (rst.next()) {
            String lastId = rst.getString(1);
            int num = Integer.parseInt(lastId.substring(4)) + 1;
            return String.format("CUST%03d", num);
        }
        return "CUST001";
    }

    private Customer mapResultSetToCustomer(ResultSet rst) throws SQLException {
        return new Customer(
            rst.getString("customer_id"),
            rst.getString("name"),
            rst.getString("nic_passport"),
            rst.getString("contact"),
            rst.getString("email"),
            rst.getString("address"),
            rst.getString("membership_level")
        );
    }
}
