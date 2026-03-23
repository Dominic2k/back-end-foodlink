package org.datpham.foodlink.security;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("adminAuthChecker")
@RequiredArgsConstructor
public class AdminAuthChecker {

    private final UserRepository userRepository;

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null && Boolean.TRUE.equals(user.getIsAdmin());
    }
}
