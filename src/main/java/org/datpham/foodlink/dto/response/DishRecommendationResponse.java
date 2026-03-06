package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DishRecommendationResponse {
    private String recipeId;
    private String recipeName;
    private Integer score;
    private Boolean suitable;
    private String reason;
    private String suggestion;
}
