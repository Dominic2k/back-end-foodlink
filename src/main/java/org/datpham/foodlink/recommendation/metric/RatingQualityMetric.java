package org.datpham.foodlink.recommendation.metric;

import org.datpham.foodlink.dto.response.DishRatingSummaryResponse;
import org.datpham.foodlink.recommendation.RecommendationMetric;
import org.datpham.foodlink.recommendation.RecommendationMetricContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RatingQualityMetric implements RecommendationMetric {

    @Override
    public String key() {
        return "ratingQuality";
    }

    @Override
    public BigDecimal evaluate(RecommendationMetricContext context) {
        DishRatingSummaryResponse summary = context.ratingSummary();
        if (summary == null || summary.getAverageRating() == null) {
            return BigDecimal.ZERO;
        }

        return summary.getAverageRating()
                .divide(new BigDecimal("5"), 4, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO)
                .min(BigDecimal.ONE);
    }
}
