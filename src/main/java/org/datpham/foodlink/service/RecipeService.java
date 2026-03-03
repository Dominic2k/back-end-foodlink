package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.RecipeRequest;
import org.datpham.foodlink.dto.response.RecipeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecipeService {
    Page<RecipeResponse> getAllRecipes(String search, String status, Pageable pageable);
    RecipeResponse getRecipeById(String id);
    RecipeResponse createRecipe(RecipeRequest request);
    RecipeResponse updateRecipe(String id, RecipeRequest request);
    RecipeResponse updateRecipeStatus(String id, String status);
    void deleteRecipe(String id);
}
