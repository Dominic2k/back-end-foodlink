package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.enums.ActivityLevel;
import org.datpham.foodlink.enums.Gender;
import org.datpham.foodlink.enums.Relationship;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "family_members")
public class FamilyMember extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false)
    private Relationship relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;

    @Column(name = "health_notes", columnDefinition = "TEXT")
    private String healthNotes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "member_conditions",
        joinColumns = @JoinColumn(name = "member_id"),
        inverseJoinColumns = @JoinColumn(name = "condition_id")
    )
    private java.util.Set<HealthCondition> healthConditions = new java.util.HashSet<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.Set<MemberAllergy> allergies = new java.util.HashSet<>();
}
