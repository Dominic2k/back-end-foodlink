package org.datpham.foodlink.service;

import org.datpham.foodlink.dto.request.OrderItemRatingRequest;
import org.datpham.foodlink.dto.response.OrderItemRatingResponse;
import org.datpham.foodlink.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<OrderResponse> getAllOrders(String status, Pageable pageable);
    Page<OrderResponse> getMyOrders(Pageable pageable);
    OrderResponse getMyOrderById(String id);
    OrderResponse cancelMyOrder(String id);
    OrderResponse cancelOrder(String id);
    OrderResponse getOrderById(String id);
    OrderResponse updateOrderStatus(String id, String status);
    OrderResponse createOrder(org.datpham.foodlink.dto.request.OrderRequest request);
    OrderItemRatingResponse submitDishRating(String orderId, String orderItemId, OrderItemRatingRequest request);
    void deleteDishRating(String orderId, String orderItemId);
}
