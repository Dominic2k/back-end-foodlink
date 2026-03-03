package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IngredientRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String category;
    private String defaultUnit;
    private String imageUrl;
    private Boolean isActive;

    // Nutrition
    private BigDecimal caloriesPer100;
    private BigDecimal proteinGPer100;
    private BigDecimal carbGPer100;
    private BigDecimal fatGPer100;
}
