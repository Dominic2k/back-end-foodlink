package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.response.ActivityLogResponse;
import org.datpham.foodlink.entity.ActivityLog;
import org.datpham.foodlink.repository.ActivityLogRepository;
import org.datpham.foodlink.service.ActivityLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public void log(ActivityLog.Action action, String entityType, String entityId, String description) {
        String performer = getCurrentUsername();
        ActivityLog log = ActivityLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .performedBy(performer)
                .build();
        activityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getRecentLogs() {
        return activityLogRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countTodayActivities() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return activityLogRepository.countByCreatedAtAfter(startOfToday);
    }

    @Override
    public List<Object[]> getDailyActivityCounts(int days) {
        LocalDateTime since = LocalDate.now().minusDays(days - 1).atStartOfDay();
        return activityLogRepository.countDailyActivitySince(since);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "system";
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .action(log.getAction().name())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .performedBy(log.getPerformedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
