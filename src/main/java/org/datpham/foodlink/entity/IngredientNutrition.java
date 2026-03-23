package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "ingredient_nutrition")
public class IngredientNutrition {

    @Id
    @Column(name = "ingredient_id", length = 36)
    private String ingredientId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(name = "calories_per_100", precision = 6, scale = 2)
    private BigDecimal caloriesPer100;

    @Column(name = "protein_g_per_100", precision = 6, scale = 2)
    private BigDecimal proteinGPer100;

    @Column(name = "carb_g_per_100", precision = 6, scale = 2)
    private BigDecimal carbGPer100;

    @Column(name = "fat_g_per_100", precision = 6, scale = 2)
    private BigDecimal fatGPer100;
}
