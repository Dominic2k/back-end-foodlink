package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.validation.PasswordValidation;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @PasswordValidation
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;
}
