package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.AdminUpdateStatusRequest;
import org.datpham.foodlink.dto.response.AdminStatsResponse;
import org.datpham.foodlink.dto.response.AdminUserResponse;
import org.datpham.foodlink.dto.response.FamilyMemberResponse;
import org.datpham.foodlink.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("@adminAuthChecker.isAdmin()")
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<BaseResponse<Page<AdminUserResponse>>> getAllUsers(
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
                new BaseResponse<>(adminService.getAllUsers(search, pageable), "Success", 200)
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<BaseResponse<AdminUserResponse>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(
                new BaseResponse<>(adminService.getUserById(id), "Success", 200)
        );
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<BaseResponse<AdminUserResponse>> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody AdminUpdateStatusRequest request) {
        return ResponseEntity.ok(
                new BaseResponse<>(adminService.updateUserStatus(id, request), "Status updated successfully", 200)
        );
    }

    @GetMapping("/users/{id}/family-members")
    public ResponseEntity<BaseResponse<List<FamilyMemberResponse>>> getUserFamilyMembers(
            @PathVariable String id) {
        return ResponseEntity.ok(
                new BaseResponse<>(adminService.getFamilyMembersByUserId(id), "Success", 200)
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<BaseResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(
                new BaseResponse<>(adminService.getStats(), "Success", 200)
        );
    }
}
