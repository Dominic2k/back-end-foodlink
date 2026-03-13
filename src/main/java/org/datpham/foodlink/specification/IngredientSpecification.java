package org.datpham.foodlink.specification;

import org.datpham.foodlink.entity.Ingredient;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class IngredientSpecification {

    public static Specification<Ingredient> nameOrCategoryContains(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String like = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("category")), like)
            );
        };
    }

    public static Specification<Ingredient> hasCategory(String category) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(category)) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
        };
    }

    public static Specification<Ingredient> hasIsActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
