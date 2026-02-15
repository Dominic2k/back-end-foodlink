package org.datpham.foodlink.dto.response;

import lombok.Getter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Getter
public class UserProfileResponse {

    private String email;
    private String avatarUrl;
    private String address;
    private String fullName;
    private String phone;
}
