package com.buildit.dto.response;

public class DeliveryPartnerResponse {
    private Long id;
    private String name;
    private String vehicleNumber;
    private Boolean available;

    public DeliveryPartnerResponse(Long id, String name, String vehicleNumber, Boolean available) {
        this.id = id;
        this.name = name;
        this.vehicleNumber = vehicleNumber;
        this.available = available;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
