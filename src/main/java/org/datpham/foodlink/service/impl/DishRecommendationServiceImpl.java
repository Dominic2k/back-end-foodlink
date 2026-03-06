package org.datpham.foodlink.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.dto.response.DishRecommendationResponse;
import org.datpham.foodlink.entity.*;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.DishRecommendationRepository;
import org.datpham.foodlink.repository.FamilyMemberRepository;
import org.datpham.foodlink.repository.RecipeRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.service.DishRecommendationService;
import org.datpham.foodlink.service.GenaiService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DishRecommendationServiceImpl implements DishRecommendationService {

    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final RecipeRepository recipeRepository;
    private final DishRecommendationRepository dishRecommendationRepository;
    private final GenaiService genaiService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<DishRecommendationResponse> evaluateForCurrentUser() {
        User user = getCurrentUser();
        return evaluateForUserId(user.getId());
    }

    @Override
    @Transactional
    public List<DishRecommendationResponse> evaluateForUserId(String userId) {
        long startedAt = System.currentTimeMillis();
        log.info("Recommendation evaluation started for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        List<FamilyMember> members = familyMemberRepository.findAllByUserIdWithConditions(user.getId());
        List<Recipe> recipes = recipeRepository.findByStatus(Recipe.RecipeStatus.published);
        log.info("Loaded recommendation input for user {}: members={}, publishedRecipes={}", userId, members.size(), recipes.size());

        if (recipes.isEmpty()) {
            log.info("No published recipes found. Skip recommendation evaluation for user {}", userId);
            return List.of();
        }

        log.info("Building AI prompt for user {}", userId);
        String prompt = buildPrompt(user, members, recipes);
        log.info("Prompt built for user {} ({} chars)", userId, prompt.length());
        log.debug("Prompt preview for user {}: {}", userId, shorten(prompt, 500));

        log.info("Calling GenAI for user {}", userId);
        long aiStartedAt = System.currentTimeMillis();
        String aiRaw = genaiService.testGenai(prompt);
        log.info("GenAI response received for user {} in {} ms ({} chars)",
                userId, System.currentTimeMillis() - aiStartedAt, aiRaw == null ? 0 : aiRaw.length());
        log.debug("GenAI raw response preview for user {}: {}", userId, shorten(aiRaw, 500));

        log.info("Parsing GenAI response for user {}", userId);
        List<EvaluationResult> results = parseAiResponse(aiRaw, recipes);
        log.info("Parsed {} evaluation records for user {}", results.size(), userId);

        Map<String, Recipe> recipeMap = recipes.stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));

        List<DishRecommendation> saved = new ArrayList<>();
        int skippedUnknownRecipe = 0;
        for (EvaluationResult result : results) {
            Recipe recipe = recipeMap.get(result.recipeId());
            if (recipe == null) {
                skippedUnknownRecipe++;
                continue;
            }

            DishRecommendation record = dishRecommendationRepository
                    .findByUser_IdAndRecipe_Id(user.getId(), recipe.getId())
                    .orElseGet(DishRecommendation::new);

            record.setUser(user);
            record.setRecipe(recipe);
            record.setScore(Math.max(0, Math.min(100, result.score())));
            record.setSuitable(result.suitable());
            record.setReason(result.reason());
            record.setSuggestion(result.suggestion());
            saved.add(dishRecommendationRepository.save(record));
        }

        log.info("Saved {} recommendations for user {} (skippedUnknownRecipeIds={})",
                saved.size(), userId, skippedUnknownRecipe);

        List<DishRecommendationResponse> response = saved.stream()
                .sorted(Comparator.comparing(DishRecommendation::getScore).reversed())
                .map(this::toResponse)
                .toList();

        log.info("Recommendation evaluation completed for user {} in {} ms",
                userId, System.currentTimeMillis() - startedAt);
        return response;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED));
    }

    private String buildPrompt(User user, List<FamilyMember> members, List<Recipe> recipes) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a nutrition assistant.\n");
        sb.append("Evaluate whether each recipe is suitable for this user profile.\n");
        sb.append("IMPORTANT: Output all text fields in Vietnamese.\n");
        sb.append("Return STRICT JSON only with this shape:\n");
        sb.append("{\"evaluations\":[{\"recipeId\":\"...\",\"score\":0,\"suitable\":true,\"reason\":\"...\",\"suggestion\":\"...\"}]}\n");
        sb.append("Score is 0-100. Higher means more suitable.\n\n");
        sb.append("`reason`: explain shortly why this dish is suitable or not suitable.\n");
        sb.append("`suggestion`: short, clear guidance for how family members should eat this dish (who should eat less/more/avoid).\n\n");

        sb.append("USER:\n");
        sb.append("- fullName: ").append(nullSafe(user.getFullName())).append('\n');
        sb.append("- health context by family members:\n");
        for (FamilyMember member : members) {
            String conditions = member.getHealthConditions().stream()
                    .map(HealthCondition::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));
            String allergies = member.getAllergies().stream()
                    .map(a -> {
                        Ingredient ingredient = a.getIngredient();
                        String ingredientName = ingredient != null ? ingredient.getName() : "unknown";
                        return ingredientName + " (" + a.getSeverity() + ")";
                    })
                    .sorted()
                    .collect(Collectors.joining(", "));

            sb.append("  - ").append(member.getDisplayName())
                    .append(": conditions=[").append(conditions).append("]")
                    .append(", allergies=[").append(allergies).append("]")
                    .append(", notes=").append(nullSafe(member.getHealthNotes()))
                    .append('\n');
        }

        sb.append("\nRECIPES:\n");
        for (Recipe recipe : recipes) {
            sb.append("- recipeId=").append(recipe.getId()).append('\n');
            sb.append("  name=").append(recipe.getName()).append('\n');
            sb.append("  description=").append(nullSafe(recipe.getDescription())).append('\n');
            sb.append("  ingredients:\n");

            if (recipe.getRecipeIngredients() != null) {
                for (RecipeIngredient ri : recipe.getRecipeIngredients()) {
                    Ingredient ingredient = ri.getIngredient();
                    String ingredientName = ingredient != null ? ingredient.getName() : "unknown";
                    IngredientNutrition nutrition = ingredient != null ? ingredient.getNutrition() : null;

                    sb.append("    - ").append(ingredientName)
                            .append(", qty=").append(ri.getQuantity())
                            .append(" ").append(ri.getUnit());

                    if (nutrition != null) {
                        sb.append(", nutritionPer100g={cal=")
                                .append(dec(nutrition.getCaloriesPer100()))
                                .append(", protein=").append(dec(nutrition.getProteinGPer100()))
                                .append(", carb=").append(dec(nutrition.getCarbGPer100()))
                                .append(", fat=").append(dec(nutrition.getFatGPer100()))
                                .append("}");
                    }
                    sb.append('\n');
                }
            }
        }

        return sb.toString();
    }

    private List<EvaluationResult> parseAiResponse(String aiRaw, List<Recipe> recipes) {
        try {
            String json = extractJson(aiRaw);
            JsonNode root = objectMapper.readTree(json);
            JsonNode evaluations = root.path("evaluations");
            if (!evaluations.isArray()) {
                throw new BusinessException("AI response does not contain 'evaluations' array", HttpStatus.BAD_GATEWAY);
            }

            Set<String> validRecipeIds = recipes.stream().map(Recipe::getId).collect(Collectors.toSet());
            List<EvaluationResult> result = new ArrayList<>();
            for (JsonNode node : evaluations) {
                String recipeId = node.path("recipeId").asText("");
                if (!validRecipeIds.contains(recipeId)) {
                    continue;
                }

                int score = node.path("score").asInt(0);
                boolean suitable = node.path("suitable").asBoolean(score >= 60);
                String reason = node.path("reason").asText("AI chưa cung cấp lý do.");
                String suggestion = node.path("suggestion").asText("Chưa có đề xuất khẩu phần cho từng thành viên.");

                result.add(new EvaluationResult(recipeId, score, suitable, reason, suggestion));
            }

            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to parse AI response. Raw preview: {}", shorten(aiRaw, 500));
            throw new BusinessException("Failed to parse AI response: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    private String extractJson(String aiRaw) {
        if (aiRaw == null || aiRaw.isBlank()) {
            throw new BusinessException("AI response is empty", HttpStatus.BAD_GATEWAY);
        }

        String content = aiRaw.trim();
        if (content.startsWith("```")) {
            content = content.replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "")
                    .replaceFirst("\\s*```$", "");
        }

        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new BusinessException("AI response is not valid JSON", HttpStatus.BAD_GATEWAY);
        }
        return content.substring(start, end + 1);
    }

    private DishRecommendationResponse toResponse(DishRecommendation recommendation) {
        return DishRecommendationResponse.builder()
                .recipeId(recommendation.getRecipe().getId())
                .recipeName(recommendation.getRecipe().getName())
                .score(recommendation.getScore())
                .suitable(recommendation.getSuitable())
                .reason(recommendation.getReason())
                .suggestion(recommendation.getSuggestion())
                .build();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String dec(BigDecimal value) {
        return value == null ? "null" : value.stripTrailingZeros().toPlainString();
    }

    private String shorten(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars) + "...";
    }

    private record EvaluationResult(String recipeId, int score, boolean suitable, String reason, String suggestion) {
    }
}
