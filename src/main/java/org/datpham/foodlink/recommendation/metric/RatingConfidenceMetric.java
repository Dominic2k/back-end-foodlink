package org.datpham.foodlink.recommendation.metric;

import org.datpham.foodlink.dto.response.DishRatingSummaryResponse;
import org.datpham.foodlink.recommendation.RecommendationMetric;
import org.datpham.foodlink.recommendation.RecommendationMetricContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RatingConfidenceMetric implements RecommendationMetric {

    private static final BigDecimal TRUSTED_RATING_COUNT = new BigDecimal("20");

    @Override
    public String key() {
        return "ratingConfidence";
    }

    @Override
    public BigDecimal evaluate(RecommendationMetricContext context) {
        DishRatingSummaryResponse summary = context.ratingSummary();
        long totalRatings = summary == null || summary.getTotalRatings() == null ? 0L : summary.getTotalRatings();

        if (totalRatings <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(totalRatings)
                .divide(TRUSTED_RATING_COUNT, 4, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO)
                .min(BigDecimal.ONE);
    }
}
