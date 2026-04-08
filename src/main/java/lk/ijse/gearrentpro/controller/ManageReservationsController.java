package lk.ijse.gearrentpro.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.custom.CustomerDAO;
import lk.ijse.gearrentpro.dao.custom.EquipmentDAO;
import lk.ijse.gearrentpro.dao.custom.ReservationDAO;
import lk.ijse.gearrentpro.entity.Customer;
import lk.ijse.gearrentpro.entity.Equipment;
import lk.ijse.gearrentpro.entity.Rental;
import lk.ijse.gearrentpro.entity.Reservation;
import lk.ijse.gearrentpro.service.custom.ReservationService;
import lk.ijse.gearrentpro.service.custom.impl.ReservationServiceImpl;

import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageReservationsController {

    @FXML private TableView<Reservation> tblReservations;
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colCustomer;
    @FXML private TableColumn<?, ?> colEquipment;
    @FXML private TableColumn<?, ?> colDates;
    @FXML private TableColumn<?, ?> colStatus;

    @FXML private ComboBox<String> cmbCustomer;
    @FXML private ComboBox<String> cmbEquipment;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;

    private final ReservationDAO reservationDAO = (ReservationDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RESERVATION);
    private final CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CUSTOMER);
    private final EquipmentDAO equipmentDAO = (EquipmentDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.EQUIPMENT);
    private final ReservationService reservationService = new ReservationServiceImpl();
    
    // Maps to store ID references
    private Map<String, String> customerMap = new HashMap<>();
    private Map<String, String> equipmentMap = new HashMap<>();

    public void initialize() {
        setCellValueFactory();
        loadTableData();
        loadComboBoxes();
        
        // Add table selection listener
        tblReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });
    }

    private void setCellValueFactory() {
        colId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colEquipment.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        colDates.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadTableData() {
        tblReservations.getItems().clear();
        try {
            List<Reservation> all = reservationDAO.getAll();
            tblReservations.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading reservations: " + e.getMessage()).show();
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
                String display = e.getEquipmentId() + " - " + e.getBrand() + " " + e.getModel();
                equipmentMap.put(display, e.getEquipmentId());
                cmbEquipment.getItems().add(display);
            }
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading data: " + e.getMessage()).show();
        }
    }
    
    private void populateForm(Reservation reservation) {
        // Find and select customer in combo
        for (String display : customerMap.keySet()) {
            if (customerMap.get(display).equals(reservation.getCustomerId())) {
                cmbCustomer.setValue(display);
                break;
            }
        }
        
        // Find and select equipment in combo
        for (String display : equipmentMap.keySet()) {
            if (equipmentMap.get(display).equals(reservation.getEquipmentId())) {
                cmbEquipment.setValue(display);
                break;
            }
        }
        
        dpStart.setValue(reservation.getStartDate().toLocalDate());
        dpEnd.setValue(reservation.getEndDate().toLocalDate());
    }

    @FXML
    void btnReserveOnAction(ActionEvent event) {
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
            
            Reservation reservation = new Reservation();
            reservation.setCustomerId(customerId);
            reservation.setEquipmentId(equipmentId);
            reservation.setStartDate(startDate);
            reservation.setEndDate(endDate);
            reservation.setStatus("PENDING");
            
            String result = reservationService.createReservation(reservation);
            new Alert(Alert.AlertType.INFORMATION, result).show();
            
            loadTableData();
            loadComboBoxes();
            clearForm();
            
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "System Error: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }
    
    @FXML
    void btnCancelOnAction(ActionEvent event) {
        Reservation selected = tblReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a reservation to cancel.").show();
            return;
        }
        
        if ("CANCELLED".equals(selected.getStatus()) || "CONVERTED".equals(selected.getStatus())) {
            new Alert(Alert.AlertType.WARNING, "This reservation cannot be cancelled.").show();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel this reservation?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationService.cancelReservation(selected.getReservationId());
                    new Alert(Alert.AlertType.INFORMATION, "Reservation cancelled successfully.").show();
                    loadTableData();
                    loadComboBoxes();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Error cancelling reservation: " + e.getMessage()).show();
                }
            }
        });
    }
    
    @FXML
    void btnConvertToRentalOnAction(ActionEvent event) {
        Reservation selected = tblReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a reservation to convert.").show();
            return;
        }
        
        if (!"PENDING".equals(selected.getStatus()) && !"CONFIRMED".equals(selected.getStatus())) {
            new Alert(Alert.AlertType.WARNING, "Only pending or confirmed reservations can be converted to rental.").show();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Convert this reservation to an active rental?\n\n" +
            "Equipment: " + selected.getEquipmentId() + "\n" +
            "Period: " + selected.getStartDate() + " to " + selected.getEndDate());
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Rental rental = reservationService.convertToRental(selected.getReservationId(), selected.getBranchId());
                    new Alert(Alert.AlertType.INFORMATION, 
                        "Reservation converted to rental successfully!\n\n" +
                        "Rental ID: " + rental.getRentalId() + "\n" +
                        "Amount: LKR " + String.format("%.2f", rental.getFinalPayableAmount()) + "\n" +
                        "Deposit: LKR " + String.format("%.2f", rental.getSecurityDepositHeld())).show();
                    loadTableData();
                    loadComboBoxes();
                } catch (RuntimeException e) {
                    new Alert(Alert.AlertType.WARNING, e.getMessage()).show();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Error converting reservation: " + e.getMessage()).show();
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void clearForm() {
        cmbCustomer.getSelectionModel().clearSelection();
        cmbEquipment.getSelectionModel().clearSelection();
        dpStart.setValue(null);
        dpEnd.setValue(null);
    }
}
