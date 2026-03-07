package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long newUsersThisMonth;
    private long totalFamilyMembers;
    private long totalHealthConditions;
    private long totalIngredients;
    private long totalRecipes;
    private long publishedRecipes;
    private long totalOrders;
    private long pendingOrders;
    private long todayActivities;
    private long totalAppVisits;
    private long todayAppVisits;
    private List<DailyActivityCount> dailyActivities;
    private List<DailyActivityCount> dailyAppVisits;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyActivityCount {
        private String date;
        private long count;
    }
}
