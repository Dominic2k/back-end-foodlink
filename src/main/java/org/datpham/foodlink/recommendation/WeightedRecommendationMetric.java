package org.datpham.foodlink.recommendation;

import java.math.BigDecimal;

public record WeightedRecommendationMetric(
        RecommendationMetric metric,
        BigDecimal weight
) {
}
