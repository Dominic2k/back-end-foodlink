package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "health_conditions")
public class HealthCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "condition_id", length = 36)
    private String id;

    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "dietary_advice", columnDefinition = "TEXT")
    private String dietaryAdvice;

    @Column(name = "exercise_advice", columnDefinition = "TEXT")
    private String exerciseAdvice;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public HealthCondition(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
