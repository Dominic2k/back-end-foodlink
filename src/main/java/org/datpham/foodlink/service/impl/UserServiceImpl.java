package org.datpham.foodlink.service.impl;

import org.datpham.foodlink.dto.request.UpdateProfileRequest;
import org.datpham.foodlink.dto.response.UserProfileResponse;
import org.datpham.foodlink.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.service.UserService;
import org.datpham.foodlink.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public UserProfileResponse getProfile() {
        User user = getCurrentUser();
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatarUrl(request.getAvatarUrl());

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) throws IOException {
        User user = getCurrentUser();
        String folderName = "avatars/" + user.getId();
        String avatarUrl = cloudinaryService.uploadImage(file, folderName);
        
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        return avatarUrl;
    }

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
