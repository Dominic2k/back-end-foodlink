package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.response.LoginResponse;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.enums.UserStatus;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.security.JwtTokenProvider;
import org.datpham.foodlink.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Email or password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() == UserStatus.block) {
            throw new BusinessException("Account has been blocked", HttpStatus.FORBIDDEN);
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        
        return new LoginResponse(
                token,
                "Bearer",
                user.getEmail(),
                user.getIsAdmin() ? "ADMIN" : "USER"
        );
    }
}
