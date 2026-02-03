package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.validation.PasswordValidation;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @PasswordValidation
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(
        regexp = "^[\\p{L}\\s'-]+$",
        message = "Full name can only contain letters, spaces, hyphens, and apostrophes"
    )
    private String fullName;

    @Pattern(
        regexp = "^(\\+84|0)[0-9]{9,10}$",
        message = "Phone number must be a valid Vietnamese phone number"
    )
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phone;
}