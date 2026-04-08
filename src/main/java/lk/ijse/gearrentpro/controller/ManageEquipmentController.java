package lk.ijse.gearrentpro.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.custom.BranchDAO;
import lk.ijse.gearrentpro.dao.custom.CategoryDAO;
import lk.ijse.gearrentpro.dao.custom.EquipmentDAO;
import lk.ijse.gearrentpro.entity.Branch;
import lk.ijse.gearrentpro.entity.Category;
import lk.ijse.gearrentpro.entity.Equipment;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageEquipmentController {
    @FXML private TableView<Equipment> tblEquipment;
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colName;
    @FXML private TableColumn<?, ?> colBrand;
    @FXML private TableColumn<?, ?> colCategory;
    @FXML private TableColumn<?, ?> colPrice;
    @FXML private TableColumn<?, ?> colDeposit;
    @FXML private TableColumn<?, ?> colStatus;

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtBrand;
    @FXML private TextField txtModel;
    @FXML private TextField txtYear;
    @FXML private TextField txtDailyPrice;
    @FXML private TextField txtDeposit;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbBranch;

    private final EquipmentDAO equipmentDAO = (EquipmentDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.EQUIPMENT);
    private final CategoryDAO categoryDAO = (CategoryDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CATEGORY);
    private final BranchDAO branchDAO = (BranchDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.BRANCH);
    
    private Map<String, String> categoryMap = new HashMap<>();
    private Map<String, String> branchMap = new HashMap<>();

    public void initialize() {
        cmbStatus.getItems().addAll("AVAILABLE", "RESERVED", "RENTED", "MAINTENANCE");
        cmbStatus.setValue("AVAILABLE");
        loadCategories();
        loadBranches();
        setCellValueFactory();
        loadTableData();
        generateNextEquipmentId();
        
        // Add table selection listener
        tblEquipment.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.getAll();
            categoryMap.clear();
            cmbCategory.getItems().clear();
            for (Category c : categories) {
                String display = c.getName();
                categoryMap.put(display, c.getCategoryId());
                cmbCategory.getItems().add(display);
            }
            if (!cmbCategory.getItems().isEmpty()) {
                cmbCategory.setValue(cmbCategory.getItems().get(0));
            }
        } catch (Exception e) {
            // Fallback to hardcoded values
            cmbCategory.getItems().addAll("Camera", "Lens", "Drone", "Audio", "Lighting");
            categoryMap.put("Camera", "C001");
            categoryMap.put("Lens", "C003");
            categoryMap.put("Drone", "C002");
            categoryMap.put("Audio", "C005");
            categoryMap.put("Lighting", "C004");
        }
    }
    
    private void loadBranches() {
        try {
            List<Branch> branches = branchDAO.getAll();
            branchMap.clear();
            if (cmbBranch != null) {
                cmbBranch.getItems().clear();
                for (Branch b : branches) {
                    String display = b.getName();
                    branchMap.put(display, b.getBranchId());
                    cmbBranch.getItems().add(display);
                }
                if (!cmbBranch.getItems().isEmpty()) {
                    cmbBranch.setValue(cmbBranch.getItems().get(0));
                }
            }
        } catch (Exception e) {
            // Default branch
            branchMap.put("Panadura HQ", "B001");
        }
    }
    
    private void generateNextEquipmentId() {
        try {
            // Generate next ID
            List<Equipment> all = equipmentDAO.getAll();
            int maxNum = 0;
            for (Equipment e : all) {
                String id = e.getEquipmentId();
                if (id.startsWith("E")) {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNum) maxNum = num;
                }
            }
            txtId.setText(String.format("E%03d", maxNum + 1));
        } catch (Exception e) {
            txtId.setText("E001");
        }
    }

    private void setCellValueFactory() {
        colId.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("baseDailyPrice"));
        colDeposit.setCellValueFactory(new PropertyValueFactory<>("securityDeposit"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadTableData() {
        tblEquipment.getItems().clear();
        try {
            List<Equipment> all = equipmentDAO.getAll();
            tblEquipment.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading equipment: " + e.getMessage()).show();
        }
    }
    
    private void populateForm(Equipment equipment) {
        txtId.setText(equipment.getEquipmentId());
        txtName.setText(equipment.getName());
        txtBrand.setText(equipment.getBrand());
        if (txtModel != null) txtModel.setText(equipment.getModel());
        txtYear.setText(String.valueOf(equipment.getPurchaseYear()));
        txtDailyPrice.setText(String.valueOf(equipment.getBaseDailyPrice()));
        txtDeposit.setText(String.valueOf(equipment.getSecurityDeposit()));
        cmbStatus.setValue(equipment.getStatus());
        
        // Find category name
        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            if (entry.getValue().equals(equipment.getCategoryId())) {
                cmbCategory.setValue(entry.getKey());
                break;
            }
        }
        
        // Find branch name
        if (cmbBranch != null) {
            for (Map.Entry<String, String> entry : branchMap.entrySet()) {
                if (entry.getValue().equals(equipment.getBranchId())) {
                    cmbBranch.setValue(entry.getKey());
                    break;
                }
            }
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!validateForm()) return;
        
        try {
            double price = Double.parseDouble(txtDailyPrice.getText());
            double deposit = Double.parseDouble(txtDeposit.getText());
            int year = txtYear.getText().isEmpty() ? java.time.Year.now().getValue() : Integer.parseInt(txtYear.getText());
            
            String categoryId = categoryMap.getOrDefault(cmbCategory.getValue(), "C001");
            String branchId = cmbBranch != null ? branchMap.getOrDefault(cmbBranch.getValue(), "B001") : "B001";
            String model = txtModel != null ? txtModel.getText() : "";
            
            Equipment e = new Equipment(
                txtId.getText(), 
                txtName.getText(), 
                txtBrand.getText(), 
                model,
                year, 
                price, 
                deposit, 
                cmbStatus.getValue(), 
                branchId, 
                categoryId
            );
            
            if (equipmentDAO.save(e)) {
                new Alert(Alert.AlertType.INFORMATION, "Equipment Saved Successfully!").show();
                loadTableData();
                clearFields();
                generateNextEquipmentId();
            }
        } catch (SQLException | ClassNotFoundException ex) {
            new Alert(Alert.AlertType.ERROR, "Save Error: " + ex.getMessage()).show();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid number format. Please check price, deposit, and year fields.").show();
        }
    }
    
    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (!validateForm()) return;
        
        try {
            double price = Double.parseDouble(txtDailyPrice.getText());
            double deposit = Double.parseDouble(txtDeposit.getText());
            int year = txtYear.getText().isEmpty() ? java.time.Year.now().getValue() : Integer.parseInt(txtYear.getText());
            
            String categoryId = categoryMap.getOrDefault(cmbCategory.getValue(), "C001");
            String branchId = cmbBranch != null ? branchMap.getOrDefault(cmbBranch.getValue(), "B001") : "B001";
            String model = txtModel != null ? txtModel.getText() : "";
            
            Equipment e = new Equipment(
                txtId.getText(), 
                txtName.getText(), 
                txtBrand.getText(), 
                model,
                year, 
                price, 
                deposit, 
                cmbStatus.getValue(), 
                branchId, 
                categoryId
            );
            
            if (equipmentDAO.update(e)) {
                new Alert(Alert.AlertType.INFORMATION, "Equipment Updated Successfully!").show();
                loadTableData();
                clearFields();
                generateNextEquipmentId();
            }
        } catch (SQLException | ClassNotFoundException ex) {
            new Alert(Alert.AlertType.ERROR, "Update Error: " + ex.getMessage()).show();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid number format. Please check price, deposit, and year fields.").show();
        }
    }
    
    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Equipment selected = tblEquipment.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select equipment to delete.").show();
            return;
        }
        
        if (!"AVAILABLE".equals(selected.getStatus())) {
            new Alert(Alert.AlertType.WARNING, "Cannot delete equipment that is currently " + selected.getStatus()).show();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete: " + selected.getName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    equipmentDAO.delete(selected.getEquipmentId());
                    new Alert(Alert.AlertType.INFORMATION, "Equipment deleted successfully.").show();
                    loadTableData();
                    clearFields();
                    generateNextEquipmentId();
                } catch (Exception e) {
                    if (e.getMessage().contains("foreign key constraint")) {
                        new Alert(Alert.AlertType.ERROR, "Cannot delete equipment with rental history.").show();
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
        generateNextEquipmentId();
        tblEquipment.getSelectionModel().clearSelection();
    }
    
    private boolean validateForm() {
        if (txtName.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter equipment name.").show();
            return false;
        }
        if (txtBrand.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter brand.").show();
            return false;
        }
        if (txtDailyPrice.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter daily rental price.").show();
            return false;
        }
        if (txtDeposit.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter security deposit amount.").show();
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtId.clear();
        txtName.clear();
        txtBrand.clear();
        if (txtModel != null) txtModel.clear();
        txtDailyPrice.clear();
        txtDeposit.clear();
        txtYear.clear();
        cmbStatus.setValue("AVAILABLE");
    }
}
