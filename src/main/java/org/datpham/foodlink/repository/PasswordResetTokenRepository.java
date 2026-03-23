package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.PasswordResetToken;
import org.datpham.foodlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByOtpCodeAndUsedFalse(String otpCode);
    void deleteByUser(User user);
}
