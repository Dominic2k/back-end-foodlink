package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.IngredientRequest;
import org.datpham.foodlink.dto.response.IngredientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IngredientService {
    Page<IngredientResponse> getAllIngredients(String search, Pageable pageable);
    IngredientResponse getIngredientById(String id);
    IngredientResponse createIngredient(IngredientRequest request);
    IngredientResponse updateIngredient(String id, IngredientRequest request);
    void deleteIngredient(String id);
}
