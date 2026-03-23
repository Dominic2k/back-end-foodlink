package org.datpham.foodlink.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.dto.response.DishRecommendationResponse;
import org.datpham.foodlink.dto.response.DishRatingSummaryResponse;
import org.datpham.foodlink.dto.response.DishReviewResponse;
import org.datpham.foodlink.dto.response.RecommendationIngredientDetailResponse;
import org.datpham.foodlink.dto.response.RecommendationFilterOptionsResponse;
import org.datpham.foodlink.dto.response.RecommendationNutritionSummaryResponse;
import org.datpham.foodlink.dto.response.RecommendationPageResponse;
import org.datpham.foodlink.entity.*;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.recommendation.RecommendationMetricContext;
import org.datpham.foodlink.recommendation.RecommendationMetricFactory;
import org.datpham.foodlink.repository.DishCategoryRepository;
import org.datpham.foodlink.repository.DishRecommendationRepository;
import org.datpham.foodlink.repository.FamilyMemberRepository;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.repository.OrderItemRepository;
import org.datpham.foodlink.repository.RecipeRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.service.DishRecommendationService;
import org.datpham.foodlink.service.GenaiService;
import org.datpham.foodlink.util.IngredientUnitSupport;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final DishCategoryRepository dishCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final OrderItemRepository orderItemRepository;
    private final GenaiService genaiService;
    private final ObjectMapper objectMapper;
    private final RecommendationMetricFactory recommendationMetricFactory;

    @Override
    @Transactional
    public List<DishRecommendationResponse> evaluateForCurrentUser() {
        User user = getCurrentUser();
        return evaluateForUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationFilterOptionsResponse getFilterOptionsForCurrentUser() {
        getCurrentUser();
        List<String> ingredientCategories = recipeRepository.findByStatus(Recipe.RecipeStatus.published).stream()
                .map(this::extractRecipeCategory)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();

        List<String> dishCategories = dishCategoryRepository.findByIsActiveTrue().stream()
                .map(DishCategory::getName)
                .distinct()
                .sorted()
                .toList();

        return RecommendationFilterOptionsResponse.builder()
                .ingredientCategories(ingredientCategories)
                .dishCategories(dishCategories)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DishRecommendationResponse getRecommendationDetailForCurrentUser(String recipeId) {
        User user = getCurrentUser();
        List<FamilyMember> members = familyMemberRepository.findAllByUserIdWithConditions(user.getId());
        Recipe recipe = recipeRepository.findById(recipeId)
                .filter(r -> r.getStatus() == Recipe.RecipeStatus.published)
                .orElseThrow(() -> new BusinessException("Recipe not found", HttpStatus.NOT_FOUND));

        DishRecommendationResponse response = dishRecommendationRepository.findByUser_IdAndRecipe_Id(user.getId(), recipeId)
                .map(record -> toResponse(record, true))
                .orElseGet(() -> toUnevaluatedResponse(recipe, true));

        DishRecommendationResponse ratedResponse = enrichWithRatings(response, user.getId());
        return withRecommendationScore(ratedResponse, recipe, members, ratedResponse.getRatingSummary());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationIngredientDetailResponse> aggregateIngredients(List<org.datpham.foodlink.dto.request.RecipeSelectionRequest> selections) {
        if (selections == null || selections.isEmpty()) {
            return List.of();
        }

        Map<String, RecommendationIngredientDetailResponse> aggregatedMap = new LinkedHashMap<>();

        for (org.datpham.foodlink.dto.request.RecipeSelectionRequest req : selections) {
            Recipe recipe = recipeRepository.findById(req.getRecipeId()).orElse(null);
            if (recipe == null || recipe.getRecipeIngredients() == null) {
                continue;
            }

            int multiplier = Math.max(1, req.getQuantity());

            for (RecipeIngredient ri : recipe.getRecipeIngredients()) {
                Ingredient ingredient = resolveIngredient(ri);
                if (ingredient == null) {
                    continue;
                }

                String ingredientId = ingredient.getId();
                BigDecimal baseQty = ri.getQuantity();
                BigDecimal addedQty = baseQty != null ? baseQty.multiply(BigDecimal.valueOf(multiplier)) : BigDecimal.ZERO;
                BigDecimal addedQtyBase = IngredientUnitSupport.convertToBaseQuantity(ingredient, addedQty, ri.getUnit());
                String responseUnit = ingredient.getBaseUnit() != null ? ingredient.getBaseUnit() : ri.getUnit();
                BigDecimal responseQty = addedQtyBase != null ? addedQtyBase : addedQty;

                IngredientNutrition nutrition = ingredient.getNutrition();
                BigDecimal qtyInBaseUnit = convertToNutritionBaseQuantity(ingredient, addedQty, ri.getUnit());
                BigDecimal nutritionFactor = qtyInBaseUnit != null
                        ? qtyInBaseUnit.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                        : null;

                BigDecimal addedPrice = calculateIngredientLinePrice(ingredient, addedQty, ri.getUnit());
                if (addedPrice != null) {
                    addedPrice = addedPrice.setScale(2, RoundingMode.HALF_UP);
                }

                BigDecimal addedCalories = multiplyNutrition(nutrition == null ? null : nutrition.getCaloriesPer100(), nutritionFactor);
                BigDecimal addedProtein = multiplyNutrition(nutrition == null ? null : nutrition.getProteinGPer100(), nutritionFactor);
                BigDecimal addedCarb = multiplyNutrition(nutrition == null ? null : nutrition.getCarbGPer100(), nutritionFactor);
                BigDecimal addedFat = multiplyNutrition(nutrition == null ? null : nutrition.getFatGPer100(), nutritionFactor);

                if (aggregatedMap.containsKey(ingredientId)) {
                    RecommendationIngredientDetailResponse existing = aggregatedMap.get(ingredientId);

                    BigDecimal newQty = zeroIfNull(existing.getQuantity()).add(zeroIfNull(responseQty));
                    BigDecimal newTotalPrice = (existing.getTotalPrice() != null && addedPrice != null)
                            ? existing.getTotalPrice().add(addedPrice)
                            : (existing.getTotalPrice() != null ? existing.getTotalPrice() : addedPrice);

                    BigDecimal newCal = (existing.getCalories() != null && addedCalories != null)
                            ? existing.getCalories().add(addedCalories) : existing.getCalories();
                    BigDecimal newProtein = (existing.getProtein() != null && addedProtein != null)
                            ? existing.getProtein().add(addedProtein) : existing.getProtein();
                    BigDecimal newCarb = (existing.getCarb() != null && addedCarb != null)
                            ? existing.getCarb().add(addedCarb) : existing.getCarb();
                    BigDecimal newFat = (existing.getFat() != null && addedFat != null)
                            ? existing.getFat().add(addedFat) : existing.getFat();

                    aggregatedMap.put(ingredientId, RecommendationIngredientDetailResponse.builder()
                            .ingredientId(existing.getIngredientId())
                            .ingredientName(existing.getIngredientName())
                            .category(existing.getCategory())
                            .quantity(newQty)
                            .unit(existing.getUnit())
                            .pricePerBaseUnit(existing.getPricePerBaseUnit())
                            .totalPrice(newTotalPrice)
                            .calories(newCal)
                            .protein(newProtein)
                            .carb(newCarb)
                            .fat(newFat)
                            .optional(existing.getOptional())
                            .build());
                } else {
                    aggregatedMap.put(ingredientId, RecommendationIngredientDetailResponse.builder()
                            .ingredientId(ingredientId)
                            .ingredientName(ingredient.getName())
                            .category(ingredient.getCategory())
                            .quantity(responseQty)
                            .unit(responseUnit)
                            .pricePerBaseUnit(ingredient.getPricePerBaseUnit())
                            .totalPrice(addedPrice)
                            .calories(addedCalories)
                            .protein(addedProtein)
                            .carb(addedCarb)
                            .fat(addedFat)
                            .optional(Boolean.TRUE.equals(ri.getIsOptional()))
                            .build());
                }
            }
        }

        return new ArrayList<>(aggregatedMap.values());
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationPageResponse getRecommendationsForCurrentUser(
            int page,
            int size,
            String suitable,
            String evaluated,
            Integer scoreMin,
            Integer scoreMax,
            String q,
            String ingredientCategory,
            String dishCategory
    ) {
        User user = getCurrentUser();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int minScore = scoreMin == null ? 0 : Math.max(0, Math.min(100, scoreMin));
        int maxScore = scoreMax == null ? 100 : Math.max(0, Math.min(100, scoreMax));
        String normalizedSuitable = suitable == null ? "all" : suitable.trim().toLowerCase();
        String normalizedEvaluated = evaluated == null ? "all" : evaluated.trim().toLowerCase();
        String keyword = q == null ? "" : q.trim().toLowerCase();
        String normalizedIngredientCategory = ingredientCategory == null ? "" : ingredientCategory.trim().toLowerCase();
        String normalizedDishCategory = dishCategory == null ? "" : dishCategory.trim().toLowerCase();
        List<FamilyMember> members = familyMemberRepository.findAllByUserIdWithConditions(user.getId());

        List<Recipe> publishedRecipes = recipeRepository.findByStatus(Recipe.RecipeStatus.published);
        Map<String, DishRecommendation> recommendationMap = dishRecommendationRepository
                .findAllByUser_IdOrderByScoreDesc(user.getId())
                .stream()
                .collect(Collectors.toMap(r -> r.getRecipe().getId(), r -> r, (left, right) -> left));
        Map<String, DishRatingSummaryResponse> ratingSummaryMap = buildRatingSummaryMap(
                publishedRecipes.stream().map(Recipe::getId).toList()
        );

        List<DishRecommendationResponse> merged = publishedRecipes.stream()
                .map(recipe -> {
                    DishRecommendation recommendation = recommendationMap.get(recipe.getId());
                    DishRecommendationResponse response = recommendation != null
                            ? toResponse(recommendation, false)
                            : toUnevaluatedResponse(recipe, false);
                    DishRecommendationResponse withRatings = withRatingSummary(response, ratingSummaryMap.get(recipe.getId()));
                    return withRecommendationScore(withRatings, recipe, members, withRatings.getRatingSummary());
                })
                .filter(item -> matchEvaluatedFilter(item, normalizedEvaluated))
                .filter(item -> matchSuitableFilter(item, normalizedSuitable))
                .filter(item -> matchScoreRange(item, minScore, maxScore))
                .filter(item -> matchKeyword(item, keyword))
                .filter(item -> matchIngredientCategory(item, normalizedIngredientCategory))
                .filter(item -> matchDishCategory(item, normalizedDishCategory))
                .sorted(
                        Comparator.comparing((DishRecommendationResponse r) -> Boolean.TRUE.equals(r.getEvaluated())).reversed()
                                .thenComparing(r -> r.getScore() == null ? 0 : r.getScore(), Comparator.reverseOrder())
                                .thenComparing(DishRecommendationResponse::getRecipeName, String.CASE_INSENSITIVE_ORDER)
                )
                .toList();

        int total = merged.size();
        int fromIndex = Math.min(safePage * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<DishRecommendationResponse> items = merged.subList(fromIndex, toIndex);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        boolean hasNext = toIndex < total;

        return RecommendationPageResponse.builder()
                .items(items)
                .page(safePage)
                .size(safeSize)
                .totalItems(total)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
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
        Map<String, DishRatingSummaryResponse> ratingSummaryMap = buildRatingSummaryMap(
                recipes.stream().map(Recipe::getId).toList()
        );

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
                .map(record -> withRecommendationScore(
                        toResponse(record, false),
                        record.getRecipe(),
                        members,
                        ratingSummaryMap.get(record.getRecipe().getId())
                ))
                .sorted(Comparator.comparing(DishRecommendationResponse::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
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

    private DishRecommendationResponse toResponse(DishRecommendation recommendation, boolean includeDetails) {
        List<RecommendationIngredientDetailResponse> ingredients = includeDetails
                ? buildIngredientDetails(recommendation.getRecipe())
                : null;
        RecommendationNutritionSummaryResponse nutritionSummary = includeDetails
                ? buildNutritionSummary(ingredients)
                : null;
        BigDecimal totalIngredientPrice = calculateTotalIngredientPrice(recommendation.getRecipe(), ingredients);
        BigDecimal pricePerServing = calculatePricePerServing(recommendation.getRecipe(), totalIngredientPrice);

        return DishRecommendationResponse.builder()
                .recipeId(recommendation.getRecipe().getId())
                .recipeName(recommendation.getRecipe().getName())
                .imageUrl(recommendation.getRecipe().getImageUrl())
                .recipeDescription(recommendation.getRecipe().getDescription())
                .recipeInstructions(recommendation.getRecipe().getInstructions())
                .prepTimeMin(recommendation.getRecipe().getPrepTimeMin())
                .cookTimeMin(recommendation.getRecipe().getCookTimeMin())
                .baseServings(recommendation.getRecipe().getBaseServings())
                .totalIngredientPrice(totalIngredientPrice)
                .pricePerServing(pricePerServing)
                .category(extractRecipeCategory(recommendation.getRecipe()))
                .dishCategories(recommendation.getRecipe().getCategories() != null
                        ? recommendation.getRecipe().getCategories().stream().map(DishCategory::getName).toList()
                        : List.of())
                .evaluated(true)
                .aiScore(recommendation.getScore())
                .score(recommendation.getScore())
                .suitable(recommendation.getSuitable())
                .reason(recommendation.getReason())
                .suggestion(recommendation.getSuggestion())
                .ratingSummary(null)
                .myRating(null)
                .reviews(includeDetails ? List.of() : null)
                .ingredients(ingredients)
                .nutritionSummary(nutritionSummary)
                .build();
    }

    private DishRecommendationResponse toUnevaluatedResponse(Recipe recipe, boolean includeDetails) {
        List<RecommendationIngredientDetailResponse> ingredients = includeDetails
                ? buildIngredientDetails(recipe)
                : null;
        RecommendationNutritionSummaryResponse nutritionSummary = includeDetails
                ? buildNutritionSummary(ingredients)
                : null;
        BigDecimal totalIngredientPrice = calculateTotalIngredientPrice(recipe, ingredients);
        BigDecimal pricePerServing = calculatePricePerServing(recipe, totalIngredientPrice);

        return DishRecommendationResponse.builder()
                .recipeId(recipe.getId())
                .recipeName(recipe.getName())
                .imageUrl(recipe.getImageUrl())
                .recipeDescription(recipe.getDescription())
                .recipeInstructions(recipe.getInstructions())
                .prepTimeMin(recipe.getPrepTimeMin())
                .cookTimeMin(recipe.getCookTimeMin())
                .baseServings(recipe.getBaseServings())
                .totalIngredientPrice(totalIngredientPrice)
                .pricePerServing(pricePerServing)
                .category(extractRecipeCategory(recipe))
                .dishCategories(recipe.getCategories() != null
                        ? recipe.getCategories().stream().map(DishCategory::getName).toList()
                        : List.of())
                .evaluated(false)
                .aiScore(0)
                .score(0)
                .suitable(false)
                .reason(null)
                .suggestion(null)
                .ratingSummary(null)
                .myRating(null)
                .reviews(includeDetails ? List.of() : null)
                .ingredients(ingredients)
                .nutritionSummary(nutritionSummary)
                .build();
    }

    private Map<String, DishRatingSummaryResponse> buildRatingSummaryMap(List<String> recipeIds) {
        if (recipeIds == null || recipeIds.isEmpty()) {
            return Map.of();
        }

        return orderItemRepository.findRatingSummariesByRecipeIds(recipeIds).stream()
                .collect(Collectors.toMap(
                        OrderItemRepository.RecipeRatingSummaryProjection::getRecipeId,
                        item -> DishRatingSummaryResponse.builder()
                                .averageRating(item.getAverageRating() == null
                                        ? null
                                        : BigDecimal.valueOf(item.getAverageRating()).setScale(1, RoundingMode.HALF_UP))
                                .totalRatings(item.getTotalRatings())
                                .build()
                ));
    }

    private DishRecommendationResponse withRatingSummary(DishRecommendationResponse response, DishRatingSummaryResponse ratingSummary) {
        return DishRecommendationResponse.builder()
                .recipeId(response.getRecipeId())
                .recipeName(response.getRecipeName())
                .imageUrl(response.getImageUrl())
                .recipeDescription(response.getRecipeDescription())
                .recipeInstructions(response.getRecipeInstructions())
                .prepTimeMin(response.getPrepTimeMin())
                .cookTimeMin(response.getCookTimeMin())
                .baseServings(response.getBaseServings())
                .totalIngredientPrice(response.getTotalIngredientPrice())
                .pricePerServing(response.getPricePerServing())
                .category(response.getCategory())
                .dishCategories(response.getDishCategories())
                .evaluated(response.getEvaluated())
                .aiScore(response.getAiScore())
                .score(response.getScore())
                .suitable(response.getSuitable())
                .reason(response.getReason())
                .suggestion(response.getSuggestion())
                .ratingSummary(ratingSummary != null
                        ? ratingSummary
                        : DishRatingSummaryResponse.builder().averageRating(null).totalRatings(0L).build())
                .myRating(response.getMyRating())
                .reviews(response.getReviews())
                .ingredients(response.getIngredients())
                .nutritionSummary(response.getNutritionSummary())
                .build();
    }

    private DishRecommendationResponse enrichWithRatings(DishRecommendationResponse response, String currentUserId) {
        List<DishReviewResponse> reviews = orderItemRepository.findReviewsByRecipeId(response.getRecipeId()).stream()
                .map(row -> DishReviewResponse.builder()
                        .userId(row.getUserId())
                        .userFullName(row.getUserFullName())
                        .rating(row.getRating())
                        .comment(row.getComment())
                        .ratedAt(row.getRatedAt())
                        .mine(Objects.equals(currentUserId, row.getUserId()))
                        .build())
                .toList();

        DishReviewResponse myRating = reviews.stream()
                .filter(review -> Boolean.TRUE.equals(review.getMine()))
                .findFirst()
                .orElse(null);

        DishRatingSummaryResponse ratingSummary = reviews.isEmpty()
                ? DishRatingSummaryResponse.builder().averageRating(null).totalRatings(0L).build()
                : DishRatingSummaryResponse.builder()
                        .averageRating(BigDecimal.valueOf(
                                reviews.stream().mapToInt(DishReviewResponse::getRating).average().orElse(0)
                        ).setScale(1, RoundingMode.HALF_UP))
                        .totalRatings((long) reviews.size())
                        .build();

        return DishRecommendationResponse.builder()
                .recipeId(response.getRecipeId())
                .recipeName(response.getRecipeName())
                .imageUrl(response.getImageUrl())
                .recipeDescription(response.getRecipeDescription())
                .recipeInstructions(response.getRecipeInstructions())
                .prepTimeMin(response.getPrepTimeMin())
                .cookTimeMin(response.getCookTimeMin())
                .baseServings(response.getBaseServings())
                .totalIngredientPrice(response.getTotalIngredientPrice())
                .pricePerServing(response.getPricePerServing())
                .category(response.getCategory())
                .dishCategories(response.getDishCategories())
                .evaluated(response.getEvaluated())
                .aiScore(response.getAiScore())
                .score(response.getScore())
                .suitable(response.getSuitable())
                .reason(response.getReason())
                .suggestion(response.getSuggestion())
                .ratingSummary(ratingSummary)
                .myRating(myRating)
                .reviews(reviews)
                .ingredients(response.getIngredients())
                .nutritionSummary(response.getNutritionSummary())
                .build();
    }

    private List<RecommendationIngredientDetailResponse> buildIngredientDetails(Recipe recipe) {
        if (recipe.getRecipeIngredients() == null || recipe.getRecipeIngredients().isEmpty()) {
            return List.of();
        }

        return recipe.getRecipeIngredients().stream()
                .map(ri -> {
                    Ingredient ingredient = resolveIngredient(ri);
                    IngredientNutrition nutrition = ingredient != null ? ingredient.getNutrition() : null;
                    BigDecimal quantityInBaseUnit = convertToNutritionBaseQuantity(ingredient, ri.getQuantity(), ri.getUnit());
                    BigDecimal quantityBase = IngredientUnitSupport.convertToBaseQuantity(ingredient, ri.getQuantity(), ri.getUnit());
                    BigDecimal factor = quantityInBaseUnit == null
                            ? null
                            : quantityInBaseUnit.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                    BigDecimal linePrice = calculateIngredientLinePrice(ingredient, ri.getQuantity(), ri.getUnit());

                    return RecommendationIngredientDetailResponse.builder()
                            .ingredientId(ingredient != null ? ingredient.getId() : null)
                            .ingredientName(ingredient != null ? ingredient.getName() : "unknown")
                            .category(ingredient != null ? ingredient.getCategory() : null)
                            .quantity(quantityBase != null ? quantityBase : ri.getQuantity())
                            .unit(ingredient != null && ingredient.getBaseUnit() != null ? ingredient.getBaseUnit() : ri.getUnit())
                            .pricePerBaseUnit(ingredient != null ? ingredient.getPricePerBaseUnit() : null)
                            .totalPrice(linePrice != null ? linePrice.setScale(2, RoundingMode.HALF_UP) : null)
                            .optional(Boolean.TRUE.equals(ri.getIsOptional()))
                            .calories(multiplyNutrition(nutrition == null ? null : nutrition.getCaloriesPer100(), factor))
                            .protein(multiplyNutrition(nutrition == null ? null : nutrition.getProteinGPer100(), factor))
                            .carb(multiplyNutrition(nutrition == null ? null : nutrition.getCarbGPer100(), factor))
                            .fat(multiplyNutrition(nutrition == null ? null : nutrition.getFatGPer100(), factor))
                            .build();
                })
                .toList();
    }

    private RecommendationNutritionSummaryResponse buildNutritionSummary(List<RecommendationIngredientDetailResponse> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return RecommendationNutritionSummaryResponse.builder()
                    .calories(BigDecimal.ZERO)
                    .protein(BigDecimal.ZERO)
                    .carb(BigDecimal.ZERO)
                    .fat(BigDecimal.ZERO)
                    .coveredIngredients(0)
                    .totalIngredients(0)
                    .build();
        }

        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carb = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        int covered = 0;

        for (RecommendationIngredientDetailResponse item : ingredients) {
            if (item.getCalories() != null || item.getProtein() != null || item.getCarb() != null || item.getFat() != null) {
                covered++;
            }

            calories = calories.add(zeroIfNull(item.getCalories()));
            protein = protein.add(zeroIfNull(item.getProtein()));
            carb = carb.add(zeroIfNull(item.getCarb()));
            fat = fat.add(zeroIfNull(item.getFat()));
        }

        return RecommendationNutritionSummaryResponse.builder()
                .calories(scale2(calories))
                .protein(scale2(protein))
                .carb(scale2(carb))
                .fat(scale2(fat))
                .coveredIngredients(covered)
                .totalIngredients(ingredients.size())
                .build();
    }

    private int calculateCompositeScore(
            Recipe recipe,
            List<FamilyMember> members,
            Integer aiScore,
            DishRatingSummaryResponse ratingSummary,
            boolean includeAiMetric
    ) {
        RecommendationNutritionSummaryResponse nutritionSummary = buildNutritionSummary(buildIngredientDetails(recipe));
        RecommendationMetricContext context = new RecommendationMetricContext(
                recipe,
                members == null ? List.of() : members,
                aiScore == null ? 0 : aiScore,
                ratingSummary != null ? ratingSummary : DishRatingSummaryResponse.builder().averageRating(null).totalRatings(0L).build(),
                nutritionSummary
        );

        List<org.datpham.foodlink.recommendation.WeightedRecommendationMetric> activeMetrics = recommendationMetricFactory.createBaseMetrics().stream()
                .filter(metric -> includeAiMetric || !"aiSuitability".equals(metric.metric().key()))
                .toList();

        BigDecimal totalWeight = activeMetrics.stream()
                .map(org.datpham.foodlink.recommendation.WeightedRecommendationMetric::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (activeMetrics.isEmpty() || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal weightedScore = activeMetrics.stream()
                .map(metric -> metric.weight().multiply(metric.metric().evaluate(context)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal normalizedScore = weightedScore.divide(totalWeight, 4, RoundingMode.HALF_UP);

        BigDecimal safetyPenalty = recommendationMetricFactory.createSafetyMetric().evaluate(context);
        BigDecimal finalScore = normalizedScore.multiply(safetyPenalty)
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO)
                .min(new BigDecimal("100"));

        if (log.isDebugEnabled()) {
            Map<String, BigDecimal> metricBreakdown = activeMetrics.stream()
                    .collect(Collectors.toMap(
                            metric -> metric.metric().key(),
                            metric -> metric.metric().evaluate(context),
                            (left, right) -> right,
                            LinkedHashMap::new
                    ));
            metricBreakdown.put(recommendationMetricFactory.createSafetyMetric().key(), safetyPenalty);
            log.debug("Recommendation metrics for recipe {}: aiScore={}, includeAiMetric={}, metrics={}, finalScore={}",
                    recipe.getId(), aiScore, includeAiMetric, metricBreakdown, finalScore);
        }

        return finalScore.intValue();
    }

    private DishRecommendationResponse withRecommendationScore(
            DishRecommendationResponse response,
            Recipe recipe,
            List<FamilyMember> members,
            DishRatingSummaryResponse ratingSummary
    ) {
        int aiScore = response.getAiScore() == null ? 0 : response.getAiScore();
        int recommendationScore = calculateCompositeScore(
                recipe,
                members,
                response.getAiScore(),
                ratingSummary,
                Boolean.TRUE.equals(response.getEvaluated())
        );

        return DishRecommendationResponse.builder()
                .recipeId(response.getRecipeId())
                .recipeName(response.getRecipeName())
                .imageUrl(response.getImageUrl())
                .recipeDescription(response.getRecipeDescription())
                .recipeInstructions(response.getRecipeInstructions())
                .prepTimeMin(response.getPrepTimeMin())
                .cookTimeMin(response.getCookTimeMin())
                .baseServings(response.getBaseServings())
                .totalIngredientPrice(response.getTotalIngredientPrice())
                .pricePerServing(response.getPricePerServing())
                .category(response.getCategory())
                .dishCategories(response.getDishCategories())
                .evaluated(response.getEvaluated())
                .aiScore(aiScore)
                .score(recommendationScore)
                .suitable(response.getSuitable())
                .reason(response.getReason())
                .suggestion(response.getSuggestion())
                .ratingSummary(response.getRatingSummary())
                .myRating(response.getMyRating())
                .reviews(response.getReviews())
                .ingredients(response.getIngredients())
                .nutritionSummary(response.getNutritionSummary())
                .build();
    }

    private BigDecimal calculateTotalIngredientPrice(Recipe recipe, List<RecommendationIngredientDetailResponse> ingredients) {
        if (ingredients != null && !ingredients.isEmpty()) {
            return ingredients.stream()
                    .map(RecommendationIngredientDetailResponse::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (recipe == null || recipe.getRecipeIngredients() == null) {
            return null;
        }

        return recipe.getRecipeIngredients().stream()
                .map(ri -> {
                    Ingredient ingredient = resolveIngredient(ri);
                    BigDecimal linePrice = calculateIngredientLinePrice(ingredient, ri.getQuantity(), ri.getUnit());
                    if (linePrice == null) {
                        return BigDecimal.ZERO;
                    }
                    return linePrice;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Ingredient resolveIngredient(RecipeIngredient ri) {
        try {
            Ingredient ingredient = ri.getIngredient();
            if (ingredient != null) return ingredient;
        } catch (Exception ignored) {
        }
        if (ri.getIngredientId() == null) return null;
        return ingredientRepository.findById(ri.getIngredientId()).orElse(null);
    }

    private BigDecimal calculateIngredientLinePrice(Ingredient ingredient, BigDecimal quantity, String quantityUnit) {
        return IngredientUnitSupport.calculateLinePriceFromRequestUnit(ingredient, quantity, quantityUnit);
    }

    private BigDecimal calculatePricePerServing(Recipe recipe, BigDecimal totalIngredientPrice) {
        if (recipe == null || totalIngredientPrice == null) {
            return null;
        }

        int servings = recipe.getBaseServings() == null || recipe.getBaseServings() <= 0 ? 1 : recipe.getBaseServings();
        return totalIngredientPrice
                .divide(BigDecimal.valueOf(servings), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal convertToNutritionBaseQuantity(Ingredient ingredient, BigDecimal quantity, String unit) {
        if (ingredient == null) {
            return null;
        }

        BigDecimal quantityBase = IngredientUnitSupport.convertToBaseQuantity(ingredient, quantity, unit);
        if (quantityBase == null) {
            return null;
        }

        String baseUnit = IngredientUnitSupport.normalizeUnit(ingredient.getBaseUnit());
        if (IngredientUnitSupport.isWeightUnit(baseUnit) || IngredientUnitSupport.isVolumeUnit(baseUnit)) {
            return quantityBase;
        }

        return null;
    }

    private BigDecimal multiplyNutrition(BigDecimal valuePer100, BigDecimal factor) {
        if (valuePer100 == null || factor == null) {
            return null;
        }
        return scale2(valuePer100.multiply(factor));
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
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

    private boolean matchEvaluatedFilter(DishRecommendationResponse item, String evaluated) {
        return switch (evaluated) {
            case "evaluated", "true", "1" -> Boolean.TRUE.equals(item.getEvaluated());
            case "unevaluated", "false", "0" -> !Boolean.TRUE.equals(item.getEvaluated());
            default -> true;
        };
    }

    private boolean matchSuitableFilter(DishRecommendationResponse item, String suitable) {
        if (!Boolean.TRUE.equals(item.getEvaluated()) && ("suitable".equals(suitable) || "not_suitable".equals(suitable))) {
            return false;
        }
        return switch (suitable) {
            case "suitable", "true", "1" -> Boolean.TRUE.equals(item.getSuitable());
            case "not_suitable", "unsuitable", "false", "0" -> !Boolean.TRUE.equals(item.getSuitable());
            default -> true;
        };
    }

    private boolean matchScoreRange(DishRecommendationResponse item, int minScore, int maxScore) {
        if (!Boolean.TRUE.equals(item.getEvaluated())) {
            return true;
        }
        int score = item.getScore() == null ? 0 : item.getScore();
        return score >= Math.min(minScore, maxScore) && score <= Math.max(minScore, maxScore);
    }

    private boolean matchKeyword(DishRecommendationResponse item, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        String name = item.getRecipeName() == null ? "" : item.getRecipeName().toLowerCase();
        return name.contains(keyword);
    }

    private boolean matchIngredientCategory(DishRecommendationResponse item, String ingredientCategory) {
        if (ingredientCategory.isEmpty() || "all".equals(ingredientCategory)) {
            return true;
        }
        String itemCategory = item.getCategory() == null ? "" : item.getCategory().toLowerCase();
        return itemCategory.equals(ingredientCategory);
    }

    private boolean matchDishCategory(DishRecommendationResponse item, String dishCategory) {
        if (dishCategory.isEmpty() || "all".equals(dishCategory)) {
            return true;
        }
        
        if (item.getDishCategories() == null || item.getDishCategories().isEmpty()) {
            return false;
        }
        
        return item.getDishCategories().stream()
                .anyMatch(c -> c.toLowerCase().equals(dishCategory));
    }

    private String extractRecipeCategory(Recipe recipe) {
        if (recipe.getRecipeIngredients() == null || recipe.getRecipeIngredients().isEmpty()) {
            return "other";
        }

        Map<String, Long> categoryCount = recipe.getRecipeIngredients().stream()
                .map(RecipeIngredient::getIngredient)
                .filter(Objects::nonNull)
                .map(Ingredient::getCategory)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        if (categoryCount.isEmpty()) {
            return "other";
        }

        return categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("other");
    }

    private record EvaluationResult(String recipeId, int score, boolean suitable, String reason, String suggestion) {
    }
}
