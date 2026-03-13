package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.ChangePasswordRequest;
import org.datpham.foodlink.dto.request.ForgotPasswordRequest;
import org.datpham.foodlink.dto.request.ResetPasswordRequest;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.RegisterResponse;
import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.response.LoginResponse;
import org.datpham.foodlink.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(
            new BaseResponse<>(response, "Registered successfully",200)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(
                new BaseResponse<>(response, "Login successful", 200)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(
                new BaseResponse<>("Logout successful", "OK", 200)
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<BaseResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(
                new BaseResponse<>("Đổi mật khẩu thành công", "OK", 200)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                new BaseResponse<>("Nếu email tồn tại, mã OTP đã được gửi đến email của bạn", "OK", 200)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                new BaseResponse<>("Đặt lại mật khẩu thành công", "OK", 200)
        );
    }
}