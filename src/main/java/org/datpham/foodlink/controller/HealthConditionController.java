package org.datpham.foodlink.controller;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.datpham.foodlink.entity.HealthCondition;
import org.datpham.foodlink.repository.HealthConditionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/health-conditions")
@RequiredArgsConstructor
public class HealthConditionController {

    private final HealthConditionRepository healthConditionRepository;

    @GetMapping
    public ResponseEntity<List<HealthConditionResponse>> getAll() {
        List<HealthConditionResponse> list = healthConditionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthConditionResponse> getById(@PathVariable String id) {
        return healthConditionRepository.findById(id)
                .map(h -> ResponseEntity.ok(mapToResponse(h)))
                .orElse(ResponseEntity.notFound().build());
    }

    private HealthConditionResponse mapToResponse(HealthCondition h) {
        return HealthConditionResponse.builder()
                .id(h.getId())
                .code(h.getCode())
                .name(h.getName())
                .description(h.getDescription())
                .dietaryAdvice(h.getDietaryAdvice())
                .exerciseAdvice(h.getExerciseAdvice())
                .imageUrl(h.getImageUrl())
                .build();
    }
}
