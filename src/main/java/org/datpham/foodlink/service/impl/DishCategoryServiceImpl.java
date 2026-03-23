package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.DishCategoryRequest;
import org.datpham.foodlink.dto.response.DishCategoryResponse;
import org.datpham.foodlink.entity.DishCategory;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.DishCategoryRepository;
import org.datpham.foodlink.service.DishCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DishCategoryServiceImpl implements DishCategoryService {

    private final DishCategoryRepository dishCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DishCategoryResponse> getAllCategories(String search, Pageable pageable) {
        Page<DishCategory> categories;
        if (search != null && !search.isBlank()) {
            categories = dishCategoryRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            categories = dishCategoryRepository.findAll(pageable);
        }
        return categories.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<DishCategoryResponse> getAllActiveCategories() {
        return dishCategoryRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DishCategoryResponse createCategory(DishCategoryRequest request) {
        ensureUniqueName(request.getName(), null);

        DishCategory category = new DishCategory();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        DishCategory saved = dishCategoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DishCategoryResponse updateCategory(String id, DishCategoryRequest request) {
        DishCategory category = dishCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Dish category not found", HttpStatus.NOT_FOUND));

        ensureUniqueName(request.getName(), id);

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        DishCategory saved = dishCategoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DishCategoryResponse deactivateCategory(String id) {
        DishCategory category = dishCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Dish category not found", HttpStatus.NOT_FOUND));

        category.setIsActive(false);
        DishCategory saved = dishCategoryRepository.save(category);
        return toResponse(saved);
    }

    private void ensureUniqueName(String name, String currentId) {
        dishCategoryRepository.findByNameIgnoreCase(name.trim()).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessException("Category name '" + name + "' already exists", HttpStatus.CONFLICT);
            }
        });
    }

    private DishCategoryResponse toResponse(DishCategory category) {
        return DishCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
