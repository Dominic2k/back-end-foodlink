package org.datpham.foodlink.controller;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.common.BaseResponse;
import org.datpham.foodlink.dto.response.OrderResponse;
import org.datpham.foodlink.entity.ActivityLog;
import org.datpham.foodlink.service.ActivityLogService;
import org.datpham.foodlink.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("@adminAuthChecker.isAdmin()")
public class AdminOrderController {

    private final OrderService orderService;
    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<OrderResponse>>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                new BaseResponse<>(orderService.getAllOrders(status, pageable), "Success", 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(
                new BaseResponse<>(orderService.getOrderById(id), "Success", 200));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BaseResponse<OrderResponse>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        OrderResponse result = orderService.updateOrderStatus(id, newStatus);
        activityLogService.log(ActivityLog.Action.STATUS_CHANGE, "Order", id,
                "Changed order status to: " + newStatus);
        return ResponseEntity.ok(
                new BaseResponse<>(result, "Status updated successfully", 200));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BaseResponse<OrderResponse>> cancelOrder(@PathVariable String id) {
        OrderResponse response = orderService.cancelOrder(id);
        activityLogService.log(ActivityLog.Action.STATUS_CHANGE, "Order", id,
                "Canceled order via admin");
        return ResponseEntity.ok(new BaseResponse<>(response, "Order canceled successfully", 200));
    }
}
