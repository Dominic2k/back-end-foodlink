package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.HealthCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthConditionRepository extends JpaRepository<HealthCondition, String> {
    Optional<HealthCondition> findByCode(String code);
}
