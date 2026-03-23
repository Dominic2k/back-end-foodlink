package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.enums.Severity;

@Getter
@Setter
public class AllergyRequest {

    @NotBlank(message = "Ingredient ID is required")
    private String ingredientId;

    @NotNull(message = "Severity is required")
    private Severity severity;
}
