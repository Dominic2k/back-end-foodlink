package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RecipeRequest {

    @NotBlank(message = "Recipe name is required")
    private String name;

    private String description;
    private String instructions;
    private Integer prepTimeMin;
    private Integer cookTimeMin;
    private Integer baseServings;
    private String imageUrl;
    private String status; // draft, published, archived

    private List<RecipeIngredientItem> ingredients;

    @Getter
    @Setter
    public static class RecipeIngredientItem {
        private String ingredientId;
        private String ingredientName; // used when adding a new ingredient not yet in DB
        private BigDecimal quantity;
        private String unit;
        private Boolean isOptional;
    }
}
