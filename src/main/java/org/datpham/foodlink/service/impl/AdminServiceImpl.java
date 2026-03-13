package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.AdminUpdateStatusRequest;
import org.datpham.foodlink.dto.response.AdminStatsResponse;
import org.datpham.foodlink.dto.response.AdminUserResponse;
import org.datpham.foodlink.dto.response.FamilyMemberResponse;
import org.datpham.foodlink.dto.response.HealthConditionResponse;
import org.datpham.foodlink.entity.FamilyMember;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.enums.UserStatus;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.entity.Order;
import org.datpham.foodlink.entity.Recipe;
import org.datpham.foodlink.repository.*;
import org.datpham.foodlink.service.ActivityLogService;
import org.datpham.foodlink.service.AdminService;
import org.datpham.foodlink.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final HealthConditionRepository healthConditionRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final OrderRepository orderRepository;
    private final AppVisitRepository appVisitRepository;
    private final ActivityLogService activityLogService;
    private final UserSessionRepository userSessionRepository;

    @Override
    public Page<AdminUserResponse> getAllUsers(String search, String role, String status, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<User> spec = org.springframework.data.jpa.domain.Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(UserSpecification.fullNameOrEmailContains(search));
        }
        if (role != null && !role.isBlank()) {
            spec = spec.and(UserSpecification.hasRole(role));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and(UserSpecification.hasStatus(status));
        }
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::toAdminUserResponse);
    }

    @Override
    public AdminUserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        return toAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(String id, AdminUpdateStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            throw new BusinessException("Cannot change status of an admin user", HttpStatus.FORBIDDEN);
        }

        user.setStatus(request.getStatus());
        User saved = userRepository.save(user);
        return toAdminUserResponse(saved);
    }

    @Override
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.active);
        long blockedUsers = userRepository.countByStatus(UserStatus.blocked);

        LocalDateTime startOfMonth = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

        long totalFamilyMembers = familyMemberRepository.count();
        long totalHealthConditions = healthConditionRepository.count();

        long totalIngredients = ingredientRepository.count();
        long totalRecipes = recipeRepository.count();
        long publishedRecipes = recipeRepository.countByStatus(Recipe.RecipeStatus.published);
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.pending);

        // Activity stats
        long todayActivities = activityLogService.countTodayActivities();
        List<AdminStatsResponse.DailyActivityCount> dailyActivities = buildDailyActivities();

        // App visit stats
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long totalAppVisits = appVisitRepository.count();
        long todayAppVisits = appVisitRepository.countByVisitedAtAfter(startOfToday);
        List<AdminStatsResponse.DailyActivityCount> dailyAppVisits = buildDailyVisits();

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalFamilyMembers(totalFamilyMembers)
                .totalHealthConditions(totalHealthConditions)
                .totalIngredients(totalIngredients)
                .totalRecipes(totalRecipes)
                .publishedRecipes(publishedRecipes)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .todayActivities(todayActivities)
                .totalAppVisits(totalAppVisits)
                .todayAppVisits(todayAppVisits)
                .dailyActivities(dailyActivities)
                .dailyAppVisits(dailyAppVisits)
                .build();
    }

    private List<AdminStatsResponse.DailyActivityCount> buildDailyActivities() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        // Initialize last 7 days with 0
        Map<String, Long> dayMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dayMap.put(LocalDate.now().minusDays(i).format(fmt), 0L);
        }
        // Fill actual data
        List<Object[]> raw = activityLogService.getDailyActivityCounts(7);
        for (Object[] row : raw) {
            String dateStr;
            if (row[0] instanceof java.sql.Date) {
                dateStr = ((java.sql.Date) row[0]).toLocalDate().format(fmt);
            } else {
                dateStr = LocalDate.parse(row[0].toString()).format(fmt);
            }
            long count = ((Number) row[1]).longValue();
            dayMap.put(dateStr, count);
        }
        List<AdminStatsResponse.DailyActivityCount> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : dayMap.entrySet()) {
            result.add(AdminStatsResponse.DailyActivityCount.builder()
                    .date(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }
        return result;
    }

    private List<AdminStatsResponse.DailyActivityCount> buildDailyVisits() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        Map<String, Long> dayMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dayMap.put(LocalDate.now().minusDays(i).format(fmt), 0L);
        }
        LocalDateTime since = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> raw = appVisitRepository.getDailyVisitCounts(since);
        for (Object[] row : raw) {
            String dateStr;
            if (row[0] instanceof java.sql.Date) {
                dateStr = ((java.sql.Date) row[0]).toLocalDate().format(fmt);
            } else {
                dateStr = LocalDate.parse(row[0].toString()).format(fmt);
            }
            long count = ((Number) row[1]).longValue();
            dayMap.put(dateStr, count);
        }
        List<AdminStatsResponse.DailyActivityCount> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : dayMap.entrySet()) {
            result.add(AdminStatsResponse.DailyActivityCount.builder()
                    .date(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getFamilyMembersByUserId(String userId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        return familyMemberRepository.findAllByUserIdWithConditions(userId).stream()
                .map(this::toFamilyMemberResponse)
                .collect(Collectors.toList());
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        // Get latest session duration
        Long lastSessionDuration = userSessionRepository
                .findTopByUserIdOrderByStartedAtDesc(user.getId())
                .map(s -> s.getDurationSeconds())
                .orElse(null);

        // Get average session duration
        Double avgSessionDuration = userSessionRepository
                .findAverageSessionDurationByUserId(user.getId());

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .isAdmin(user.getIsAdmin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .lastSessionDurationSeconds(lastSessionDuration)
                .avgSessionDurationSeconds(avgSessionDuration)
                .build();
    }

    private FamilyMemberResponse toFamilyMemberResponse(FamilyMember member) {
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
