package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    List<ActivityLog> findTop20ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT FUNCTION('DATE', a.createdAt) as day, COUNT(a) " +
           "FROM ActivityLog a WHERE a.createdAt >= :since " +
           "GROUP BY FUNCTION('DATE', a.createdAt) " +
           "ORDER BY day ASC")
    List<Object[]> countDailyActivitySince(@Param("since") LocalDateTime since);
}
