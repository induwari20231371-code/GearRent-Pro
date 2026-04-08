package lk.ijse.gearrentpro.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.UserDAO;
import lk.ijse.gearrentpro.dao.custom.BranchDAO;
import lk.ijse.gearrentpro.entity.Branch;
import lk.ijse.gearrentpro.entity.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageBranchesController {

    // Branch Tab Fields
    @FXML private TableView<Branch> tblBranches;
    @FXML private TableColumn<?, ?> colName;
    @FXML private TableColumn<?, ?> colAddress;
    @FXML private TextField txtName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtId;
    @FXML private TextField txtContact;
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colContact;
    @FXML private Button btnSaveBranch;
    @FXML private Button btnUpdateBranch;
    @FXML private Button btnDeleteBranch;
    @FXML private Button btnClearBranch;

    // User Tab Fields
    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<?, ?> colUserId;
    @FXML private TableColumn<?, ?> colUsername;
    @FXML private TableColumn<?, ?> colRole;
    @FXML private TableColumn<?, ?> colUserBranch;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private ComboBox<String> cmbUserBranch;
    @FXML private Label lblUserId;

    // DAOs
    private final BranchDAO branchDAO = (BranchDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.BRANCH);
    private final UserDAO userDAO = (UserDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.USER);
    
    private Map<String, String> branchMap = new HashMap<>();
    private User selectedUser = null;

    public void initialize() {
        // Initialize Branch Tab
        setBranchCellValueFactory();
        loadBranchData();
        generateNextBranchId();
        
        // Initialize User Tab
        if (cmbRole != null) {
            cmbRole.getItems().addAll("ADMIN", "BRANCH_MANAGER", "STAFF");
            cmbRole.setValue("STAFF");
        }
        loadBranchComboBox();
        setUserCellValueFactory();
        loadUserData();
        
        // Add table selection listeners
        if (tblBranches != null) {
            tblBranches.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    populateBranchForm(newSel);
                }
            });
        }
        
        if (tblUsers != null) {
            tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    populateUserForm(newSel);
                }
            });
        }
    }

    // ==================== BRANCH METHODS ====================
    
    private void generateNextBranchId() {
        try {
            List<Branch> all = branchDAO.getAll();
            int maxNum = 0;
            for (Branch b : all) {
                String id = b.getBranchId();
                if (id.startsWith("B")) {
                    try {
                        int num = Integer.parseInt(id.substring(1));
                        if (num > maxNum) maxNum = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (txtId != null) txtId.setText(String.format("B%03d", maxNum + 1));
        } catch (Exception e) {
            if (txtId != null) txtId.setText("B001");
        }
    }
    
    private void populateBranchForm(Branch branch) {
        if (txtId != null) txtId.setText(branch.getBranchId());
        if (txtName != null) txtName.setText(branch.getName());
        if (txtAddress != null) txtAddress.setText(branch.getAddress());
        if (txtContact != null) txtContact.setText(branch.getContact());
    }

    private void setBranchCellValueFactory() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        if (colName != null) colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colAddress != null) colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (colContact != null) colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
    }

    private void loadBranchData() {
        if (tblBranches == null) return;
        tblBranches.getItems().clear();
        try {
            List<Branch> allBranches = branchDAO.getAll();
            ObservableList<Branch> obList = FXCollections.observableArrayList(allBranches);
            tblBranches.setItems(obList);
        } catch (SQLException | ClassNotFoundException e) {
            showError("Failed to load branches: " + e.getMessage());
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = (txtId != null && !txtId.getText().isEmpty()) ? txtId.getText() : generateBranchId();
        String name = txtName != null ? txtName.getText() : "";
        String address = txtAddress != null ? txtAddress.getText() : "";
        String contact = txtContact != null ? txtContact.getText() : "";

        if (name.isEmpty()) {
            showWarning("Branch name is required!");
            return;
        }
        if (address.isEmpty()) {
            showWarning("Branch address is required!");
            return;
        }

        Branch branch = new Branch(id, name, address, contact);
        try {
            if (branchDAO.save(branch)) {
                showSuccess("Branch saved successfully!");
                loadBranchData();
                loadBranchComboBox();
                clearBranchFields();
                generateNextBranchId();
            } else {
                showError("Failed to save branch");
            }
        } catch (SQLException | ClassNotFoundException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Branch ID already exists!");
            } else {
                showError("Save Error: " + e.getMessage());
            }
        }
    }
    
    @FXML
    void btnUpdateBranchOnAction(ActionEvent event) {
        String id = txtId != null ? txtId.getText() : "";
        String name = txtName != null ? txtName.getText() : "";
        String address = txtAddress != null ? txtAddress.getText() : "";
        String contact = txtContact != null ? txtContact.getText() : "";
        
        if (id.isEmpty()) {
            showWarning("Please select a branch to update");
            return;
        }
        if (name.isEmpty() || address.isEmpty()) {
            showWarning("Name and Address are required!");
            return;
        }
        
        Branch branch = new Branch(id, name, address, contact);
        try {
            if (branchDAO.update(branch)) {
                showSuccess("Branch updated successfully!");
                loadBranchData();
                loadBranchComboBox();
                clearBranchFields();
                generateNextBranchId();
            } else {
                showError("Failed to update branch");
            }
        } catch (SQLException | ClassNotFoundException e) {
            showError("Update Error: " + e.getMessage());
        }
    }
    
    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Branch selected = tblBranches != null ? tblBranches.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showWarning("Please select a branch to delete");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete branch: " + selected.getName() + "?",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (branchDAO.delete(selected.getBranchId())) {
                        showSuccess("Branch deleted successfully!");
                        loadBranchData();
                        loadBranchComboBox();
                        clearBranchFields();
                        generateNextBranchId();
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    if (e.getMessage().contains("foreign key constraint")) {
                        showError("Cannot delete branch with equipment or users assigned!");
                    } else {
                        showError("Delete Error: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearBranchFields();
        generateNextBranchId();
        if (tblBranches != null) tblBranches.getSelectionModel().clearSelection();
    }
    
    private String generateBranchId() {
        return "B" + System.currentTimeMillis();
    }

    private void clearBranchFields() {
        if (txtName != null) txtName.clear();
        if (txtAddress != null) txtAddress.clear();
        if (txtId != null) txtId.clear();
        if (txtContact != null) txtContact.clear();
    }
    
    // ==================== USER METHODS ====================
    
    private void loadBranchComboBox() {
        if (cmbUserBranch == null) return;
        try {
            List<Branch> branches = branchDAO.getAll();
            branchMap.clear();
            cmbUserBranch.getItems().clear();
            cmbUserBranch.getItems().add("-- No Branch --");
            branchMap.put("-- No Branch --", null);
            
            for (Branch b : branches) {
                String display = b.getBranchId() + " - " + b.getName();
                branchMap.put(display, b.getBranchId());
                cmbUserBranch.getItems().add(display);
            }
            cmbUserBranch.setValue("-- No Branch --");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setUserCellValueFactory() {
        if (colUserId != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (colUsername != null) colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (colRole != null) colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (colUserBranch != null) colUserBranch.setCellValueFactory(new PropertyValueFactory<>("branchId"));
    }
    
    private void loadUserData() {
        if (tblUsers == null) return;
        tblUsers.getItems().clear();
        try {
            List<User> users = userDAO.getAll();
            tblUsers.setItems(FXCollections.observableArrayList(users));
        } catch (SQLException | ClassNotFoundException e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }
    
    private void populateUserForm(User user) {
        selectedUser = user;
        if (lblUserId != null) lblUserId.setText(String.valueOf(user.getUserId()));
        if (txtUsername != null) txtUsername.setText(user.getUsername());
        if (txtPassword != null) txtPassword.clear();
        if (txtConfirmPassword != null) txtConfirmPassword.clear();
        if (cmbRole != null) cmbRole.setValue(user.getRole());
        
        if (cmbUserBranch != null && user.getBranchId() != null) {
            for (Map.Entry<String, String> entry : branchMap.entrySet()) {
                if (user.getBranchId().equals(entry.getValue())) {
                    cmbUserBranch.setValue(entry.getKey());
                    break;
                }
            }
        } else if (cmbUserBranch != null) {
            cmbUserBranch.setValue("-- No Branch --");
        }
    }
    
    @FXML
    void btnSaveUserOnAction(ActionEvent event) {
        String username = txtUsername != null ? txtUsername.getText().trim() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";
        String confirmPassword = txtConfirmPassword != null ? txtConfirmPassword.getText() : "";
        String role = cmbRole != null ? cmbRole.getValue() : "STAFF";
        String branchId = cmbUserBranch != null ? branchMap.get(cmbUserBranch.getValue()) : null;
        
        // Validation
        if (username.isEmpty()) {
            showWarning("Username is required!");
            return;
        }
        if (username.length() < 4) {
            showWarning("Username must be at least 4 characters!");
            return;
        }
        if (password.isEmpty()) {
            showWarning("Password is required!");
            return;
        }
        if (password.length() < 6) {
            showWarning("Password must be at least 6 characters!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showWarning("Passwords do not match!");
            return;
        }
        if (!"ADMIN".equals(role) && branchId == null) {
            showWarning("Staff and Branch Managers must be assigned to a branch!");
            return;
        }
        
        User user = new User(0, username, password, role, branchId);
        try {
            if (userDAO.save(user)) {
                showSuccess("User created successfully!");
                loadUserData();
                clearUserFields();
            } else {
                showError("Failed to create user");
            }
        } catch (SQLException | ClassNotFoundException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Username already exists!");
            } else {
                showError("Save Error: " + e.getMessage());
            }
        }
    }
    
    @FXML
    void btnUpdateUserOnAction(ActionEvent event) {
        if (selectedUser == null) {
            showWarning("Please select a user to update");
            return;
        }
        
        String username = txtUsername != null ? txtUsername.getText().trim() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";
        String confirmPassword = txtConfirmPassword != null ? txtConfirmPassword.getText() : "";
        String role = cmbRole != null ? cmbRole.getValue() : "STAFF";
        String branchId = cmbUserBranch != null ? branchMap.get(cmbUserBranch.getValue()) : null;
        
        if (username.isEmpty()) {
            showWarning("Username is required!");
            return;
        }
        
        // If password is empty, keep the old password
        String finalPassword = password.isEmpty() ? selectedUser.getPassword() : password;
        
        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            showWarning("Passwords do not match!");
            return;
        }
        
        if (!"ADMIN".equals(role) && branchId == null) {
            showWarning("Staff and Branch Managers must be assigned to a branch!");
            return;
        }
        
        User user = new User(selectedUser.getUserId(), username, finalPassword, role, branchId);
        try {
            if (userDAO.update(user)) {
                showSuccess("User updated successfully!");
                loadUserData();
                clearUserFields();
            } else {
                showError("Failed to update user");
            }
        } catch (SQLException | ClassNotFoundException e) {
            showError("Update Error: " + e.getMessage());
        }
    }
    
    @FXML
    void btnDeleteUserOnAction(ActionEvent event) {
        if (selectedUser == null) {
            showWarning("Please select a user to delete");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete user: " + selectedUser.getUsername() + "?",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (userDAO.delete(selectedUser.getUserId())) {
                        showSuccess("User deleted successfully!");
                        loadUserData();
                        clearUserFields();
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    showError("Delete Error: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    void btnClearUserOnAction(ActionEvent event) {
        clearUserFields();
        if (tblUsers != null) tblUsers.getSelectionModel().clearSelection();
    }
    
    private void clearUserFields() {
        selectedUser = null;
        if (lblUserId != null) lblUserId.setText("Auto");
        if (txtUsername != null) txtUsername.clear();
        if (txtPassword != null) txtPassword.clear();
        if (txtConfirmPassword != null) txtConfirmPassword.clear();
        if (cmbRole != null) cmbRole.setValue("STAFF");
        if (cmbUserBranch != null) cmbUserBranch.setValue("-- No Branch --");
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
