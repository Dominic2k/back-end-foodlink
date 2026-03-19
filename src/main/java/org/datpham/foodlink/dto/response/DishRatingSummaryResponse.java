package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class DishRatingSummaryResponse {
    private BigDecimal averageRating;
    private Long totalRatings;
}
