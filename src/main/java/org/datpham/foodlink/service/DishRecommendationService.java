package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.response.DishRecommendationResponse;
import org.datpham.foodlink.dto.response.RecommendationPageResponse;

import java.util.List;

public interface DishRecommendationService {
    List<DishRecommendationResponse> evaluateForCurrentUser();
    List<DishRecommendationResponse> evaluateForUserId(String userId);
    RecommendationPageResponse getRecommendationsForCurrentUser(int page, int size);
}
