package com.buildit.dto.response;

public class AdminVendorResponse {
    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private String storeName;
    private String location;

    public AdminVendorResponse(Long id, String username, String email, Boolean enabled, String storeName,
                                String location) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.storeName = storeName;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
