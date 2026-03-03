package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.datpham.foodlink.enums.UserStatus;

@Getter
@Setter
public class AdminUpdateStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
