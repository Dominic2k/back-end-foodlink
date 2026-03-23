package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.DishCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishCategoryRepository extends JpaRepository<DishCategory, String> {
    Page<DishCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<DishCategory> findByNameIgnoreCase(String name);
    List<DishCategory> findByIsActiveTrue();
    List<DishCategory> findAllByIdIn(List<String> ids);
}
