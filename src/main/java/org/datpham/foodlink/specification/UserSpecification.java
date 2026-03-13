package org.datpham.foodlink.specification;

import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<User> fullNameOrEmailContains(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String like = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("email")), like)
            );
        };
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(role)) {
                return cb.conjunction();
            }
            String normalized = role.trim().toLowerCase();
            if ("admin".equals(normalized)) {
                return cb.equal(root.get("isAdmin"), true);
            } else if ("user".equals(normalized)) {
                return cb.or(
                    cb.equal(root.get("isAdmin"), false),
                    cb.isNull(root.get("isAdmin"))
                );
            }
            return cb.conjunction();
        };
    }

    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(status)) {
                return cb.conjunction();
            }
            try {
                UserStatus userStatus = UserStatus.valueOf(status.trim());
                return cb.equal(root.get("status"), userStatus);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }
}
