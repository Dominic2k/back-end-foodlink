package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RecipeResponse {
    private String id;
    private String name;
    private String description;
    private String instructions;
    private Integer prepTimeMin;
    private Integer cookTimeMin;
    private Integer baseServings;
    private BigDecimal totalIngredientPrice;
    private BigDecimal pricePerServing;
    private String imageUrl;
    private String status;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecipeIngredientItem> ingredients;
    private List<CategoryItem> categories;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecipeIngredientItem {
        private String ingredientId;
        private String ingredientName;
        private BigDecimal quantity;
        private String unit;
        private Boolean isOptional;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CategoryItem {
        private String id;
        private String name;
    }
}
