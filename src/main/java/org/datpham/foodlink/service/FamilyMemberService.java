package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.FamilyMemberRequest;
import org.datpham.foodlink.dto.response.FamilyMemberResponse;
import org.datpham.foodlink.dto.response.HealthConditionResponse;

import java.util.List;

public interface FamilyMemberService {
    List<FamilyMemberResponse> getFamilyMembers();
    FamilyMemberResponse addFamilyMember(FamilyMemberRequest request);
    FamilyMemberResponse updateFamilyMember(String id, FamilyMemberRequest request);
    void deleteFamilyMember(String id);
    List<HealthConditionResponse> getAllConditions();
}
