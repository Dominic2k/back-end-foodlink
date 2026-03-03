package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.HealthCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthConditionRepository extends JpaRepository<HealthCondition, String> {
    Optional<HealthCondition> findByCode(String code);
    Optional<HealthCondition> findByName(String name);
    Page<HealthCondition> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
