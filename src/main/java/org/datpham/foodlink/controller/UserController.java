package org.datpham.foodlink.controller;

import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
}
