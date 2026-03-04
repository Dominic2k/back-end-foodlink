package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.datpham.foodlink.enums.Severity;

@Getter
@Builder
@AllArgsConstructor
public class MemberAllergyResponse {
    private String id;
    private String ingredientId;
    private String ingredientName;
    private Severity severity;
}
