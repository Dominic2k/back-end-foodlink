package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.response.DishRecommendationResponse;
import org.datpham.foodlink.dto.response.RecommendationFilterOptionsResponse;
import org.datpham.foodlink.dto.response.RecommendationPageResponse;

import java.util.List;

public interface DishRecommendationService {
    List<DishRecommendationResponse> evaluateForCurrentUser();
    List<DishRecommendationResponse> evaluateForUserId(String userId);
    RecommendationFilterOptionsResponse getFilterOptionsForCurrentUser();
    DishRecommendationResponse getRecommendationDetailForCurrentUser(String recipeId);
    RecommendationPageResponse getRecommendationsForCurrentUser(
            int page,
            int size,
            String suitable,
            String evaluated,
            Integer scoreMin,
            Integer scoreMax,
            String q,
            String ingredientCategory,
            String dishCategory
    );
}
