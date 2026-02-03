package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.RegisterResponse;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.enums.UserStatus;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.security.JwtTokenProvider;
import org.datpham.foodlink.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
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
}

