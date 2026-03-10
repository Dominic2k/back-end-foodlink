package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<OrderResponse> getAllOrders(String status, Pageable pageable);
    OrderResponse getOrderById(String id);
    OrderResponse updateOrderStatus(String id, String status);
    OrderResponse createOrder(org.datpham.foodlink.dto.request.OrderRequest request);
}
