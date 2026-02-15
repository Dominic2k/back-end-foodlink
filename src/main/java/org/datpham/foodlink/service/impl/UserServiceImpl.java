package org.datpham.foodlink.service.impl;

import org.datpham.foodlink.dto.response.UserProfileResponse;
import org.datpham.foodlink.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    
    @Override
    public UserProfileResponse getProfile() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        return new UserProfileResponse(
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatarUrl()
        );
    }

}
