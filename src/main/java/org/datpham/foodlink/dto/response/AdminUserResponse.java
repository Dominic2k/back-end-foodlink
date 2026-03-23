package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.datpham.foodlink.enums.UserStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminUserResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String avatarUrl;
    private UserStatus status;
    private Boolean isAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private Long lastSessionDurationSeconds;
    private Double avgSessionDurationSeconds;
}
