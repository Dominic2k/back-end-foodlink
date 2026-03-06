package org.datpham.foodlink.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.service.DishRecommendationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendationEvaluationEventHandler {

    private final DishRecommendationService dishRecommendationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFamilyProfileChanged(FamilyProfileChangedEvent event) {
        try {
            log.info("Starting background recommendation evaluation for user {}", event.userId());
            dishRecommendationService.evaluateForUserId(event.userId());
            log.info("Background recommendation evaluation completed for user {}", event.userId());
        } catch (Exception ex) {
            log.warn("Background recommendation evaluation failed for user {}: {}", event.userId(), ex.getMessage());
        }
    }
}
