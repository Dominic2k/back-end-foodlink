package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.response.DishRecommendationResponse;

import java.util.List;

public interface DishRecommendationService {
    List<DishRecommendationResponse> evaluateForCurrentUser();
    List<DishRecommendationResponse> evaluateForUserId(String userId);
}
