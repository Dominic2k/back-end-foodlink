package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationEvaluationStatusResponse {
    private String status;
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime lastCompletedAt;
    private LocalDateTime updatedAt;
    private String errorMessage;
}
