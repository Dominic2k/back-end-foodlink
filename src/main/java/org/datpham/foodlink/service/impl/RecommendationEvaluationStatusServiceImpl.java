package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.response.RecommendationEvaluationStatusResponse;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.service.RecommendationEvaluationStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RecommendationEvaluationStatusServiceImpl implements RecommendationEvaluationStatusService {

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, RecommendationEvaluationStatusResponse> statusStore = new ConcurrentHashMap<>();

    @Override
    public void markQueued(String userId) {
        LocalDateTime now = LocalDateTime.now();
        statusStore.compute(userId, (key, current) -> RecommendationEvaluationStatusResponse.builder()
                .status("queued")
                .lastTriggeredAt(now)
                .lastCompletedAt(current != null ? current.getLastCompletedAt() : null)
                .updatedAt(now)
                .errorMessage(null)
                .build());
    }

    @Override
    public void markProcessing(String userId) {
        LocalDateTime now = LocalDateTime.now();
        statusStore.compute(userId, (key, current) -> RecommendationEvaluationStatusResponse.builder()
                .status("processing")
                .lastTriggeredAt(current != null ? current.getLastTriggeredAt() : now)
                .lastCompletedAt(current != null ? current.getLastCompletedAt() : null)
                .updatedAt(now)
                .errorMessage(null)
                .build());
    }

    @Override
    public void markCompleted(String userId) {
        LocalDateTime now = LocalDateTime.now();
        statusStore.compute(userId, (key, current) -> RecommendationEvaluationStatusResponse.builder()
                .status("completed")
                .lastTriggeredAt(current != null ? current.getLastTriggeredAt() : now)
                .lastCompletedAt(now)
                .updatedAt(now)
                .errorMessage(null)
                .build());
    }

    @Override
    public void markFailed(String userId, String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        statusStore.compute(userId, (key, current) -> RecommendationEvaluationStatusResponse.builder()
                .status("failed")
                .lastTriggeredAt(current != null ? current.getLastTriggeredAt() : now)
                .lastCompletedAt(current != null ? current.getLastCompletedAt() : null)
                .updatedAt(now)
                .errorMessage(errorMessage)
                .build());
    }

    @Override
    public RecommendationEvaluationStatusResponse getCurrentUserStatus() {
        User user = getCurrentUser();
        return statusStore.getOrDefault(
                user.getId(),
                RecommendationEvaluationStatusResponse.builder()
                        .status("idle")
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }
}
