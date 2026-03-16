package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class IngredientReceiveStockRequest {

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Quantity must be greater than or equal to 0")
    private BigDecimal quantityBase;

    private LocalDate receivedDate;
}
