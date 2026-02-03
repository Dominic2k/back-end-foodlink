package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.RegisterResponse;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.response.LoginResponse;
import org.datpham.foodlink.enums.UserStatus;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.security.JwtTokenProvider;
import org.datpham.foodlink.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.datpham.foodlink.security.TokenBlacklistService;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.active);
        user.setIsAdmin(false);

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getEmail());

        return RegisterResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .email(saved.getEmail())
            .fullName(saved.getFullName())
            .isAdmin(saved.getIsAdmin())
            .build();
            
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Email or password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() == UserStatus.blocked) {
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

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Invalid token", HttpStatus.BAD_REQUEST);
        }

        String token = authHeader.substring(7);

        Date expirationDate = jwtTokenProvider.getExpirationDate(token);
        long remainingTime = expirationDate.getTime() - System.currentTimeMillis();

        tokenBlacklistService.blacklistToken(token, remainingTime);
    }
}

