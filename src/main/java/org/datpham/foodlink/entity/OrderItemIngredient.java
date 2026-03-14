package org.datpham.foodlink.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "order_item_ingredients")
public class OrderItemIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_ingredient_id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "ingredient_name_snapshot", nullable = false)
    private String ingredientNameSnapshot;

    @Column(name = "quantity_base", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityBase;

    @Column(name = "base_unit", nullable = false, length = 50)
    private String baseUnit;

    @Column(name = "unit_price_snapshot", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitPriceSnapshot;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}
