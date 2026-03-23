package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.ChangePasswordRequest;
import org.datpham.foodlink.dto.request.ForgotPasswordRequest;
import org.datpham.foodlink.dto.request.ResetPasswordRequest;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.RegisterResponse;
import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.response.LoginResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String authHeader);

    void changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
