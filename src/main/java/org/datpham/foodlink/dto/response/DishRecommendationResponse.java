package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DishRecommendationResponse {
    private String recipeId;
    private String recipeName;
    private String imageUrl;
    private String recipeDescription;
    private String recipeInstructions;
    private Integer prepTimeMin;
    private Integer cookTimeMin;
    private Integer baseServings;
    private BigDecimal totalIngredientPrice;
    private BigDecimal pricePerServing;
    private String category; // Currently used for most frequent ingredient category
    private List<String> dishCategories;
    private Boolean evaluated;
    private Integer score;
    private Boolean suitable;
    private String reason;
    private String suggestion;
    private List<RecommendationIngredientDetailResponse> ingredients;
    private RecommendationNutritionSummaryResponse nutritionSummary;
}
