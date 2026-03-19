package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    @Query("""
            select oi.recipe.id as recipeId,
                   avg(cast(oi.dishRating as double)) as averageRating,
                   count(oi.id) as totalRatings
            from OrderItem oi
            where oi.recipe.id in :recipeIds
              and oi.dishRating is not null
            group by oi.recipe.id
            """)
    List<RecipeRatingSummaryProjection> findRatingSummariesByRecipeIds(@Param("recipeIds") Collection<String> recipeIds);

    @Query("""
            select o.user.id as userId,
                   o.user.fullName as userFullName,
                   oi.dishRating as rating,
                   oi.dishRatingComment as comment,
                   oi.dishRatedAt as ratedAt
            from OrderItem oi
            join oi.order o
            where oi.recipe.id = :recipeId
              and oi.dishRating is not null
            order by oi.dishRatedAt desc
            """)
    List<RecipeReviewProjection> findReviewsByRecipeId(@Param("recipeId") String recipeId);

    interface RecipeRatingSummaryProjection {
        String getRecipeId();
        Double getAverageRating();
        Long getTotalRatings();
    }

    interface RecipeReviewProjection {
        String getUserId();
        String getUserFullName();
        Integer getRating();
        String getComment();
        LocalDateTime getRatedAt();
    }
}
