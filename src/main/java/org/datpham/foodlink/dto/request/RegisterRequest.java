package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.validation.PasswordValidation;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @PasswordValidation
    private String password;

    // TODO: Add extra profile fields if needed (email, phone, name...).
}
