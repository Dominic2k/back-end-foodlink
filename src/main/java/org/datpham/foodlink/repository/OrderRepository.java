package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    Page<Order> findByUserEmailOrderByCreatedAtDesc(String email, Pageable pageable);
    Optional<Order> findByIdAndUserEmail(String id, String email);
    long countByStatus(Order.OrderStatus status);
}
