package org.datpham.foodlink.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.RecipeRequest;
import org.datpham.foodlink.dto.response.RecipeResponse;
import org.datpham.foodlink.entity.DishCategory;
import org.datpham.foodlink.entity.Ingredient;
import org.datpham.foodlink.entity.Recipe;
import org.datpham.foodlink.entity.RecipeIngredient;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.DishCategoryRepository;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.repository.RecipeRepository;
import org.datpham.foodlink.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final DishCategoryRepository dishCategoryRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<RecipeResponse> getAllRecipes(String search, String status, Pageable pageable) {
        Page<Recipe> recipes;

        Recipe.RecipeStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = Recipe.RecipeStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
            }
        }

        boolean hasSearch = search != null && !search.isBlank();

        if (hasSearch && statusEnum != null) {
            recipes = recipeRepository.findByNameContainingIgnoreCaseAndStatus(search, statusEnum, pageable);
        } else if (hasSearch) {
            recipes = recipeRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (statusEnum != null) {
            recipes = recipeRepository.findByStatus(statusEnum, pageable);
        } else {
            recipes = recipeRepository.findAll(pageable);
        }

        return recipes.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Recipe not found", HttpStatus.NOT_FOUND));
        return toResponse(recipe);
    }

    @Override
    @Transactional
    public RecipeResponse createRecipe(RecipeRequest request) {
        // Resolve all ingredient IDs first (may create new ingredients)
        List<ResolvedIngredient> resolvedIngredients = new ArrayList<>();
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            for (RecipeRequest.RecipeIngredientItem item : request.getIngredients()) {
                resolvedIngredients.add(new ResolvedIngredient(
                        resolveIngredientId(item),
                        item.getQuantity(),
                        item.getUnit(),
                        item.getIsOptional() != null ? item.getIsOptional() : false
                ));
            }
        }

        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setInstructions(request.getInstructions());
        recipe.setPrepTimeMin(request.getPrepTimeMin());
        recipe.setCookTimeMin(request.getCookTimeMin());
        recipe.setBaseServings(request.getBaseServings() != null ? request.getBaseServings() : 1);
        recipe.setImageUrl(request.getImageUrl());

        if (request.getStatus() != null) {
            try {
                recipe.setStatus(Recipe.RecipeStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                recipe.setStatus(Recipe.RecipeStatus.draft);
            }
        }

        // Save first to get ID
        Recipe savedRecipe = recipeRepository.save(recipe);

        // Add ingredients to the managed list
        for (ResolvedIngredient ri : resolvedIngredients) {
            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setRecipeId(savedRecipe.getId());
            recipeIngredient.setIngredientId(ri.ingredientId);
            recipeIngredient.setQuantity(ri.quantity);
            recipeIngredient.setUnit(ri.unit);
            recipeIngredient.setIsOptional(ri.isOptional);
            savedRecipe.getRecipeIngredients().add(recipeIngredient);
        }

        if (!resolvedIngredients.isEmpty()) {
            savedRecipe = recipeRepository.save(savedRecipe);
        }

        // Set categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<DishCategory> categories = dishCategoryRepository.findAllByIdIn(request.getCategoryIds());
            savedRecipe.setCategories(categories);
            savedRecipe = recipeRepository.save(savedRecipe);
        }

        return toResponse(savedRecipe);
    }

    @Override
    @Transactional
    public RecipeResponse updateRecipe(String id, RecipeRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Recipe not found", HttpStatus.NOT_FOUND));

        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setInstructions(request.getInstructions());
        recipe.setPrepTimeMin(request.getPrepTimeMin());
        recipe.setCookTimeMin(request.getCookTimeMin());
        if (request.getBaseServings() != null) recipe.setBaseServings(request.getBaseServings());
        recipe.setImageUrl(request.getImageUrl());

        if (request.getStatus() != null) {
            try {
                recipe.setStatus(Recipe.RecipeStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Update ingredients (clear and re-add)
        if (request.getIngredients() != null) {
            recipe.getRecipeIngredients().clear();
            for (RecipeRequest.RecipeIngredientItem item : request.getIngredients()) {
                RecipeIngredient ri = new RecipeIngredient();
                ri.setRecipeId(recipe.getId());
                ri.setIngredientId(resolveIngredientId(item));
                ri.setQuantity(item.getQuantity());
                ri.setUnit(item.getUnit());
                ri.setIsOptional(item.getIsOptional() != null ? item.getIsOptional() : false);
                recipe.getRecipeIngredients().add(ri);
            }
        }

        // Update categories — use native approach to avoid Hibernate collection conflicts
        Recipe saved = recipeRepository.save(recipe);

        if (request.getCategoryIds() != null) {
            recipeRepository.flush();
            entityManager.createNativeQuery("DELETE FROM recipe_categories WHERE recipe_id = :rid")
                    .setParameter("rid", saved.getId())
                    .executeUpdate();
            for (String catId : request.getCategoryIds()) {
                entityManager.createNativeQuery("INSERT INTO recipe_categories (recipe_id, category_id) VALUES (:rid, :cid)")
                        .setParameter("rid", saved.getId())
                        .setParameter("cid", catId)
                        .executeUpdate();
            }
            entityManager.flush();
            entityManager.clear();
            saved = recipeRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("Recipe not found", HttpStatus.NOT_FOUND));
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public RecipeResponse updateRecipeStatus(String id, String status) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Recipe not found", HttpStatus.NOT_FOUND));

        try {
            recipe.setStatus(Recipe.RecipeStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status, HttpStatus.BAD_REQUEST);
        }

        Recipe saved = recipeRepository.save(recipe);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRecipe(String id) {
        if (!recipeRepository.existsById(id)) {
            throw new BusinessException("Recipe not found", HttpStatus.NOT_FOUND);
        }
        recipeRepository.deleteById(id);
    }

    private RecipeResponse toResponse(Recipe recipe) {
        List<RecipeResponse.RecipeIngredientItem> ingredientItems = new ArrayList<>();
        if (recipe.getRecipeIngredients() != null) {
            ingredientItems = recipe.getRecipeIngredients().stream().map(ri -> {
                String ingredientName = "";
                try {
                    Ingredient ing = ri.getIngredient();
                    if (ing != null) ingredientName = ing.getName();
                } catch (Exception e) {
                    // lazy load failure - fetch manually
                    ingredientName = ingredientRepository.findById(ri.getIngredientId())
                            .map(Ingredient::getName).orElse("Unknown");
                }
                return RecipeResponse.RecipeIngredientItem.builder()
                        .ingredientId(ri.getIngredientId())
                        .ingredientName(ingredientName)
                        .quantity(ri.getQuantity())
                        .unit(ri.getUnit())
                        .isOptional(ri.getIsOptional())
                        .build();
            }).collect(Collectors.toList());
        }

        BigDecimal totalIngredientPrice = calculateTotalIngredientPrice(recipe);
        BigDecimal pricePerServing = calculatePricePerServing(recipe, totalIngredientPrice);

        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .instructions(recipe.getInstructions())
                .prepTimeMin(recipe.getPrepTimeMin())
                .cookTimeMin(recipe.getCookTimeMin())
                .baseServings(recipe.getBaseServings())
                .totalIngredientPrice(totalIngredientPrice)
                .pricePerServing(pricePerServing)
                .imageUrl(recipe.getImageUrl())
                .status(recipe.getStatus() != null ? recipe.getStatus().name() : "draft")
                .createdByEmail(getCreatedByEmail(recipe))
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .ingredients(ingredientItems)
                .categories(mapCategories(recipe))
                .build();
    }

    private BigDecimal calculateTotalIngredientPrice(Recipe recipe) {
        if (recipe.getRecipeIngredients() == null || recipe.getRecipeIngredients().isEmpty()) {
            return null;
        }

        BigDecimal total = recipe.getRecipeIngredients().stream()
                .map(ri -> {
                    Ingredient ingredient = resolveIngredient(ri);
                    BigDecimal linePrice = calculateIngredientLinePrice(ingredient, ri.getQuantity(), ri.getUnit());
                    if (linePrice == null) {
                        return BigDecimal.ZERO;
                    }
                    return linePrice;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePricePerServing(Recipe recipe, BigDecimal totalIngredientPrice) {
        if (totalIngredientPrice == null) {
            return null;
        }

        int servings = recipe.getBaseServings() == null || recipe.getBaseServings() <= 0 ? 1 : recipe.getBaseServings();
        return totalIngredientPrice.divide(BigDecimal.valueOf(servings), 0, RoundingMode.HALF_UP);
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
        if (ingredient == null || ingredient.getPrice() == null || quantity == null) {
            return null;
        }

        BigDecimal qtyInDefaultUnit = convertToDefaultUnit(quantity, quantityUnit, ingredient.getDefaultUnit());
        if (qtyInDefaultUnit == null) {
            return null;
        }

        return ingredient.getPrice().multiply(qtyInDefaultUnit);
    }

    private BigDecimal convertToDefaultUnit(BigDecimal quantity, String fromUnit, String toUnit) {
        if (quantity == null) return null;
        if (toUnit == null || toUnit.isBlank()) return quantity;

        String src = normalizeUnit(fromUnit);
        String dst = normalizeUnit(toUnit);

        if (src.equals(dst) || src.isEmpty()) {
            return quantity;
        }

        BigDecimal srcWeight = toGrams(quantity, src);
        BigDecimal dstWeightUnit = unitToGrams(dst);
        if (srcWeight != null && dstWeightUnit != null) {
            return srcWeight.divide(dstWeightUnit, 6, RoundingMode.HALF_UP);
        }

        BigDecimal srcVolume = toMilliliters(quantity, src);
        BigDecimal dstVolumeUnit = unitToMilliliters(dst);
        if (srcVolume != null && dstVolumeUnit != null) {
            return srcVolume.divide(dstVolumeUnit, 6, RoundingMode.HALF_UP);
        }

        return null;
    }

    private String normalizeUnit(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal toGrams(BigDecimal quantity, String unit) {
        BigDecimal unitFactor = unitToGrams(unit);
        if (unitFactor == null) return null;
        return quantity.multiply(unitFactor);
    }

    private BigDecimal unitToGrams(String unit) {
        return switch (unit) {
            case "g", "gram", "grams" -> BigDecimal.ONE;
            case "kg", "kilogram", "kilograms" -> new BigDecimal("1000");
            case "mg", "milligram", "milligrams" -> new BigDecimal("0.001");
            default -> null;
        };
    }

    private BigDecimal toMilliliters(BigDecimal quantity, String unit) {
        BigDecimal unitFactor = unitToMilliliters(unit);
        if (unitFactor == null) return null;
        return quantity.multiply(unitFactor);
    }

    private BigDecimal unitToMilliliters(String unit) {
        return switch (unit) {
            case "ml", "milliliter", "milliliters" -> BigDecimal.ONE;
            case "l", "liter", "liters" -> new BigDecimal("1000");
            default -> null;
        };
    }

    private String getCreatedByEmail(Recipe recipe) {
        try {
            User user = recipe.getCreatedBy();
            return user != null ? user.getEmail() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private List<RecipeResponse.CategoryItem> mapCategories(Recipe recipe) {
        try {
            if (recipe.getCategories() == null) return List.of();
            return recipe.getCategories().stream()
                    .map(c -> RecipeResponse.CategoryItem.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Resolves an ingredient ID from the request item.
     * If ingredientId is provided, validates it exists.
     * If only ingredientName is provided, finds by name or creates a new ingredient.
     */
    private String resolveIngredientId(RecipeRequest.RecipeIngredientItem item) {
        if (item.getIngredientId() != null && !item.getIngredientId().isBlank()) {
            ingredientRepository.findById(item.getIngredientId())
                    .orElseThrow(() -> new BusinessException(
                            "Ingredient not found: " + item.getIngredientId(), HttpStatus.NOT_FOUND));
            return item.getIngredientId();
        }
        if (item.getIngredientName() != null && !item.getIngredientName().isBlank()) {
            return ingredientRepository.findByNameIgnoreCase(item.getIngredientName().trim())
                    .map(Ingredient::getId)
                    .orElseGet(() -> {
                        Ingredient newIng = new Ingredient();
                        newIng.setName(item.getIngredientName().trim());
                        return ingredientRepository.save(newIng).getId();
                    });
        }
        throw new BusinessException("ingredientId or ingredientName is required", HttpStatus.BAD_REQUEST);
    }

    /**
     * Simple holder for pre-resolved ingredient data.
     */
    private record ResolvedIngredient(String ingredientId, java.math.BigDecimal quantity, String unit, boolean isOptional) {}
}
