package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.UpdateProfileRequest;
import org.datpham.foodlink.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.response.UserProfileResponse;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileResponse>> getProfile() {
        return ResponseEntity.ok(
                new BaseResponse<>(userService.getProfile(), "Success", 200)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                new BaseResponse<>(userService.updateProfile(request), "Profile updated successfully", 200)
        );
    }
}
