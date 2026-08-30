package com.buildit.dto.response;

public class VendorResponse {
    private Long id;
    private String storeName;
    private String location;

    public VendorResponse(Long id, String storeName, String location) {
        this.id = id;
        this.storeName = storeName;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
