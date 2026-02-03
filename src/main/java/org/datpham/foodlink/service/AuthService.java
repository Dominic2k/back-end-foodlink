package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
