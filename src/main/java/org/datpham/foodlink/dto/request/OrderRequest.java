package org.datpham.foodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddressText;

    private String deliveryPhone;
    private String note;
    
    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;
    
    private String paymentMethod;
    
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;

    @Getter
    @Setter
    public static class OrderItemRequest {
        @NotBlank(message = "Ingredient ID is required")
        private String ingredientId;
        
        @NotNull(message = "Quantity is required")
        private BigDecimal quantity;
        
        @NotBlank(message = "Unit is required")
        private String unit;
        
        private BigDecimal price;
        private BigDecimal lineTotal;
    }
}
