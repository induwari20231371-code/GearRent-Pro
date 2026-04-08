package lk.ijse.gearrentpro.entity;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // ADMIN, BRANCH_MANAGER, STAFF
    private String branchId;

    public User() {
    }

    public User(int userId, String username, String password, String role, String branchId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.branchId = branchId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", branchId='" + branchId + '\'' +
                '}';
    }
}
