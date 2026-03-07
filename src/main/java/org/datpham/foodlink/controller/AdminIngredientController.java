package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.IngredientRequest;
import org.datpham.foodlink.dto.response.IngredientResponse;
import org.datpham.foodlink.entity.ActivityLog;
import org.datpham.foodlink.service.ActivityLogService;
import org.datpham.foodlink.service.IngredientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ingredients")
@RequiredArgsConstructor
@PreAuthorize("@adminAuthChecker.isAdmin()")
public class AdminIngredientController {

    private final IngredientService ingredientService;
    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<IngredientResponse>>> getAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                new BaseResponse<>(ingredientService.getAllIngredients(search, pageable), "Success", 200));
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<java.util.List<IngredientResponse>>> getAllActive() {
        return ResponseEntity.ok(
                new BaseResponse<>(ingredientService.getAllActiveIngredients(), "Success", 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<IngredientResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(
                new BaseResponse<>(ingredientService.getIngredientById(id), "Success", 200));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<IngredientResponse>> create(
            @Valid @RequestBody IngredientRequest request) {
        IngredientResponse result = ingredientService.createIngredient(request);
        activityLogService.log(ActivityLog.Action.CREATE, "Ingredient", result.getId(),
                "Created ingredient: " + request.getName());
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Created successfully", 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<IngredientResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody IngredientRequest request) {
        IngredientResponse result = ingredientService.updateIngredient(id, request);
        activityLogService.log(ActivityLog.Action.UPDATE, "Ingredient", id,
                "Updated ingredient: " + request.getName());
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Updated successfully", 200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        ingredientService.deleteIngredient(id);
        activityLogService.log(ActivityLog.Action.DELETE, "Ingredient", id,
                "Deleted ingredient #" + id.substring(0, 8));
        return ResponseEntity.ok(
                new BaseResponse<>(null, "Deleted successfully", 200));
    }
}
