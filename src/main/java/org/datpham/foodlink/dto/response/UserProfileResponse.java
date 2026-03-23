package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class UserProfileResponse {

    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String avatarUrl;
}
