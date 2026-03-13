package org.datpham.foodlink.specification;

import org.datpham.foodlink.entity.Recipe;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class RecipeSpecification {

    public static Specification<Recipe> nameContains(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String like = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), like);
        };
    }

    public static Specification<Recipe> hasStatus(String status) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(status)) {
                return cb.conjunction();
            }
            try {
                Recipe.RecipeStatus st = Recipe.RecipeStatus.valueOf(status.trim());
                return cb.equal(root.get("status"), st);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Recipe> hasCategory(String category) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(category)) {
                return cb.conjunction();
            }
            query.distinct(true);
            return cb.equal(cb.lower(root.join("categories").get("name")), category.trim().toLowerCase());
        };
    }

            return cb.like(cb.lower(root.join("createdBy").get("email")), "%" + createdBy.trim().toLowerCase() + "%");
        };
    }
}
