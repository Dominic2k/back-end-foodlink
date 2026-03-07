package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "dish_recommendations",
        uniqueConstraints = @UniqueConstraint(name = "uk_recommend_user_recipe", columnNames = {"user_id", "recipe_id"})
)
public class DishRecommendation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "recommendation_id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "is_suitable", nullable = false)
    private Boolean suitable;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;
}
