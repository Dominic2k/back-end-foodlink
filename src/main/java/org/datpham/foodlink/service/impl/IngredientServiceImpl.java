package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.IngredientRequest;
import org.datpham.foodlink.dto.request.IngredientReceiveStockRequest;
import org.datpham.foodlink.dto.response.IngredientResponse;
import org.datpham.foodlink.entity.Ingredient;
import org.datpham.foodlink.entity.IngredientNutrition;
import org.datpham.foodlink.enums.IngredientExpirationStatus;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.service.IngredientService;
import org.datpham.foodlink.specification.IngredientSpecification;
import org.datpham.foodlink.util.DateUtils;
import org.datpham.foodlink.util.IngredientUnitSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;

    @Value("${app.ingredient.expiring-soon-days:7}")
    private long expiringSoonDays;

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
        String ingredientName = request.getName().trim();
        ensureUniqueName(ingredientName, null);

        validateExpirationAndReceivedDates(request.getExpirationDate(), request.getReceivedDate());

        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientName);
        ingredient.setCategory(request.getCategory());
        ingredient.setBaseUnit(IngredientUnitSupport.normalizeUnit(request.getBaseUnit()));
        ingredient.setPricePerBaseUnit(request.getPricePerBaseUnit());
        ingredient.setStockQuantityBase(request.getStockQuantityBase() != null ? request.getStockQuantityBase() : BigDecimal.ZERO);
        ingredient.setImageUrl(request.getImageUrl());
        ingredient.setExpirationDate(request.getExpirationDate());
        ingredient.setReceivedDate(request.getReceivedDate());
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

        String ingredientName = request.getName().trim();
        ensureUniqueName(ingredientName, id);

        validateExpirationAndReceivedDates(request.getExpirationDate(), request.getReceivedDate());

        ingredient.setName(ingredientName);
        ingredient.setCategory(request.getCategory());
        ingredient.setBaseUnit(IngredientUnitSupport.normalizeUnit(request.getBaseUnit()));
        ingredient.setPricePerBaseUnit(request.getPricePerBaseUnit());
        if (request.getStockQuantityBase() != null) {
            ingredient.setStockQuantityBase(request.getStockQuantityBase());
        }
        ingredient.setImageUrl(request.getImageUrl());
        ingredient.setExpirationDate(request.getExpirationDate());
        if (request.getReceivedDate() != null) {
            ingredient.setReceivedDate(request.getReceivedDate());
        }
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
    public IngredientResponse receiveStock(String id, IngredientReceiveStockRequest request) {
        Ingredient ingredient = ingredientRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Ingredient not found", HttpStatus.NOT_FOUND));

        LocalDate receivedDate = request.getReceivedDate() != null ? request.getReceivedDate() : DateUtils.today();
        LocalDate expirationDate = ingredient.getExpirationDate();
        if (expirationDate != null && expirationDate.isBefore(receivedDate)) {
            throw new BusinessException(
                    "Cannot receive expired ingredient '" + ingredient.getName() + "' (expirationDate=" + expirationDate + ")",
                    HttpStatus.BAD_REQUEST
            );
        }

        BigDecimal currentStock = ingredient.getStockQuantityBase() != null ? ingredient.getStockQuantityBase() : BigDecimal.ZERO;
        BigDecimal quantity = request.getQuantityBase() != null ? request.getQuantityBase() : BigDecimal.ZERO;
        ingredient.setStockQuantityBase(scaleQuantity(currentStock.add(quantity)));
        ingredient.setReceivedDate(receivedDate);

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

    private void ensureUniqueName(String name, String currentId) {
        ingredientRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessException("Ingredient name '" + name + "' already exists", HttpStatus.CONFLICT);
            }
        });
    }

    private IngredientResponse toResponse(Ingredient ingredient) {
        IngredientNutrition n = ingredient.getNutrition();
        LocalDate expirationDate = ingredient.getExpirationDate();
        return IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .category(ingredient.getCategory())
                .baseUnit(ingredient.getBaseUnit())
                .pricePerBaseUnit(ingredient.getPricePerBaseUnit())
                .stockQuantityBase(ingredient.getStockQuantityBase())
                .imageUrl(ingredient.getImageUrl())
                .expirationDate(expirationDate)
                .receivedDate(ingredient.getReceivedDate())
                .expirationStatus(calculateExpirationStatus(expirationDate))
                .isActive(ingredient.getIsActive())
                .createdAt(ingredient.getCreatedAt())
                .updatedAt(ingredient.getUpdatedAt())
                .caloriesPer100(n != null ? n.getCaloriesPer100() : null)
                .proteinGPer100(n != null ? n.getProteinGPer100() : null)
                .carbGPer100(n != null ? n.getCarbGPer100() : null)
                .fatGPer100(n != null ? n.getFatGPer100() : null)
                .build();
    }

    private void validateExpirationAndReceivedDates(LocalDate expirationDate, LocalDate receivedDate) {
        if (expirationDate != null && receivedDate != null && expirationDate.isBefore(receivedDate)) {
            throw new BusinessException("expirationDate must be on/after receivedDate", HttpStatus.BAD_REQUEST);
        }
    }

    private IngredientExpirationStatus calculateExpirationStatus(LocalDate expirationDate) {
        if (expirationDate == null) {
            return null;
        }

        LocalDate today = DateUtils.today();
        if (expirationDate.isBefore(today)) {
            return IngredientExpirationStatus.expired;
        }

        LocalDate expiringSoonCutoff = today.plusDays(expiringSoonDays);
        if (!expirationDate.isAfter(expiringSoonCutoff)) {
            return IngredientExpirationStatus.expiringSoon;
        }

        return IngredientExpirationStatus.valid;
    }

    private BigDecimal scaleQuantity(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
