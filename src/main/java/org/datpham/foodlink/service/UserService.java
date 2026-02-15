package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.UpdateProfileRequest;
import org.datpham.foodlink.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile();
    UserProfileResponse updateProfile(UpdateProfileRequest request);
}
