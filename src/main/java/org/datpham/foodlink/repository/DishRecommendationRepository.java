package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.DishRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRecommendationRepository extends JpaRepository<DishRecommendation, String> {
    Optional<DishRecommendation> findByUser_IdAndRecipe_Id(String userId, String recipeId);
    List<DishRecommendation> findAllByUser_IdOrderByScoreDesc(String userId);
}
