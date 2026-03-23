package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DishReviewResponse {
    private String userId;
    private String userFullName;
    private Integer rating;
    private String comment;
    private LocalDateTime ratedAt;
    private Boolean mine;
}
