package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ingredients")
public class Ingredient extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ingredient_id", length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "default_unit", length = 50)
    private String defaultUnit;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToOne(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private IngredientNutrition nutrition;
}
