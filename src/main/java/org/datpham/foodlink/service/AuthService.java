package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.RegisterResponse;
import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.response.LoginResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String authHeader);
}
