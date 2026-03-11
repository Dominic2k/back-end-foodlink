package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.response.RecommendationEvaluationStatusResponse;

public interface RecommendationEvaluationStatusService {
    void markQueued(String userId);
    void markProcessing(String userId);
    void markCompleted(String userId);
    void markFailed(String userId, String errorMessage);
    RecommendationEvaluationStatusResponse getCurrentUserStatus();
}
