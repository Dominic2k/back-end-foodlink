package org.datpham.foodlink.recommendation.metric;

import org.datpham.foodlink.dto.response.RecommendationNutritionSummaryResponse;
import org.datpham.foodlink.recommendation.RecommendationMetric;
import org.datpham.foodlink.recommendation.RecommendationMetricContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class NutritionCompletenessMetric implements RecommendationMetric {

    @Override
    public String key() {
        return "nutritionCompleteness";
    }

    @Override
    public BigDecimal evaluate(RecommendationMetricContext context) {
        RecommendationNutritionSummaryResponse summary = context.nutritionSummary();
        if (summary == null || summary.getTotalIngredients() == null || summary.getTotalIngredients() <= 0) {
            return BigDecimal.ZERO;
        }

        int covered = summary.getCoveredIngredients() == null ? 0 : Math.max(summary.getCoveredIngredients(), 0);
        int total = Math.max(summary.getTotalIngredients(), 1);
        return BigDecimal.valueOf(covered)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO)
                .min(BigDecimal.ONE);
    }
}
