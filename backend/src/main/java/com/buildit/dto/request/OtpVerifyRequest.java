package com.buildit.dto.request;

import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String code;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
