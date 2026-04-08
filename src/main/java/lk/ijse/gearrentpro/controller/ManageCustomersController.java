package lk.ijse.gearrentpro.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.custom.CustomerDAO;
import lk.ijse.gearrentpro.entity.Customer;

import java.sql.SQLException;
import java.util.List;

public class ManageCustomersController {
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colName;
    @FXML private TableColumn<?, ?> colNIC;
    @FXML private TableColumn<?, ?> colContact;
    @FXML private TableColumn<?, ?> colType;
    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtNIC;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAddress;
    @FXML private ComboBox<String> cmbMembership;

    private final CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CUSTOMER);

    public void initialize() {
        cmbMembership.getItems().addAll("REGULAR", "SILVER", "GOLD");
        cmbMembership.setValue("REGULAR");
        setCellValueFactory();
        loadTableData();
        generateNextCustomerId();
        
        // Add table selection listener
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });
    }

    private void setCellValueFactory() {
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colNIC.setCellValueFactory(new PropertyValueFactory<>("nicPassport"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colType.setCellValueFactory(new PropertyValueFactory<>("membershipLevel"));
    }

    private void loadTableData() {
        tblCustomers.getItems().clear();
        try {
            List<Customer> all = customerDAO.getAll();
            tblCustomers.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading customers: " + e.getMessage()).show();
        }
    }
    
    private void generateNextCustomerId() {
        try {
            String nextId = customerDAO.generateNextId();
            txtId.setText(nextId);
        } catch (Exception e) {
            txtId.setText("CUST001");
        }
    }
    
    private void populateForm(Customer customer) {
        txtId.setText(customer.getCustomerId());
        txtName.setText(customer.getName());
        txtNIC.setText(customer.getNicPassport());
        txtContact.setText(customer.getContact());
        if (txtEmail != null) txtEmail.setText(customer.getEmail());
        if (txtAddress != null) txtAddress.setText(customer.getAddress());
        cmbMembership.setValue(customer.getMembershipLevel());
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!validateForm()) return;
        
        try {
            Customer c = new Customer(
                txtId.getText(), 
                txtName.getText(), 
                txtNIC.getText(), 
                txtContact.getText(), 
                txtEmail != null ? txtEmail.getText() : "", 
                txtAddress != null ? txtAddress.getText() : "", 
                cmbMembership.getValue()
            );
            
            if (customerDAO.save(c)) {
                new Alert(Alert.AlertType.INFORMATION, "Customer Saved Successfully!").show();
                loadTableData();
                clearFields();
                generateNextCustomerId();
            }
        } catch (SQLException | ClassNotFoundException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                new Alert(Alert.AlertType.ERROR, "A customer with this NIC/Passport already exists.").show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Save Error: " + e.getMessage()).show();
            }
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (!validateForm()) return;
        
        try {
            Customer c = new Customer(
                txtId.getText(), 
                txtName.getText(), 
                txtNIC.getText(), 
                txtContact.getText(), 
                txtEmail != null ? txtEmail.getText() : "", 
                txtAddress != null ? txtAddress.getText() : "", 
                cmbMembership.getValue()
            );
            
            if (customerDAO.update(c)) {
                new Alert(Alert.AlertType.INFORMATION, "Customer Updated Successfully!").show();
                loadTableData();
                clearFields();
                generateNextCustomerId();
            }
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Update Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a customer to delete.").show();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete customer: " + selected.getName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    customerDAO.delete(selected.getCustomerId());
                    new Alert(Alert.AlertType.INFORMATION, "Customer deleted successfully.").show();
                    loadTableData();
                    clearFields();
                    generateNextCustomerId();
                } catch (Exception e) {
                    if (e.getMessage().contains("foreign key constraint")) {
                        new Alert(Alert.AlertType.ERROR, "Cannot delete customer with active rentals or reservations.").show();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Delete Error: " + e.getMessage()).show();
                    }
                }
            }
        });
    }
    
    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
        generateNextCustomerId();
        tblCustomers.getSelectionModel().clearSelection();
    }
    
    private boolean validateForm() {
        if (txtName.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter customer name.").show();
            return false;
        }
        if (txtNIC.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter NIC/Passport number.").show();
            return false;
        }
        if (txtContact.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter contact number.").show();
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtId.clear();
        txtName.clear();
        txtNIC.clear();
        txtContact.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtAddress != null) txtAddress.clear();
        cmbMembership.setValue("REGULAR");
    }
}
