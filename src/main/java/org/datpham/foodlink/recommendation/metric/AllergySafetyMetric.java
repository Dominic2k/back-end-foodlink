package org.datpham.foodlink.recommendation.metric;

import org.datpham.foodlink.entity.FamilyMember;
import org.datpham.foodlink.entity.Ingredient;
import org.datpham.foodlink.entity.MemberAllergy;
import org.datpham.foodlink.entity.RecipeIngredient;
import org.datpham.foodlink.enums.Severity;
import org.datpham.foodlink.recommendation.RecommendationMetric;
import org.datpham.foodlink.recommendation.RecommendationMetricContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class AllergySafetyMetric implements RecommendationMetric {

    @Override
    public String key() {
        return "allergySafety";
    }

    @Override
    public BigDecimal evaluate(RecommendationMetricContext context) {
        if (context.recipe() == null || context.recipe().getRecipeIngredients() == null || context.familyMembers() == null) {
            return BigDecimal.ONE;
        }

        Map<String, Severity> allergySeverityByIngredientId = new HashMap<>();
        for (FamilyMember member : context.familyMembers()) {
            if (member.getAllergies() == null) {
                continue;
            }
            for (MemberAllergy allergy : member.getAllergies()) {
                Ingredient ingredient = allergy.getIngredient();
                if (ingredient == null || ingredient.getId() == null) {
                    continue;
                }
                allergySeverityByIngredientId.merge(
                        ingredient.getId(),
                        allergy.getSeverity(),
                        this::maxSeverity
                );
            }
        }

        if (allergySeverityByIngredientId.isEmpty()) {
            return BigDecimal.ONE;
        }

        BigDecimal penalty = BigDecimal.ONE;
        for (RecipeIngredient recipeIngredient : context.recipe().getRecipeIngredients()) {
            String ingredientId = recipeIngredient.getIngredientId();
            if (ingredientId == null && recipeIngredient.getIngredient() != null) {
                ingredientId = recipeIngredient.getIngredient().getId();
            }
            if (ingredientId == null) {
                continue;
            }

            Severity severity = allergySeverityByIngredientId.get(ingredientId);
            if (severity == null) {
                continue;
            }

            if (severity == Severity.severe) {
                return BigDecimal.ZERO;
            }
            if (severity == Severity.medium) {
                penalty = penalty.min(new BigDecimal("0.25"));
            } else if (severity == Severity.mild) {
                penalty = penalty.min(new BigDecimal("0.60"));
            }
        }

        return penalty;
    }

    private Severity maxSeverity(Severity left, Severity right) {
        return severityRank(left) >= severityRank(right) ? left : right;
    }

    private int severityRank(Severity severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity) {
            case mild -> 1;
            case medium -> 2;
            case severe -> 3;
        };
    }
}
