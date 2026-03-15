package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IngredientRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String category;
    @NotBlank(message = "Base unit is required")
    private String baseUnit;
    @NotNull(message = "Price per base unit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per base unit must be greater than or equal to 0")
    private BigDecimal pricePerBaseUnit;
    @NotNull(message = "Stock quantity is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Stock quantity must be greater than or equal to 0")
    private BigDecimal stockQuantityBase;
    private String imageUrl;
    private Boolean isActive;

    // Nutrition
    private BigDecimal caloriesPer100;
    private BigDecimal proteinGPer100;
    private BigDecimal carbGPer100;
    private BigDecimal fatGPer100;
}
