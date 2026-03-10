package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.response.OrderResponse;
import org.datpham.foodlink.entity.Order;
import org.datpham.foodlink.entity.OrderItem;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.entity.Ingredient;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.OrderRepository;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(String status, Pageable pageable) {
        Page<Order> orders;

        if (status != null && !status.isBlank()) {
            try {
                Order.OrderStatus statusEnum = Order.OrderStatus.valueOf(status);
                orders = orderRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                orders = orderRepository.findAll(pageable);
            }
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Order not found", HttpStatus.NOT_FOUND));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Order not found", HttpStatus.NOT_FOUND));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status, HttpStatus.BAD_REQUEST);
        }

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse createOrder(org.datpham.foodlink.dto.request.OrderRequest request) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.pending);
        order.setDeliveryAddressText(request.getDeliveryAddressText());
        order.setDeliveryPhone(request.getDeliveryPhone());
        order.setNote(request.getNote());
        order.setTotalAmount(request.getTotalAmount());
        order.setPaymentMethod(request.getPaymentMethod());

        for (var itemReq : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                    .orElseThrow(() -> new BusinessException("Ingredient not found: " + itemReq.getIngredientId(), HttpStatus.BAD_REQUEST));
            
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setIngredient(ingredient);
            item.setQuantity(itemReq.getQuantity());
            item.setUnit(itemReq.getUnit());
            item.setPrice(itemReq.getPrice());
            item.setLineTotal(itemReq.getLineTotal());
            
            order.getOrderItems().add(item);
        }

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    private OrderResponse toResponse(Order order) {
        var items = order.getOrderItems() != null
                ? order.getOrderItems().stream().map(item ->
                OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .ingredientId(item.getIngredient() != null ? item.getIngredient().getId() : null)
                        .ingredientName(item.getIngredient() != null ? item.getIngredient().getName() : "Unknown")
                        .quantity(item.getQuantity())
                        .unit(item.getUnit())
                        .price(item.getPrice())
                        .lineTotal(item.getLineTotal())
                        .build()
        ).collect(Collectors.toList())
                : new ArrayList<OrderResponse.OrderItemResponse>();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .status(order.getStatus() != null ? order.getStatus().name() : "pending")
                .deliveryAddressText(order.getDeliveryAddressText())
                .deliveryPhone(order.getDeliveryPhone())
                .note(order.getNote())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }
}
