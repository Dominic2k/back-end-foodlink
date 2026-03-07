package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RecommendationFilterOptionsResponse {
    private List<String> ingredientCategories;
    private List<String> dishCategories;
}

