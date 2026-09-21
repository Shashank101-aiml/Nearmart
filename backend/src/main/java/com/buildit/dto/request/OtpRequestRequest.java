package com.buildit.dto.request;

import jakarta.validation.constraints.NotBlank;

public class OtpRequestRequest {
    @NotBlank
    private String phoneNumber;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
