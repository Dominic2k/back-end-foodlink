package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "servings", nullable = false)
    private Integer servings;

    @Column(name = "price_per_serving_snapshot", precision = 12, scale = 2)
    private BigDecimal pricePerServingSnapshot;

    @Column(name = "line_total", precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "dish_rating")
    private Integer dishRating;

    @Column(name = "dish_rating_comment", columnDefinition = "TEXT")
    private String dishRatingComment;

    @Column(name = "dish_rated_at")
    private java.time.LocalDateTime dishRatedAt;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemIngredient> ingredientSnapshots = new ArrayList<>();
}
