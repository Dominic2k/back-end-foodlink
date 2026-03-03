package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.AdminUpdateStatusRequest;
import org.datpham.foodlink.dto.response.AdminStatsResponse;
import org.datpham.foodlink.dto.response.AdminUserResponse;
import org.datpham.foodlink.dto.response.FamilyMemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    Page<AdminUserResponse> getAllUsers(String search, Pageable pageable);
    AdminUserResponse getUserById(String id);
    AdminUserResponse updateUserStatus(String id, AdminUpdateStatusRequest request);
    AdminStatsResponse getStats();
    List<FamilyMemberResponse> getFamilyMembersByUserId(String userId);
}
