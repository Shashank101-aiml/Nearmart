package com.buildit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpRequestRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Please enter a valid Number")
    private String phoneNumber;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
