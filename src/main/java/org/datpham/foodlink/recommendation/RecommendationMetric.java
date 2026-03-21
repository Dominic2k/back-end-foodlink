package org.datpham.foodlink.recommendation;

import java.math.BigDecimal;

public interface RecommendationMetric {
    String key();

    BigDecimal evaluate(RecommendationMetricContext context);
}
