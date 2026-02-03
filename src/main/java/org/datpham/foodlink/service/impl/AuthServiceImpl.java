package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.AuthResponse;
import org.datpham.foodlink.entity.Account;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.enums.Role;
import org.datpham.foodlink.enums.Status;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.AccountRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.security.JwtTokenProvider;
import org.datpham.foodlink.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(Role.USER);
        account.setStatus(Status.ACTIVE);

        Account saved = accountRepository.save(account);
        String token = jwtTokenProvider.generateToken(saved.getUsername());

        return new AuthResponse(token, "Bearer", saved.getUsername(), saved.getRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer", user.getEmail(), user.getIsAdmin()? "admin" : "user");
    }
}
