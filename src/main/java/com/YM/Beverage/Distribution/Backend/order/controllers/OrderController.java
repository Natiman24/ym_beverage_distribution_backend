package com.YM.Beverage.Distribution.Backend.order.controllers;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.order.dtos.*;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import com.YM.Beverage.Distribution.Backend.order.services.OrderService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        ApiResponse response = orderService.createOrder(dto, jwtUtil.getUserId(jwtUtil.resolveToken(request)));
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getOrders(
            @RequestParam(value = "store-id", required = false) UUID storeId,
            @RequestParam(value = "statuses", required = false) List<OrderStatus> statuses,
            @RequestParam(value = "from-date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(value = "to-date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(value = "driver-id", required = false) UUID driverId,
            @RequestParam(value = "payment-method", required = false) PaymentMethod paymentMethod,
            @RequestParam(value = "payment-status", required = false) PaymentStatus paymentStatus,
            @RequestParam(value = "delivered-from-date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deliveredFromDate,
            @RequestParam(value = "delivered-to-date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deliveredToDate,
            @RequestParam(value = "created-by-user-id", required = false) UUID createdByUserId,
            @RequestParam(value = "overdue-credit", required = false) Boolean overdueCredit,
            @RequestParam(value = "store-search", required = false) String storeSearch,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = orderService.getOrders(
                storeId, statuses, fromDate, toDate, driverId, paymentMethod, paymentStatus,
                deliveredFromDate, deliveredToDate, createdByUserId, overdueCredit, storeSearch,
                page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable UUID id) {
        ApiResponse response = orderService.getOrderById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateOrder(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateOrderDTO dto) {
        ApiResponse response = orderService.updateOrder(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse> confirmOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.confirmOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approveOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.approveOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/assign-driver/{driverId}")
    public ResponseEntity<ApiResponse> assignDriver(@PathVariable UUID id, @PathVariable UUID driverId) {
        ApiResponse response = orderService.assignDriver(id, driverId);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<ApiResponse> dispatchOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.dispatchOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse> deliverOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.deliverOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<ApiResponse> returnOrder(@PathVariable UUID id,
                                                   @Valid @RequestBody ReturnOrderDTO dto) {
        ApiResponse response = orderService.returnOrder(id, dto.getReason());
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable UUID id,
                                                   @Valid @RequestBody CancelOrderDTO dto) {
        ApiResponse response = orderService.cancelOrder(id, dto.getReason());
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.deleteOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse> getStatusHistory(@PathVariable UUID id) {
        ApiResponse response = orderService.getStatusHistory(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
