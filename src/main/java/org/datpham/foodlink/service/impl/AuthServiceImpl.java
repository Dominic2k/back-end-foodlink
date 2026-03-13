package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.dto.request.ChangePasswordRequest;
import org.datpham.foodlink.dto.request.ForgotPasswordRequest;
import org.datpham.foodlink.dto.request.ResetPasswordRequest;
import org.datpham.foodlink.dto.request.RegisterRequest;
import org.datpham.foodlink.dto.response.RegisterResponse;
import org.datpham.foodlink.entity.PasswordResetToken;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.dto.request.LoginRequest;
import org.datpham.foodlink.dto.response.LoginResponse;
import org.datpham.foodlink.enums.UserStatus;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.PasswordResetTokenRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.security.JwtTokenProvider;
import org.datpham.foodlink.service.AuthService;
import org.datpham.foodlink.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.datpham.foodlink.security.TokenBlacklistService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
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

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu mới không được trùng với mật khẩu hiện tại", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user {}", email);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        // Always respond success to prevent email enumeration
        if (user == null) {
            log.warn("Forgot password requested for non-existent email: {}", request.getEmail());
            return;
        }

        if (user.getStatus() == UserStatus.blocked) {
            log.warn("Forgot password requested for blocked user: {}", request.getEmail());
            return;
        }

        // Delete existing tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate OTP
        String otpCode = generateOtp();

        // Save token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setOtpCode(otpCode);
        resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        // Send email
        try {
            emailService.sendPasswordResetOtp(user.getEmail(), otpCode);
            log.info("Password reset OTP generated and sent to {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", request.getEmail(), e.getMessage());
            throw new BusinessException("Không thể gửi email. Vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByOtpCodeAndUsedFalse(request.getOtpCode())
                .orElseThrow(() -> new BusinessException("Mã OTP không hợp lệ hoặc đã được sử dụng", HttpStatus.BAD_REQUEST));

        if (resetToken.isExpired()) {
            throw new BusinessException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new BusinessException("Mã OTP không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for user {}", user.getEmail());
    }

    private String generateOtp() {
        int otp = SECURE_RANDOM.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }
}
