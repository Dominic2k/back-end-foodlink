package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.HealthConditionRequest;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.datpham.foodlink.entity.ActivityLog;
import org.datpham.foodlink.service.ActivityLogService;
import org.datpham.foodlink.service.HealthConditionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/health-conditions")
@RequiredArgsConstructor
@PreAuthorize("@adminAuthChecker.isAdmin()")
public class AdminHealthConditionController {

    private final HealthConditionService healthConditionService;
    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<HealthConditionResponse>>> getAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                new BaseResponse<>(healthConditionService.getAllConditions(search, pageable), "Success", 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<HealthConditionResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(
                new BaseResponse<>(healthConditionService.getConditionById(id), "Success", 200));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<HealthConditionResponse>> create(
            @Valid @RequestBody HealthConditionRequest request) {
        HealthConditionResponse result = healthConditionService.createCondition(request);
        activityLogService.log(ActivityLog.Action.CREATE, "HealthCondition", result.getId(),
                "Created health condition: " + request.getName() + " (" + request.getCode() + ")");
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Created successfully", 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<HealthConditionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody HealthConditionRequest request) {
        HealthConditionResponse result = healthConditionService.updateCondition(id, request);
        activityLogService.log(ActivityLog.Action.UPDATE, "HealthCondition", id,
                "Updated health condition: " + request.getName() + " (" + request.getCode() + ")");
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Updated successfully", 200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        healthConditionService.deleteCondition(id);
        activityLogService.log(ActivityLog.Action.DELETE, "HealthCondition", id,
                "Deleted health condition #" + id.substring(0, 8));
        return ResponseEntity.ok(
                new BaseResponse<>(null, "Deleted successfully", 200));
    }
}
