package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.request.FamilyMemberRequest;
import org.datpham.foodlink.dto.response.FamilyMemberResponse;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.datpham.foodlink.dto.response.IngredientResponse;
import org.datpham.foodlink.service.FamilyMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<FamilyMemberResponse>>> getFamilyMembers() {
        return ResponseEntity.ok(
                new BaseResponse<>(familyMemberService.getFamilyMembers(), "Success", 200)
        );
    }

    @PostMapping
    public ResponseEntity<BaseResponse<FamilyMemberResponse>> addFamilyMember(
            @Valid @RequestBody FamilyMemberRequest request) {
        return ResponseEntity.ok(
                new BaseResponse<>(familyMemberService.addFamilyMember(request), "Member added successfully", 200)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<FamilyMemberResponse>> updateFamilyMember(
            @PathVariable String id,
            @Valid @RequestBody FamilyMemberRequest request) {
        return ResponseEntity.ok(
                new BaseResponse<>(familyMemberService.updateFamilyMember(id, request), "Member updated successfully", 200)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteFamilyMember(@PathVariable String id) {
        familyMemberService.deleteFamilyMember(id);
        return ResponseEntity.ok(
                new BaseResponse<>(null, "Member deleted successfully", 200)
        );
    }

    @GetMapping("/conditions")
    public ResponseEntity<BaseResponse<List<HealthConditionResponse>>> getAllConditions() {
        return ResponseEntity.ok(
                new BaseResponse<>(familyMemberService.getAllConditions(), "Success", 200)
        );
    }

    @GetMapping("/ingredients")
    public ResponseEntity<BaseResponse<List<IngredientResponse>>> getAllIngredients() {
        return ResponseEntity.ok(
                new BaseResponse<>(familyMemberService.getAllIngredients(), "Success", 200)
        );
    }
}

