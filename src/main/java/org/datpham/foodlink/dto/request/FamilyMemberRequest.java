package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.enums.ActivityLevel;
import org.datpham.foodlink.enums.Gender;
import org.datpham.foodlink.enums.Relationship;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class FamilyMemberRequest {

    @NotBlank(message = "Display name is required")
    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;

    @NotNull(message = "Relationship is required")
    private Relationship relationship;

    private Gender gender;

    private LocalDate birthDate;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private ActivityLevel activityLevel;

    private String healthNotes;

    private Set<String> conditionIds;
}
