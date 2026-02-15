package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.FamilyMemberRequest;
import org.datpham.foodlink.dto.response.FamilyMemberResponse;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.datpham.foodlink.entity.FamilyMember;
import org.datpham.foodlink.entity.HealthCondition;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.enums.Relationship;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.FamilyMemberRepository;
import org.datpham.foodlink.repository.HealthConditionRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.service.FamilyMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final HealthConditionRepository healthConditionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<FamilyMemberResponse> getFamilyMembers() {
        User user = getCurrentUser();
        
        // Auto-create "self" if not exists
        if (familyMemberRepository.findByUserAndRelationship(user, Relationship.self).isEmpty()) {
            FamilyMember self = new FamilyMember();
            self.setUser(user);
            self.setDisplayName(user.getFullName());
            self.setRelationship(Relationship.self);
            familyMemberRepository.save(self);
        }

        return familyMemberRepository.findAllByUser(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FamilyMemberResponse addFamilyMember(FamilyMemberRequest request) {
        User user = getCurrentUser();
        
        if (request.getRelationship() == Relationship.self) {
            if (familyMemberRepository.findByUserAndRelationship(user, Relationship.self).isPresent()) {
                throw new BusinessException("Self member already exists", HttpStatus.BAD_REQUEST);
            }
        }

        FamilyMember member = new FamilyMember();
        member.setUser(user);
        updateMemberFields(member, request);
        
        return toResponse(familyMemberRepository.save(member));
    }

    @Override
    @Transactional
    public FamilyMemberResponse updateFamilyMember(String id, FamilyMemberRequest request) {
        User user = getCurrentUser();
        FamilyMember member = familyMemberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Member not found", HttpStatus.NOT_FOUND));

        if (!member.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Permission denied", HttpStatus.FORBIDDEN);
        }

        // Don't allow changing relationship to "self" if it's not already self
        if (request.getRelationship() == Relationship.self && member.getRelationship() != Relationship.self) {
             throw new BusinessException("Cannot change relationship to 'self'", HttpStatus.BAD_REQUEST);
        }

        updateMemberFields(member, request);
        return toResponse(familyMemberRepository.save(member));
    }

    @Override
    @Transactional
    public void deleteFamilyMember(String id) {
        User user = getCurrentUser();
        FamilyMember member = familyMemberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Member not found", HttpStatus.NOT_FOUND));

        if (!member.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Permission denied", HttpStatus.FORBIDDEN);
        }

        if (member.getRelationship() == Relationship.self) {
            throw new BusinessException("Cannot delete yourself from family members", HttpStatus.BAD_REQUEST);
        }

        familyMemberRepository.delete(member);
    }

    @Override
    public List<HealthConditionResponse> getAllConditions() {
        return healthConditionRepository.findAll().stream()
                .map(c -> HealthConditionResponse.builder()
                        .id(c.getId())
                        .code(c.getCode())
                        .name(c.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    private void updateMemberFields(FamilyMember member, FamilyMemberRequest request) {
        member.setDisplayName(request.getDisplayName());
        member.setRelationship(request.getRelationship());
        member.setGender(request.getGender());
        member.setBirthDate(request.getBirthDate());
        member.setHeightCm(request.getHeightCm());
        member.setWeightKg(request.getWeightKg());
        member.setActivityLevel(request.getActivityLevel());
        member.setHealthNotes(request.getHealthNotes());

        if (request.getConditionIds() != null) {
            Set<HealthCondition> conditions = new HashSet<>(healthConditionRepository.findAllById(request.getConditionIds()));
            member.setHealthConditions(conditions);
        }
    }

    private FamilyMemberResponse toResponse(FamilyMember member) {
        return FamilyMemberResponse.builder()
                .id(member.getId())
                .displayName(member.getDisplayName())
                .relationship(member.getRelationship())
                .gender(member.getGender())
                .birthDate(member.getBirthDate())
                .heightCm(member.getHeightCm())
                .weightKg(member.getWeightKg())
                .activityLevel(member.getActivityLevel())
                .healthNotes(member.getHealthNotes())
                .healthConditions(member.getHealthConditions().stream()
                        .map(c -> HealthConditionResponse.builder()
                                .id(c.getId())
                                .code(c.getCode())
                                .name(c.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
