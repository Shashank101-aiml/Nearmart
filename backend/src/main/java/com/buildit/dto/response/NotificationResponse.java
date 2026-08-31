package com.buildit.dto.response;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse(Long id, String message, Boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
