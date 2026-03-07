package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.AppVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppVisitRepository extends JpaRepository<AppVisit, String> {

    long countByVisitedAtAfter(LocalDateTime after);

    @Query(value = "SELECT DATE(visited_at) as visit_date, COUNT(*) as visit_count " +
            "FROM app_visits " +
            "WHERE visited_at >= :since " +
            "GROUP BY DATE(visited_at) " +
            "ORDER BY visit_date",
            nativeQuery = true)
    List<Object[]> getDailyVisitCounts(@Param("since") LocalDateTime since);
}
