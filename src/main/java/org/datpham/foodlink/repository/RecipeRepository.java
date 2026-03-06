package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {
    Page<Recipe> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Recipe> findByStatus(Recipe.RecipeStatus status, Pageable pageable);
    List<Recipe> findByStatus(Recipe.RecipeStatus status);
    Page<Recipe> findByNameContainingIgnoreCaseAndStatus(String name, Recipe.RecipeStatus status, Pageable pageable);
    long countByStatus(Recipe.RecipeStatus status);
}
