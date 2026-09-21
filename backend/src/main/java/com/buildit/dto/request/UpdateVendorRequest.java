package com.buildit.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateVendorRequest {
    @NotBlank
    private String storeName;

    @NotBlank
    private String location;

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
