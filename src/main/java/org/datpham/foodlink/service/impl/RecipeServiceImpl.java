package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.RecipeRequest;
import org.datpham.foodlink.dto.response.RecipeResponse;
import org.datpham.foodlink.entity.Ingredient;
import org.datpham.foodlink.entity.Recipe;
import org.datpham.foodlink.entity.RecipeIngredient;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.repository.RecipeRepository;
import org.datpham.foodlink.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    @Override
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
    public RecipeResponse getRecipeById(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Recipe not found", HttpStatus.NOT_FOUND));
        return toResponse(recipe);
    }

    @Override
    @Transactional
    public RecipeResponse createRecipe(RecipeRequest request) {
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

        // Add ingredients
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            List<RecipeIngredient> riList = new ArrayList<>();
            for (RecipeRequest.RecipeIngredientItem item : request.getIngredients()) {
                RecipeIngredient ri = new RecipeIngredient();
                ri.setRecipeId(savedRecipe.getId());
                ri.setIngredientId(item.getIngredientId());
                ri.setQuantity(item.getQuantity());
                ri.setUnit(item.getUnit());
                ri.setIsOptional(item.getIsOptional() != null ? item.getIsOptional() : false);
                riList.add(ri);
            }
            savedRecipe.setRecipeIngredients(riList);
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
                ri.setIngredientId(item.getIngredientId());
                ri.setQuantity(item.getQuantity());
                ri.setUnit(item.getUnit());
                ri.setIsOptional(item.getIsOptional() != null ? item.getIsOptional() : false);
                recipe.getRecipeIngredients().add(ri);
            }
        }

        Recipe saved = recipeRepository.save(recipe);
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

        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .instructions(recipe.getInstructions())
                .prepTimeMin(recipe.getPrepTimeMin())
                .cookTimeMin(recipe.getCookTimeMin())
                .baseServings(recipe.getBaseServings())
                .imageUrl(recipe.getImageUrl())
                .status(recipe.getStatus() != null ? recipe.getStatus().name() : "draft")
                .createdByEmail(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getEmail() : null)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .ingredients(ingredientItems)
                .build();
    }
}
