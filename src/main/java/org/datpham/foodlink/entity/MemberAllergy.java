package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.enums.Severity;

@Entity
@Getter
@Setter
@Table(name = "member_allergies")
public class MemberAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "allergy_id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private FamilyMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;
}
