package org.datpham.foodlink.dto.response;

import lombok.*;
import org.datpham.foodlink.enums.ActivityLevel;
import org.datpham.foodlink.enums.Gender;
import org.datpham.foodlink.enums.Relationship;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberResponse {
    private String id;
    private String displayName;
    private Relationship relationship;
    private Gender gender;
    private LocalDate birthDate;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private ActivityLevel activityLevel;
    private String healthNotes;
    private Set<HealthConditionResponse> healthConditions;
    private Set<MemberAllergyResponse> allergies;
}
