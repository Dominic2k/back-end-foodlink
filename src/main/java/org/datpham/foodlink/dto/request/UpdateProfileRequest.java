package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

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

    @Size(max = 512, message = "Address must not exceed 512 characters")
    private String address;

    @Size(max = 512, message = "Avatar URL must not exceed 512 characters")
    private String avatarUrl;
}
