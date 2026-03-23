package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.response.ActivityLogResponse;
import org.datpham.foodlink.entity.ActivityLog;

import java.util.List;

public interface ActivityLogService {

    void log(ActivityLog.Action action, String entityType, String entityId, String description);

    List<ActivityLogResponse> getRecentLogs();

    long countTodayActivities();

    List<Object[]> getDailyActivityCounts(int days);
}
