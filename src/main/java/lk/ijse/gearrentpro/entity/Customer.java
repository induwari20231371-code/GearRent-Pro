package lk.ijse.gearrentpro.entity;

public class Customer {
    private String customerId;
    private String name;
    private String nicPassport;
    private String contact;
    private String email;
    private String address;
    private String membershipLevel; // REGULAR, SILVER, GOLD

    public Customer() {
    }

    public Customer(String customerId, String name, String nicPassport, String contact, String email, String address, String membershipLevel) {
        this.customerId = customerId;
        this.name = name;
        this.nicPassport = nicPassport;
        this.contact = contact;
        this.email = email;
        this.address = address;
        this.membershipLevel = membershipLevel;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNicPassport() {
        return nicPassport;
    }

    public void setNicPassport(String nicPassport) {
        this.nicPassport = nicPassport;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(String membershipLevel) {
        this.membershipLevel = membershipLevel;
    }
}
