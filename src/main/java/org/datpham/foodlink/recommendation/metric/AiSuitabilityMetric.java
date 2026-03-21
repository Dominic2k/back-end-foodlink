package org.datpham.foodlink.recommendation.metric;

import org.datpham.foodlink.recommendation.RecommendationMetric;
import org.datpham.foodlink.recommendation.RecommendationMetricContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AiSuitabilityMetric implements RecommendationMetric {

    @Override
    public String key() {
        return "aiSuitability";
    }

    @Override
    public BigDecimal evaluate(RecommendationMetricContext context) {
        int boundedScore = Math.max(0, Math.min(100, context.aiScore()));
        return BigDecimal.valueOf(boundedScore)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }
}
