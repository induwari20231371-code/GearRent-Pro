package lk.ijse.gearrentpro.dao.custom;

import lk.ijse.gearrentpro.dao.CrudDAO;
import lk.ijse.gearrentpro.entity.Customer;

import java.sql.SQLException;
import java.util.List;

public interface CustomerDAO extends CrudDAO<Customer, String> {
    
    /**
     * Search customer by NIC/Passport
     */
    Customer searchByNic(String nic) throws SQLException, ClassNotFoundException;
    
    /**
     * Get customers by membership level
     */
    List<Customer> getByMembershipLevel(String level) throws SQLException, ClassNotFoundException;
    
    /**
     * Generate next customer ID
     */
    String generateNextId() throws SQLException, ClassNotFoundException;
}
