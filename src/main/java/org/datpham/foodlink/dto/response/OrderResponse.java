package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String userFullName;
    private String status;
    private String deliveryAddressText;
    private String deliveryPhone;
    private String note;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String id;
        private String recipeId;
        private String recipeName;
        private Integer servings;
        private BigDecimal pricePerServingSnapshot;
        private BigDecimal lineTotal;
        private List<OrderIngredientResponse> ingredients;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderIngredientResponse {
        private String ingredientId;
        private String ingredientName;
        private BigDecimal quantityBase;
        private String baseUnit;
        private BigDecimal unitPriceSnapshot;
        private BigDecimal lineTotal;
    }
}
