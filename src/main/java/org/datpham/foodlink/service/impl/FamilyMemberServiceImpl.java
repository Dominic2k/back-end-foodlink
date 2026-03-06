package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datpham.foodlink.dto.request.FamilyMemberRequest;
import org.datpham.foodlink.dto.response.FamilyMemberResponse;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.datpham.foodlink.dto.response.IngredientResponse;
import org.datpham.foodlink.dto.response.MemberAllergyResponse;
import org.datpham.foodlink.entity.*;
import org.datpham.foodlink.enums.Relationship;
import org.datpham.foodlink.event.FamilyProfileChangedEvent;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.FamilyMemberRepository;
import org.datpham.foodlink.repository.HealthConditionRepository;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.repository.MemberAllergyRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.service.FamilyMemberService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final HealthConditionRepository healthConditionRepository;
    private final IngredientRepository ingredientRepository;
    private final MemberAllergyRepository memberAllergyRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        member = familyMemberRepository.save(member);

        // Handle allergies after member is saved (so we have an ID)
        updateMemberAllergies(member, request);
        
        FamilyMember savedMember = familyMemberRepository.save(member);
        triggerRecommendationEvaluation(user.getId());
        return toResponse(savedMember);
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
        updateMemberAllergies(member, request);
        FamilyMember savedMember = familyMemberRepository.save(member);
        triggerRecommendationEvaluation(user.getId());
        return toResponse(savedMember);
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
        triggerRecommendationEvaluation(user.getId());
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

    @Override
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(i -> IngredientResponse.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .category(i.getCategory())
                        .defaultUnit(i.getDefaultUnit())
                        .imageUrl(i.getImageUrl())
                        .isActive(i.getIsActive())
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

    private void updateMemberAllergies(FamilyMember member, FamilyMemberRequest request) {
        if (request.getAllergies() != null) {
            // Delete existing allergies directly via repository and flush to DB
            // This ensures DELETEs execute BEFORE INSERTs, avoiding UNIQUE constraint violations
            memberAllergyRepository.deleteByMemberId(member.getId());
            memberAllergyRepository.flush();

            // Clear the in-memory collection to stay in sync
            member.getAllergies().clear();

            // Add new allergies
            request.getAllergies().forEach(allergyReq -> {
                Ingredient ingredient = ingredientRepository.findById(allergyReq.getIngredientId())
                        .orElseThrow(() -> new BusinessException(
                                "Ingredient not found: " + allergyReq.getIngredientId(), HttpStatus.BAD_REQUEST));

                MemberAllergy allergy = new MemberAllergy();
                allergy.setMember(member);
                allergy.setIngredient(ingredient);
                allergy.setSeverity(allergyReq.getSeverity());
                member.getAllergies().add(allergy);
            });
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
                .allergies(member.getAllergies().stream()
                        .map(a -> MemberAllergyResponse.builder()
                                .id(a.getId())
                                .ingredientId(a.getIngredient().getId())
                                .ingredientName(a.getIngredient().getName())
                                .severity(a.getSeverity())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    private void triggerRecommendationEvaluation(String userId) {
        eventPublisher.publishEvent(new FamilyProfileChangedEvent(userId));
        log.info("Published family profile changed event for user {}", userId);
    }
}
