package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.FamilyMember;
import org.datpham.foodlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, String> {
    List<FamilyMember> findAllByUser(User user);

    @Query("SELECT DISTINCT fm FROM FamilyMember fm LEFT JOIN FETCH fm.healthConditions WHERE fm.user.id = :userId")
    List<FamilyMember> findAllByUserIdWithConditions(@Param("userId") String userId);

    Optional<FamilyMember> findByUserAndRelationship(User user, org.datpham.foodlink.enums.Relationship relationship);
}
