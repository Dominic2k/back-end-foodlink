package org.datpham.foodlink.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class RecipeIngredientId implements Serializable {
    private String recipeId;
    private String ingredientId;
}
