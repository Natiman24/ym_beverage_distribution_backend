package com.YM.Beverage.Distribution.Backend.order.specification;

import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import com.YM.Beverage.Distribution.Backend.order.models.Order;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderSpecification implements Specification<Order> {

    private final UUID storeId;
    private final List<OrderStatus> statuses;
    private final LocalDateTime fromDate;
    private final LocalDateTime toDate;
    private final UUID driverId;
    private final PaymentMethod paymentMethod;
    private final PaymentStatus paymentStatus;
    private final LocalDateTime deliveredFromDate;
    private final LocalDateTime deliveredToDate;
    private final UUID createdByUserId;
    private final Boolean overdueCredit;
    private final String storeSearch;

    public OrderSpecification(UUID storeId, List<OrderStatus> statuses,
                              LocalDateTime fromDate, LocalDateTime toDate) {
        this(storeId, statuses, fromDate, toDate, null, null, null,
                null, null, null, null, null);
    }

    @Override
    public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if (storeId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("store").get("id"), storeId));
        }

        if (statuses != null && !statuses.isEmpty()) {
            predicate = cb.and(predicate, root.get("status").in(statuses));
        }

        if (fromDate != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("orderDate"), fromDate));
        }

        if (toDate != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("orderDate"), toDate));
        }

        if (driverId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("driver").get("id"), driverId));
        }

        if (paymentMethod != null) {
            predicate = cb.and(predicate, cb.equal(root.get("paymentMethod"), paymentMethod));
        }

        if (paymentStatus != null) {
            predicate = cb.and(predicate, cb.equal(root.get("paymentStatus"), paymentStatus));
        }

        if (deliveredFromDate != null) {
            predicate = cb.and(predicate,
                    cb.greaterThanOrEqualTo(root.get("deliveredAt"), deliveredFromDate));
        }

        if (deliveredToDate != null) {
            predicate = cb.and(predicate,
                    cb.lessThanOrEqualTo(root.get("deliveredAt"), deliveredToDate));
        }

        if (createdByUserId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("createdBy").get("id"), createdByUserId));
        }

        if (Boolean.TRUE.equals(overdueCredit)) {
            predicate = cb.and(predicate,
                    cb.equal(root.get("paymentMethod"), PaymentMethod.CREDIT),
                    root.get("paymentStatus").in(PaymentStatus.UNPAID, PaymentStatus.PARTIALLY_PAID),
                    cb.lessThan(root.get("paymentDueDate"), LocalDate.now()));
        }

        if (storeSearch != null && !storeSearch.isBlank()) {
            String pattern = "%" + storeSearch.trim().toLowerCase() + "%";
            predicate = cb.and(predicate,
                    cb.like(cb.lower(root.get("store").get("name")), pattern));
        }

        return predicate;
    }
}
