package com.buildit.dto.response;

public class AdminDeliveryPartnerResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean enabled;
    private String name;
    private String vehicleNumber;
    private Boolean available;

    public AdminDeliveryPartnerResponse(Long id, String username, String email, String phoneNumber, Boolean enabled,
                                         String name, String vehicleNumber, Boolean available) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.enabled = enabled;
        this.name = name;
        this.vehicleNumber = vehicleNumber;
        this.available = available;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
