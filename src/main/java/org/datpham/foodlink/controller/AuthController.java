package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.AuthResponse;
import org.datpham.foodlink.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(new BaseResponse<>(response, "Registered successfully"));
    }
}
