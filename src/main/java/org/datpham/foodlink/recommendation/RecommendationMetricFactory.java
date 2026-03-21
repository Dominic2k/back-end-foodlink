package org.datpham.foodlink.recommendation;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.recommendation.metric.AiSuitabilityMetric;
import org.datpham.foodlink.recommendation.metric.AllergySafetyMetric;
import org.datpham.foodlink.recommendation.metric.NutritionCompletenessMetric;
import org.datpham.foodlink.recommendation.metric.RatingConfidenceMetric;
import org.datpham.foodlink.recommendation.metric.RatingQualityMetric;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationMetricFactory {

    private final AiSuitabilityMetric aiSuitabilityMetric;
    private final RatingQualityMetric ratingQualityMetric;
    private final RatingConfidenceMetric ratingConfidenceMetric;
    private final NutritionCompletenessMetric nutritionCompletenessMetric;
    private final AllergySafetyMetric allergySafetyMetric;

    public List<WeightedRecommendationMetric> createBaseMetrics() {
        return List.of(
                new WeightedRecommendationMetric(aiSuitabilityMetric, new BigDecimal("0.60")),
                new WeightedRecommendationMetric(ratingQualityMetric, new BigDecimal("0.20")),
                new WeightedRecommendationMetric(ratingConfidenceMetric, new BigDecimal("0.10")),
                new WeightedRecommendationMetric(nutritionCompletenessMetric, new BigDecimal("0.10"))
        );
    }

    public RecommendationMetric createSafetyMetric() {
        return allergySafetyMetric;
    }
}
