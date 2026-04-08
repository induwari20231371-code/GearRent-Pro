package lk.ijse.gearrentpro.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.custom.CustomerDAO;
import lk.ijse.gearrentpro.dao.custom.EquipmentDAO;
import lk.ijse.gearrentpro.dao.custom.RentalDAO;
import lk.ijse.gearrentpro.entity.Customer;
import lk.ijse.gearrentpro.entity.Equipment;
import lk.ijse.gearrentpro.entity.Rental;
import lk.ijse.gearrentpro.service.custom.PricingService;
import lk.ijse.gearrentpro.service.custom.RentalService;
import lk.ijse.gearrentpro.service.custom.impl.RentalServiceImpl;

import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageRentalsController {

    @FXML private ComboBox<String> cmbCustomer;
    @FXML private ComboBox<String> cmbEquipment;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private TextField txtRentalId;
    
    @FXML private Label lblBase;
    @FXML private Label lblDeposit;
    @FXML private Label lblDiscount;
    @FXML private Label lblDiscountDetail;
    @FXML private Label lblTotal;

    @FXML private TableView<Rental> tblRentals;

    @FXML private TableColumn<?, ?> colRentalId;
    @FXML private TableColumn<?, ?> colCustomer;
    @FXML private TableColumn<?, ?> colEquipment;
    @FXML private TableColumn<?, ?> colStartDate;
    @FXML private TableColumn<?, ?> colEndDate;
    @FXML private TableColumn<?, ?> colTotal;
    @FXML private TableColumn<?, ?> colStatus;

    private final RentalDAO rentalDAO = (RentalDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RENTAL);
    private final CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CUSTOMER);
    private final EquipmentDAO equipmentDAO = (EquipmentDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.EQUIPMENT);
    private final RentalService rentalService = new RentalServiceImpl();
    
    // Maps to store ID references
    private Map<String, String> customerMap = new HashMap<>(); // display -> id
    private Map<String, String> equipmentMap = new HashMap<>(); // display -> id
    
    private PricingService.PricingResult currentPricing;

    public void initialize() {
        setCellValueFactory();
        loadTableData();
        loadComboBoxes();
        generateNextRentalId();
        
        // Add listener for date changes to auto-calculate
        dpStart.valueProperty().addListener((obs, old, newVal) -> tryAutoCalculate());
        dpEnd.valueProperty().addListener((obs, old, newVal) -> tryAutoCalculate());
        cmbEquipment.valueProperty().addListener((obs, old, newVal) -> tryAutoCalculate());
        cmbCustomer.valueProperty().addListener((obs, old, newVal) -> tryAutoCalculate());
    }

    private void setCellValueFactory() {
        colRentalId.setCellValueFactory(new PropertyValueFactory<>("rentalId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colEquipment.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("finalPayableAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("rentalStatus"));
    }

    private void loadTableData() {
        tblRentals.getItems().clear();
        try {
            List<Rental> all = rentalDAO.getAll();
            tblRentals.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading rentals: " + e.getMessage()).show();
        }
    }
    
    private void loadComboBoxes() {
        try {
            // Load customers
            List<Customer> customers = customerDAO.getAll();
            customerMap.clear();
            cmbCustomer.getItems().clear();
            for (Customer c : customers) {
                String display = c.getCustomerId() + " - " + c.getName() + " (" + c.getMembershipLevel() + ")";
                customerMap.put(display, c.getCustomerId());
                cmbCustomer.getItems().add(display);
            }
            
            // Load available equipment
            List<Equipment> equipment = equipmentDAO.getAvailable();
            equipmentMap.clear();
            cmbEquipment.getItems().clear();
            for (Equipment e : equipment) {
                String display = e.getEquipmentId() + " - " + e.getBrand() + " " + e.getModel() + " (LKR " + e.getBaseDailyPrice() + "/day)";
                equipmentMap.put(display, e.getEquipmentId());
                cmbEquipment.getItems().add(display);
            }
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading data: " + e.getMessage()).show();
        }
    }
    
    private void generateNextRentalId() {
        // Not needed anymore - using AUTO_INCREMENT
        txtRentalId.setText("(Auto)");
        txtRentalId.setDisable(true);
    }
    
    private void tryAutoCalculate() {
        if (cmbCustomer.getValue() != null && cmbEquipment.getValue() != null && 
            dpStart.getValue() != null && dpEnd.getValue() != null) {
            btnCalculateOnAction(null);
        }
    }

    @FXML
    void btnCalculateOnAction(ActionEvent event) {
        try {
            String customerDisplay = cmbCustomer.getValue();
            String equipmentDisplay = cmbEquipment.getValue();
            
            if (customerDisplay == null || equipmentDisplay == null) {
                new Alert(Alert.AlertType.WARNING, "Please select customer and equipment.").show();
                return;
            }
            
            if (dpStart.getValue() == null || dpEnd.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Please select start and end dates.").show();
                return;
            }
            
            if (dpStart.getValue().isAfter(dpEnd.getValue())) {
                new Alert(Alert.AlertType.WARNING, "Start date must be before or equal to end date.").show();
                return;
            }
            
            String customerId = customerMap.get(customerDisplay);
            String equipmentId = equipmentMap.get(equipmentDisplay);
            Date startDate = Date.valueOf(dpStart.getValue());
            Date endDate = Date.valueOf(dpEnd.getValue());
            
            // Check availability first
            Equipment equipment = equipmentDAO.search(equipmentId);
            if (!equipmentDAO.isAvailableForPeriod(equipmentId, startDate, endDate)) {
                new Alert(Alert.AlertType.WARNING, "Equipment is not available for the selected period.\nThere may be an overlapping reservation or rental.").show();
                clearPricing();
                return;
            }
            
            // Check customer deposit limit
            double currentDeposits = rentalService.getCustomerActiveDeposits(customerId);
            if ((currentDeposits + equipment.getSecurityDeposit()) > 500000) {
                new Alert(Alert.AlertType.WARNING, 
                    String.format("Customer's deposit limit would be exceeded!\n" +
                        "Current deposits: LKR %.2f\n" +
                        "Required deposit: LKR %.2f\n" +
                        "Limit: LKR 500,000.00", 
                        currentDeposits, equipment.getSecurityDeposit())).show();
            }
            
            // Calculate pricing
            currentPricing = rentalService.calculatePricing(equipmentId, customerId, startDate, endDate);
            
            // Update UI
            lblBase.setText(String.format("LKR %.2f", currentPricing.getBaseRentalCost() + currentPricing.getWeekendCharges()));
            lblDeposit.setText(String.format("LKR %.2f", currentPricing.getSecurityDeposit()));
            
            double totalDiscount = currentPricing.getTotalDiscount();
            lblDiscount.setText(String.format("- LKR %.2f", totalDiscount));
            
            String discountDetail = "";
            if (currentPricing.getLongRentalDiscountPercent() > 0) {
                discountDetail += String.format("Long Term %.0f%%", currentPricing.getLongRentalDiscountPercent());
            }
            if (currentPricing.getMembershipDiscountPercent() > 0) {
                if (!discountDetail.isEmpty()) discountDetail += " + ";
                discountDetail += String.format("Membership %.0f%%", currentPricing.getMembershipDiscountPercent());
            }
            if (discountDetail.isEmpty()) {
                discountDetail = "(No discounts)";
            } else {
                discountDetail = "(" + discountDetail + ")";
            }
            lblDiscountDetail.setText(discountDetail);
            
            lblTotal.setText(String.format("LKR %.2f", currentPricing.getFinalPayable()));
            
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error calculating price: " + e.getMessage()).show();
            e.printStackTrace();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }
    
    private void clearPricing() {
        lblBase.setText("LKR 0.00");
        lblDeposit.setText("LKR 0.00");
        lblDiscount.setText("- LKR 0.00");
        lblDiscountDetail.setText("(No discounts)");
        lblTotal.setText("LKR 0.00");
        currentPricing = null;
    }

    @FXML
    void btnPlaceRentalOnAction(ActionEvent event) {
        try {
            String customerDisplay = cmbCustomer.getValue();
            String equipmentDisplay = cmbEquipment.getValue();
            
            if (customerDisplay == null || equipmentDisplay == null) {
                new Alert(Alert.AlertType.WARNING, "Please select customer and equipment.").show();
                return;
            }
            
            if (dpStart.getValue() == null || dpEnd.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Please select start and end dates.").show();
                return;
            }
            
            String customerId = customerMap.get(customerDisplay);
            String equipmentId = equipmentMap.get(equipmentDisplay);
            Date startDate = Date.valueOf(dpStart.getValue());
            Date endDate = Date.valueOf(dpEnd.getValue());
            
            Rental rental = new Rental();
            // Don't set rentalId - it's AUTO_INCREMENT
            rental.setCustomerId(customerId);
            rental.setEquipmentId(equipmentId);
            rental.setStartDate(startDate);
            rental.setEndDate(endDate);
            rental.setPaymentStatus("PAID");
            rental.setRentalStatus("ACTIVE");
            
            String result = rentalService.placeRental(rental);
            new Alert(Alert.AlertType.INFORMATION, result).show();
            
            // Refresh data
            loadTableData();
            loadComboBoxes();
            generateNextRentalId();
            clearForm();
            
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "System Error: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }
    
    @FXML
    void btnReturnOnAction(ActionEvent event) {
        Rental selected = tblRentals.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a rental to process return.").show();
            return;
        }
        
        if (!"ACTIVE".equals(selected.getRentalStatus())) {
            new Alert(Alert.AlertType.WARNING, "Only active rentals can be returned.").show();
            return;
        }
        
        try {
            Date returnDate = Date.valueOf(java.time.LocalDate.now());
            rentalService.processReturn(selected.getRentalId(), returnDate);
            new Alert(Alert.AlertType.INFORMATION, "Equipment returned successfully!").show();
            loadTableData();
            loadComboBoxes();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error processing return: " + e.getMessage()).show();
        }
    }
    
    private void clearForm() {
        cmbCustomer.getSelectionModel().clearSelection();
        cmbEquipment.getSelectionModel().clearSelection();
        dpStart.setValue(null);
        dpEnd.setValue(null);
        clearPricing();
    }
}
