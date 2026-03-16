package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.datpham.foodlink.enums.IngredientExpirationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class IngredientResponse {
    private String id;
    private String name;
    private String category;
    private String baseUnit;
    private BigDecimal pricePerBaseUnit;
    private BigDecimal stockQuantityBase;
    private String imageUrl;
    private LocalDate expirationDate;
    private LocalDate receivedDate;
    private IngredientExpirationStatus expirationStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nutrition
    private BigDecimal caloriesPer100;
    private BigDecimal proteinGPer100;
    private BigDecimal carbGPer100;
    private BigDecimal fatGPer100;
}
