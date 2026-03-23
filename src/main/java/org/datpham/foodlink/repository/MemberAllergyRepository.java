package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.MemberAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberAllergyRepository extends JpaRepository<MemberAllergy, String> {
    void deleteByMemberId(String memberId);
}
