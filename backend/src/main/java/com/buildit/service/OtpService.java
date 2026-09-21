package com.buildit.service;

public interface OtpService {
    String generateAndStore(String phoneNumber);
    boolean verifyAndConsume(String phoneNumber, String code);
}
