package org.datpham.foodlink.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.service.DishRecommendationService;
import org.datpham.foodlink.service.RecommendationEvaluationStatusService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendationEvaluationEventHandler {

    private final DishRecommendationService dishRecommendationService;
    private final RecommendationEvaluationStatusService recommendationEvaluationStatusService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFamilyProfileChanged(FamilyProfileChangedEvent event) {
        try {
            recommendationEvaluationStatusService.markProcessing(event.userId());
            log.info("Starting background recommendation evaluation for user {}", event.userId());
            dishRecommendationService.evaluateForUserId(event.userId());
            recommendationEvaluationStatusService.markCompleted(event.userId());
            log.info("Background recommendation evaluation completed for user {}", event.userId());
        } catch (Exception ex) {
            recommendationEvaluationStatusService.markFailed(event.userId(), ex.getMessage());
            log.warn("Background recommendation evaluation failed for user {}: {}", event.userId(), ex.getMessage());
        }
    }
}
