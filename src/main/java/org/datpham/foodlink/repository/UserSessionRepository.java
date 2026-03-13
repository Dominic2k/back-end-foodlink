package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findTopByUserIdOrderByStartedAtDesc(String userId);

    @Query("SELECT AVG(s.durationSeconds) FROM UserSession s WHERE s.userId = :userId")
    Double findAverageSessionDurationByUserId(@Param("userId") String userId);
}
