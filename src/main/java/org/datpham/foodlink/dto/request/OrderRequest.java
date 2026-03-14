package org.datpham.foodlink.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddressText;

    private String deliveryPhone;
    private String note;

    private String paymentMethod;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Getter
    @Setter
    public static class OrderItemRequest {
        @NotBlank(message = "Recipe ID is required")
        private String recipeId;

        @NotNull(message = "Servings is required")
        @Min(value = 1, message = "Servings must be at least 1")
        private Integer servings;

        private List<CustomIngredientRequest> customIngredients;
    }

    @Getter
    @Setter
    public static class CustomIngredientRequest {
        @NotBlank(message = "Ingredient ID is required")
        private String ingredientId;

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        private Double quantity;

        private String unit;
    }
}
