package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.UpdateProfileRequest;
import org.datpham.foodlink.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile();
    UserProfileResponse updateProfile(UpdateProfileRequest request);
    String uploadAvatar(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;
}
