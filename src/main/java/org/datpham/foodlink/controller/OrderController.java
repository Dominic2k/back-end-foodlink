package org.datpham.foodlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.OrderRequest;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.response.OrderResponse;
import org.datpham.foodlink.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(new BaseResponse<>(response, "Order created successfully", 200));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<BaseResponse<Page<OrderResponse>>> getMyOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponse> response = orderService.getMyOrders(pageable);
        return ResponseEntity.ok(new BaseResponse<>(response, "Fetched my orders successfully", 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderResponse>> getMyOrderById(@PathVariable String id) {
        OrderResponse response = orderService.getMyOrderById(id);
        return ResponseEntity.ok(new BaseResponse<>(response, "Fetched order successfully", 200));
    }
}
