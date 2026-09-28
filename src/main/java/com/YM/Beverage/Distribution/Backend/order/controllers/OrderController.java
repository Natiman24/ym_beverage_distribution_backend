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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ORDER_CREATE')")
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        ApiResponse response = orderService.createOrder(dto, jwtUtil.getUserId(jwtUtil.resolveToken(request)));
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
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
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable UUID id) {
        ApiResponse response = orderService.getOrderById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    public ResponseEntity<ApiResponse> updateOrder(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateOrderDTO dto) {
        ApiResponse response = orderService.updateOrder(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('ORDER_CONFIRM')")
    public ResponseEntity<ApiResponse> confirmOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.confirmOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ORDER_APPROVE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> approveOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.approveOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/assign-driver/{driverId}")
    @PreAuthorize("hasAuthority('ORDER_ASSIGN_DRIVER') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> assignDriver(@PathVariable UUID id, @PathVariable UUID driverId) {
        ApiResponse response = orderService.assignDriver(id, driverId);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/dispatch")
    @PreAuthorize("hasAuthority('ORDER_DISPATCH') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> dispatchOrder(@PathVariable UUID id,
                                                      @Valid @RequestBody DispatchOrderDTO dto) {
        ApiResponse response = orderService.dispatchOrder(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasAuthority('ORDER_DELIVER') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> deliverOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.deliverOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAuthority('ORDER_RETURN') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> returnOrder(@PathVariable UUID id,
                                                   @Valid @RequestBody ReturnOrderDTO dto) {
        ApiResponse response = orderService.returnOrder(id, dto.getReason());
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ORDER_CANCEL')")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable UUID id,
                                                   @Valid @RequestBody CancelOrderDTO dto) {
        ApiResponse response = orderService.cancelOrder(id, dto.getReason());
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_DELETE')")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable UUID id) {
        ApiResponse response = orderService.deleteOrder(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}/status-history")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    public ResponseEntity<ApiResponse> getStatusHistory(@PathVariable UUID id) {
        ApiResponse response = orderService.getStatusHistory(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
