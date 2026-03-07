package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RecommendationIngredientDetailResponse {
    private String ingredientId;
    private String ingredientName;
    private String category;
    private BigDecimal quantity;
    private String unit;
    private Boolean optional;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carb;
    private BigDecimal fat;
}

