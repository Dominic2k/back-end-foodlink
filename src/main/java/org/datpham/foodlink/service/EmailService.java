package org.datpham.foodlink.service;

public interface EmailService {
    void sendPasswordResetOtp(String toEmail, String otpCode);
}
