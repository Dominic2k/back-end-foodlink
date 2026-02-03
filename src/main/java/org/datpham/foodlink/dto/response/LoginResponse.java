package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private String tokenType;
    private String username;
    private String role;
}
