package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
}
