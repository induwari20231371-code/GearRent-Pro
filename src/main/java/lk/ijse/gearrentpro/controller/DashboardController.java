package lk.ijse.gearrentpro.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import lk.ijse.gearrentpro.dao.DAOFactory;
import lk.ijse.gearrentpro.dao.custom.CustomerDAO;
import lk.ijse.gearrentpro.dao.custom.EquipmentDAO;
import lk.ijse.gearrentpro.dao.custom.RentalDAO;
import lk.ijse.gearrentpro.dao.custom.ReservationDAO;
import lk.ijse.gearrentpro.entity.Equipment;
import lk.ijse.gearrentpro.entity.Rental;
import lk.ijse.gearrentpro.entity.Reservation;
import lk.ijse.gearrentpro.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private AnchorPane contentPane;
    @FXML private Label lblTitle;
    @FXML private Label lblTime;
    @FXML private Label lblDate;
    @FXML private Label lblActiveRentals;
    @FXML private Label lblPendingReturns;
    @FXML private Label lblAvailableItems;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblPendingReservations;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblWelcome;
    @FXML private Label lblUserRole;
    
    // Buttons for role-based visibility
    @FXML private Button btnBranches;
    @FXML private Button btnReports;
    
    private final RentalDAO rentalDAO = (RentalDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RENTAL);
    private final EquipmentDAO equipmentDAO = (EquipmentDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.EQUIPMENT);
    private final CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.CUSTOMER);
    private final ReservationDAO reservationDAO = (ReservationDAO) DAOFactory.getDaoFactory().getDAO(DAOFactory.DAOTypes.RESERVATION);

    public void initialize() {
        initClock();
        loadDashboardData();
        setupRoleBasedAccess();
    }
    
    private void setupRoleBasedAccess() {
        User user = LoginController.getLoggedInUser();
        if (user != null) {
            // Update welcome message
            if (lblWelcome != null) {
                lblWelcome.setText("Welcome, " + user.getUsername());
            }
            if (lblUserRole != null) {
                lblUserRole.setText(formatRole(user.getRole()));
            }
            
            // Role-based menu visibility
            String role = user.getRole();
            
            // Only ADMIN can access Branch & Users management
            if (btnBranches != null && !"ADMIN".equals(role)) {
                btnBranches.setDisable(true);
                btnBranches.setStyle("-fx-opacity: 0.5;");
            }
            
            // Only ADMIN and BRANCH_MANAGER can access reports
            if (btnReports != null && "STAFF".equals(role)) {
                btnReports.setDisable(true);
                btnReports.setStyle("-fx-opacity: 0.5;");
            }
        }
    }
    
    private String formatRole(String role) {
        if (role == null) return "User";
        switch (role) {
            case "ADMIN": return "Administrator";
            case "BRANCH_MANAGER": return "Branch Manager";
            case "STAFF": return "Staff Member";
            default: return role;
        }
    }

    private void initClock() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), e -> {
            LocalDateTime now = LocalDateTime.now();
            if (lblTime != null) lblTime.setText(now.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
            if (lblDate != null) lblDate.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        }), new KeyFrame(Duration.seconds(1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loadDashboardData() {
        try {
            // Get branch filter for non-admin users
            String branchFilter = null;
            if (!LoginController.isAdmin()) {
                branchFilter = LoginController.getUserBranch();
            }
            
            // Count active rentals
            List<Rental> rentals = rentalDAO.getAll();
            final String finalBranchFilter = branchFilter;
            
            if (branchFilter != null) {
                rentals = rentals.stream()
                    .filter(r -> finalBranchFilter.equals(r.getBranchId()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            long activeRentals = rentals.stream()
                .filter(r -> "ACTIVE".equals(r.getRentalStatus()))
                .count();
            if (lblActiveRentals != null) lblActiveRentals.setText(String.valueOf(activeRentals));
            
            // Count pending returns (active rentals due today or overdue)
            java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
            long pendingReturns = rentals.stream()
                .filter(r -> "ACTIVE".equals(r.getRentalStatus()))
                .filter(r -> r.getEndDate() != null && !r.getEndDate().after(today))
                .count();
            if (lblPendingReturns != null) lblPendingReturns.setText(String.format("%02d", pendingReturns));
            
            // Count available equipment
            List<Equipment> equipment = equipmentDAO.getAll();
            if (branchFilter != null) {
                equipment = equipment.stream()
                    .filter(e -> finalBranchFilter.equals(e.getBranchId()))
                    .collect(java.util.stream.Collectors.toList());
            }
            long availableItems = equipment.stream()
                .filter(e -> "AVAILABLE".equals(e.getStatus()))
                .count();
            if (lblAvailableItems != null) lblAvailableItems.setText(String.valueOf(availableItems));
            
            // Count total customers
            long totalCustomers = customerDAO.getAll().size();
            if (lblTotalCustomers != null) lblTotalCustomers.setText(String.valueOf(totalCustomers));
            
            // Count pending reservations
            List<Reservation> reservations = reservationDAO.getAll();
            if (branchFilter != null) {
                reservations = reservations.stream()
                    .filter(r -> finalBranchFilter.equals(r.getBranchId()))
                    .collect(java.util.stream.Collectors.toList());
            }
            long pendingReservations = reservations.stream()
                .filter(r -> "PENDING".equals(r.getStatus()) || "CONFIRMED".equals(r.getStatus()))
                .count();
            if (lblPendingReservations != null) lblPendingReservations.setText(String.valueOf(pendingReservations));
            
            // Calculate total revenue (from all rentals)
            double totalRevenue = rentals.stream()
                .filter(r -> "RETURNED".equals(r.getRentalStatus()) || "ACTIVE".equals(r.getRentalStatus()))
                .mapToDouble(Rental::getFinalPayableAmount)
                .sum();
            if (lblTodayRevenue != null) {
                if (totalRevenue >= 1000000) {
                    lblTodayRevenue.setText(String.format("LKR %.1fM", totalRevenue / 1000000));
                } else if (totalRevenue >= 1000) {
                    lblTodayRevenue.setText(String.format("LKR %.1fK", totalRevenue / 1000));
                } else {
                    lblTodayRevenue.setText(String.format("LKR %.0f", totalRevenue));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values if error
            if (lblActiveRentals != null) lblActiveRentals.setText("--");
            if (lblPendingReturns != null) lblPendingReturns.setText("--");
            if (lblAvailableItems != null) lblAvailableItems.setText("--");
            if (lblTotalCustomers != null) lblTotalCustomers.setText("--");
            if (lblPendingReservations != null) lblPendingReservations.setText("--");
            if (lblTodayRevenue != null) lblTodayRevenue.setText("--");
        }
    }

    private void loadUI(String ui) {
        if (lblTitle != null) lblTitle.setText(ui.replace("Manage", "").trim());
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/" + ui + ".fxml"));
            contentPane.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load " + ui + ": " + e.getMessage()).show();
        }
    }
    
    @FXML
    void btnDashboardOnAction(ActionEvent event) {
        loadDashboardData();
        contentPane.getChildren().clear();
        if (lblTitle != null) lblTitle.setText("Dashboard Overview");
    }

    @FXML
    void btnBranchesOnAction(ActionEvent event) {
        if (!LoginController.isAdmin()) {
            new Alert(Alert.AlertType.WARNING, "Access Denied! Only administrators can manage branches and users.").show();
            return;
        }
        loadUI("ManageBranches");
    }

    @FXML
    void btnCustomersOnAction(ActionEvent event) {
        loadUI("ManageCustomers");
    }

    @FXML
    void btnEquipmentOnAction(ActionEvent event) {
        loadUI("ManageEquipment");
    }

    @FXML
    void btnLogoutOnAction(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to log out?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Logout");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    LoginController.logout();
                    lk.ijse.gearrentpro.App.setRoot("Login");
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    void btnRentalsOnAction(ActionEvent event) {
        loadUI("ManageRentals");
    }

    @FXML
    void btnReservationsOnAction(ActionEvent event) {
        loadUI("ManageReservations");
    }
    
    @FXML
    void btnRefreshOnAction(ActionEvent event) {
        loadDashboardData();
    }
}
