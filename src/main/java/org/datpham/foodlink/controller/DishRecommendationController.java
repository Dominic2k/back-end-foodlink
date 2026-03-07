package org.datpham.foodlink.controller;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.response.DishRecommendationResponse;
import org.datpham.foodlink.dto.response.RecommendationFilterOptionsResponse;
import org.datpham.foodlink.dto.response.RecommendationPageResponse;
import org.datpham.foodlink.service.DishRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class DishRecommendationController {

    private final DishRecommendationService dishRecommendationService;

    @GetMapping("/filter-options")
    public ResponseEntity<BaseResponse<RecommendationFilterOptionsResponse>> getFilterOptions() {
        return ResponseEntity.ok(
                new BaseResponse<>(dishRecommendationService.getFilterOptionsForCurrentUser(), "Success", 200)
        );
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<BaseResponse<DishRecommendationResponse>> getRecommendationDetail(
            @PathVariable String recipeId
    ) {
        return ResponseEntity.ok(
                new BaseResponse<>(
                        dishRecommendationService.getRecommendationDetailForCurrentUser(recipeId),
                        "Success",
                        200
                )
        );
    }

    @GetMapping
    public ResponseEntity<BaseResponse<RecommendationPageResponse>> getRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String suitable,
            @RequestParam(defaultValue = "all") String evaluated,
            @RequestParam(required = false) Integer scoreMin,
            @RequestParam(required = false) Integer scoreMax,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ingredientCategory,
            @RequestParam(required = false) String dishCategory,
            @RequestParam(required = false) String category
    ) {
        String effectiveIngredientCategory = ingredientCategory != null ? ingredientCategory : category;
        return ResponseEntity.ok(
                new BaseResponse<>(
                        dishRecommendationService.getRecommendationsForCurrentUser(
                                page, size, suitable, evaluated, scoreMin, scoreMax, q, effectiveIngredientCategory, dishCategory
                        ),
                        "Success",
                        200
                )
        );
    }

    @PostMapping("/evaluate")
    public ResponseEntity<BaseResponse<List<DishRecommendationResponse>>> evaluateRecommendations() {
        return ResponseEntity.ok(
                new BaseResponse<>(dishRecommendationService.evaluateForCurrentUser(), "Evaluation completed", 200)
        );
    }

    @PostMapping("/re-evaluate")
    public ResponseEntity<BaseResponse<List<DishRecommendationResponse>>> reevaluateRecommendations() {
        return ResponseEntity.ok(
                new BaseResponse<>(dishRecommendationService.evaluateForCurrentUser(), "Re-evaluation completed", 200)
        );
    }
}
