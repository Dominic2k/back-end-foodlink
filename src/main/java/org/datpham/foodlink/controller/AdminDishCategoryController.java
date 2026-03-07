package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.DishCategoryRequest;
import org.datpham.foodlink.dto.response.DishCategoryResponse;
import org.datpham.foodlink.entity.ActivityLog;
import org.datpham.foodlink.service.ActivityLogService;
import org.datpham.foodlink.service.DishCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dish-categories")
@RequiredArgsConstructor
@PreAuthorize("@adminAuthChecker.isAdmin()")
public class AdminDishCategoryController {

    private final DishCategoryService dishCategoryService;
    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<DishCategoryResponse>>> getAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                new BaseResponse<>(dishCategoryService.getAllCategories(search, pageable), "Success", 200));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DishCategoryResponse>> create(
            @Valid @RequestBody DishCategoryRequest request) {
        DishCategoryResponse result = dishCategoryService.createCategory(request);
        activityLogService.log(ActivityLog.Action.CREATE, "DishCategory", result.getId(),
                "Created dish category: " + request.getName());
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Created successfully", 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<DishCategoryResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody DishCategoryRequest request) {
        DishCategoryResponse result = dishCategoryService.updateCategory(id, request);
        activityLogService.log(ActivityLog.Action.UPDATE, "DishCategory", id,
                "Updated dish category: " + request.getName());
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Updated successfully", 200));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<BaseResponse<DishCategoryResponse>> deactivate(@PathVariable String id) {
        DishCategoryResponse result = dishCategoryService.deactivateCategory(id);
        activityLogService.log(ActivityLog.Action.STATUS_CHANGE, "DishCategory", id,
                "Deactivated dish category: " + result.getName());
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Deactivated successfully", 200));
    }
}
