package com.buildit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerifyRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Please enter a valid Number")
    private String phoneNumber;

    @NotBlank
    private String code;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
