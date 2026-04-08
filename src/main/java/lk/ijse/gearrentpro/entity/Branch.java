package lk.ijse.gearrentpro.entity;

public class Branch {
    private String branchId;
    private String name;
    private String address;
    private String contact;

    public Branch() {
    }

    public Branch(String branchId, String name, String address, String contact) {
        this.branchId = branchId;
        this.name = name;
        this.address = address;
        this.contact = contact;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
