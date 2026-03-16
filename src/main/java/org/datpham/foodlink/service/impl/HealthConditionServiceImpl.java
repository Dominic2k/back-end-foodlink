package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.HealthConditionRequest;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.datpham.foodlink.entity.HealthCondition;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.HealthConditionRepository;
import org.datpham.foodlink.service.HealthConditionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthConditionServiceImpl implements HealthConditionService {

    private final HealthConditionRepository healthConditionRepository;

    @Override
    public Page<HealthConditionResponse> getAllConditions(String search, Pageable pageable) {
        Page<HealthCondition> page;
        if (search != null && !search.isBlank()) {
            page = healthConditionRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                    search, search, pageable);
        } else {
            page = healthConditionRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    public HealthConditionResponse getConditionById(String id) {
        HealthCondition hc = healthConditionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Health condition not found", HttpStatus.NOT_FOUND));
        return toResponse(hc);
    }

    @Override
    @Transactional
    public HealthConditionResponse createCondition(HealthConditionRequest request) {
        // Check unique code
        healthConditionRepository.findByCode(request.getCode()).ifPresent(existing -> {
            throw new BusinessException("Code '" + request.getCode() + "' already exists", HttpStatus.CONFLICT);
        });
        // Check unique name
        healthConditionRepository.findByName(request.getName()).ifPresent(existing -> {
            throw new BusinessException("Name '" + request.getName() + "' already exists", HttpStatus.CONFLICT);
        });

        HealthCondition hc = new HealthCondition(request.getCode(), request.getName());
        hc.setDescription(request.getDescription());
        hc.setDietaryAdvice(request.getDietaryAdvice());
        hc.setExerciseAdvice(request.getExerciseAdvice());
        hc.setImageUrl(request.getImageUrl());
        HealthCondition saved = healthConditionRepository.save(hc);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public HealthConditionResponse updateCondition(String id, HealthConditionRequest request) {
        HealthCondition hc = healthConditionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Health condition not found", HttpStatus.NOT_FOUND));

        // Check unique code (exclude self)
        healthConditionRepository.findByCode(request.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Code '" + request.getCode() + "' already exists", HttpStatus.CONFLICT);
            }
        });
        // Check unique name (exclude self)
        healthConditionRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Name '" + request.getName() + "' already exists", HttpStatus.CONFLICT);
            }
        });

        hc.setCode(request.getCode());
        hc.setName(request.getName());
        hc.setDescription(request.getDescription());
        hc.setDietaryAdvice(request.getDietaryAdvice());
        hc.setExerciseAdvice(request.getExerciseAdvice());
        hc.setImageUrl(request.getImageUrl());
        HealthCondition saved = healthConditionRepository.save(hc);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCondition(String id) {
        if (!healthConditionRepository.existsById(id)) {
            throw new BusinessException("Health condition not found", HttpStatus.NOT_FOUND);
        }
        healthConditionRepository.deleteById(id);
    }

    private HealthConditionResponse toResponse(HealthCondition hc) {
        return HealthConditionResponse.builder()
                .id(hc.getId())
                .code(hc.getCode())
                .name(hc.getName())
                .description(hc.getDescription())
                .dietaryAdvice(hc.getDietaryAdvice())
                .exerciseAdvice(hc.getExerciseAdvice())
                .imageUrl(hc.getImageUrl())
                .build();
    }
}
