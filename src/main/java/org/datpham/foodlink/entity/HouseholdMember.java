package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.enums.ActivityLevel;
import org.datpham.foodlink.enums.Gender;
import org.datpham.foodlink.enums.Relationship;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "household_members")
@Getter
@Setter
public class HouseholdMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    // 🔥 Many household members belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;

    @Column(name = "health_notes", columnDefinition = "TEXT")
    private String healthNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}