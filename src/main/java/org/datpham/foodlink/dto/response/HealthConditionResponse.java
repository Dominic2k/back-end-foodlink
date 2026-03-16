package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthConditionResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private String dietaryAdvice;
    private String exerciseAdvice;
    private String imageUrl;
}
