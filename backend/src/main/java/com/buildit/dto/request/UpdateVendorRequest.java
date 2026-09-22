package com.buildit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateVendorRequest {
    @NotBlank
    private String storeName;

    @NotBlank
    private String location;

    @Size(min = 3, max = 50)
    private String username;

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
