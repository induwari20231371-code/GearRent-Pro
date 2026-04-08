package lk.ijse.gearrentpro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.UserDAO;
import lk.ijse.gearrentpro.entity.User;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;
    
    @FXML
    private Label lblError;

    private UserDAO userDAO = (UserDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.USER);
    
    // Static variable to store logged-in user for role-based access
    private static User loggedInUser;
    
    public static User getLoggedInUser() {
        return loggedInUser;
    }
    
    public static String getUserRole() {
        return loggedInUser != null ? loggedInUser.getRole() : null;
    }
    
    public static String getUserBranch() {
        return loggedInUser != null ? loggedInUser.getBranchId() : null;
    }
    
    public static boolean isAdmin() {
        return loggedInUser != null && "ADMIN".equals(loggedInUser.getRole());
    }
    
    public static boolean isBranchManager() {
        return loggedInUser != null && "BRANCH_MANAGER".equals(loggedInUser.getRole());
    }
    
    public static boolean isStaff() {
        return loggedInUser != null && "STAFF".equals(loggedInUser.getRole());
    }
    
    public static void logout() {
        loggedInUser = null;
    }

    @FXML
    void btnLoginOnAction(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        
        // Clear previous error
        if (lblError != null) lblError.setText("");

        // Validation
        if (username.isEmpty()) {
            showError("Please enter your username");
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password");
            return;
        }

        try {
            User user = userDAO.findByUsername(username);
            if (user != null) {
                if (user.getPassword().equals(password)) {
                    // Store logged in user
                    loggedInUser = user;
                    
                    // Navigate to Dashboard
                    lk.ijse.gearrentpro.App.setRoot("Dashboard");
                } else {
                    showError("Invalid password. Please try again.");
                    txtPassword.clear();
                    txtPassword.requestFocus();
                }
            } else {
                showError("User not found. Please check your username.");
                txtUsername.requestFocus();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    void txtUsernameOnAction(ActionEvent event) {
        txtPassword.requestFocus();
    }
    
    @FXML
    void txtPasswordOnAction(ActionEvent event) {
        btnLoginOnAction(event);
    }
    
    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}
