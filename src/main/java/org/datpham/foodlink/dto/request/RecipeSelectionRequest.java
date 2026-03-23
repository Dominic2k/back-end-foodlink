package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeSelectionRequest {

    @NotBlank(message = "Recipe ID is required")
    private String recipeId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
