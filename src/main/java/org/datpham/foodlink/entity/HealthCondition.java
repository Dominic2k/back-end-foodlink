package org.datpham.foodlink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public HealthCondition(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
