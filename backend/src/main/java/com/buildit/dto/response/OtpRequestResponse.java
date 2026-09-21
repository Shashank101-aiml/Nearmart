package com.buildit.dto.response;

public class OtpRequestResponse {
    private String message;
    private String otp;

    public OtpRequestResponse(String message, String otp) {
        this.message = message;
        this.otp = otp;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
