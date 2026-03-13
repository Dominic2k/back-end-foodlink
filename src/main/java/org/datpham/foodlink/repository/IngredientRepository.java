package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, String>, JpaSpecificationExecutor<Ingredient> {
    Page<Ingredient> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String category, Pageable pageable);

    Optional<Ingredient> findByNameIgnoreCase(String name);

    List<Ingredient> findAllByIsActiveTrue();
}
