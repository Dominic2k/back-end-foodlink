package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemRatingResponse {
    private String orderId;
    private String orderItemId;
    private String recipeId;
    private String recipeName;
    private Integer rating;
    private String comment;
    private LocalDateTime ratedAt;
}
