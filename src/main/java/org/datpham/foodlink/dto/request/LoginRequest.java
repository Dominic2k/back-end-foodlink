package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Username must not be blank")
    @Email(message = "Username must be a valid email address")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;
}
