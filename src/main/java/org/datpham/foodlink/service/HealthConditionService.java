package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.HealthConditionRequest;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HealthConditionService {
    Page<HealthConditionResponse> getAllConditions(String search, Pageable pageable);
    HealthConditionResponse getConditionById(String id);
    HealthConditionResponse createCondition(HealthConditionRequest request);
    HealthConditionResponse updateCondition(String id, HealthConditionRequest request);
    void deleteCondition(String id);
}
