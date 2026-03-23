package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.DishCategoryRequest;
import org.datpham.foodlink.dto.response.DishCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DishCategoryService {
    Page<DishCategoryResponse> getAllCategories(String search, Pageable pageable);
    List<DishCategoryResponse> getAllActiveCategories();
    DishCategoryResponse createCategory(DishCategoryRequest request);
    DishCategoryResponse updateCategory(String id, DishCategoryRequest request);
    DishCategoryResponse deactivateCategory(String id);
}
