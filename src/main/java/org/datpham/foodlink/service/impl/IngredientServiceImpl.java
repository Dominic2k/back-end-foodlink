package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.IngredientRequest;
import org.datpham.foodlink.dto.response.IngredientResponse;
import org.datpham.foodlink.entity.Ingredient;
import org.datpham.foodlink.entity.IngredientNutrition;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.service.IngredientService;
import org.datpham.foodlink.specification.IngredientSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;

    @Override
    public Page<IngredientResponse> getAllIngredients(String search, Boolean isActive, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Ingredient> spec = org.springframework.data.jpa.domain.Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(IngredientSpecification.nameOrCategoryContains(search));
        }
        if (isActive != null) {
            spec = spec.and(IngredientSpecification.hasIsActive(isActive));
        }

        Page<Ingredient> ingredients = ingredientRepository.findAll(spec, pageable);
        return ingredients.map(this::toResponse);
    }

    @Override
    public List<IngredientResponse> getAllActiveIngredients() {
        return ingredientRepository.findAllByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public IngredientResponse getIngredientById(String id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ingredient not found", HttpStatus.NOT_FOUND));
        return toResponse(ingredient);
    }

    @Override
    @Transactional
    public IngredientResponse createIngredient(IngredientRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setCategory(request.getCategory());
        ingredient.setDefaultUnit(request.getDefaultUnit());
        ingredient.setPrice(request.getPrice());
        ingredient.setImageUrl(request.getImageUrl());
        ingredient.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Nutrition
        if (hasNutrition(request)) {
            IngredientNutrition nutrition = new IngredientNutrition();
            nutrition.setIngredient(ingredient);
            nutrition.setCaloriesPer100(request.getCaloriesPer100());
            nutrition.setProteinGPer100(request.getProteinGPer100());
            nutrition.setCarbGPer100(request.getCarbGPer100());
            nutrition.setFatGPer100(request.getFatGPer100());
            ingredient.setNutrition(nutrition);
        }

        Ingredient saved = ingredientRepository.save(ingredient);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public IngredientResponse updateIngredient(String id, IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ingredient not found", HttpStatus.NOT_FOUND));

        ingredient.setName(request.getName());
        ingredient.setCategory(request.getCategory());
        ingredient.setDefaultUnit(request.getDefaultUnit());
        ingredient.setPrice(request.getPrice());
        ingredient.setImageUrl(request.getImageUrl());
        if (request.getIsActive() != null) {
            ingredient.setIsActive(request.getIsActive());
        }

        // Update nutrition
        if (hasNutrition(request)) {
            IngredientNutrition nutrition = ingredient.getNutrition();
            if (nutrition == null) {
                nutrition = new IngredientNutrition();
                nutrition.setIngredient(ingredient);
                ingredient.setNutrition(nutrition);
            }
            nutrition.setCaloriesPer100(request.getCaloriesPer100());
            nutrition.setProteinGPer100(request.getProteinGPer100());
            nutrition.setCarbGPer100(request.getCarbGPer100());
            nutrition.setFatGPer100(request.getFatGPer100());
        }

        Ingredient saved = ingredientRepository.save(ingredient);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteIngredient(String id) {
        if (!ingredientRepository.existsById(id)) {
            throw new BusinessException("Ingredient not found", HttpStatus.NOT_FOUND);
        }
        ingredientRepository.deleteById(id);
    }

    private boolean hasNutrition(IngredientRequest request) {
        return request.getCaloriesPer100() != null
                || request.getProteinGPer100() != null
                || request.getCarbGPer100() != null
                || request.getFatGPer100() != null;
    }

    private IngredientResponse toResponse(Ingredient ingredient) {
        IngredientNutrition n = ingredient.getNutrition();
        return IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .category(ingredient.getCategory())
                .defaultUnit(ingredient.getDefaultUnit())
                .price(ingredient.getPrice())
                .imageUrl(ingredient.getImageUrl())
                .isActive(ingredient.getIsActive())
                .createdAt(ingredient.getCreatedAt())
                .updatedAt(ingredient.getUpdatedAt())
                .caloriesPer100(n != null ? n.getCaloriesPer100() : null)
                .proteinGPer100(n != null ? n.getProteinGPer100() : null)
                .carbGPer100(n != null ? n.getCarbGPer100() : null)
                .fatGPer100(n != null ? n.getFatGPer100() : null)
                .build();
    }
}
