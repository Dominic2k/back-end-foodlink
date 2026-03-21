package org.datpham.foodlink.recommendation;

import org.datpham.foodlink.dto.response.DishRatingSummaryResponse;
import org.datpham.foodlink.dto.response.RecommendationNutritionSummaryResponse;
import org.datpham.foodlink.entity.FamilyMember;
import org.datpham.foodlink.entity.Recipe;

import java.util.List;

public record RecommendationMetricContext(
        Recipe recipe,
        List<FamilyMember> familyMembers,
        int aiScore,
        DishRatingSummaryResponse ratingSummary,
        RecommendationNutritionSummaryResponse nutritionSummary
) {
}
