package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.RecipeRequest;
import org.datpham.foodlink.dto.response.RecipeResponse;
import org.datpham.foodlink.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/recipes")
@RequiredArgsConstructor
@PreAuthorize("@adminAuthChecker.isAdmin()")
public class AdminRecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<RecipeResponse>>> getAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                new BaseResponse<>(recipeService.getAllRecipes(search, status, pageable), "Success", 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<RecipeResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(
                new BaseResponse<>(recipeService.getRecipeById(id), "Success", 200));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<RecipeResponse>> create(
            @Valid @RequestBody RecipeRequest request) {
        return ResponseEntity.ok(
                new BaseResponse<>(recipeService.createRecipe(request), "Created successfully", 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<RecipeResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody RecipeRequest request) {
        return ResponseEntity.ok(
                new BaseResponse<>(recipeService.updateRecipe(id, request), "Updated successfully", 200));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BaseResponse<RecipeResponse>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                new BaseResponse<>(recipeService.updateRecipeStatus(id, body.get("status")),
                        "Status updated successfully", 200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.ok(
                new BaseResponse<>(null, "Deleted successfully", 200));
    }
}
