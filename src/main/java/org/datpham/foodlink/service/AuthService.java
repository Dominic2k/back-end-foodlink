package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
