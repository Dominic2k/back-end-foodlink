package org.datpham.foodlink.controller;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.response.DishRecommendationResponse;
import org.datpham.foodlink.service.DishRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class DishRecommendationController {

    private final DishRecommendationService dishRecommendationService;

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
