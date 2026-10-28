package com.YM.Beverage.Distribution.Backend.store.specification;

import com.YM.Beverage.Distribution.Backend.order.models.Order;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@RequiredArgsConstructor
public class StoreSpecification implements Specification<Store> {
    private final String searchQuery;
    private final Boolean isActive;
    private final UUID storeId;
    private final UUID assignedDriverId;

    public StoreSpecification(String searchQuery, Boolean isActive) {
        this(searchQuery, isActive, null, null);
    }

    @Override
    public Predicate toPredicate(Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if (storeId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("id"), storeId));
        }

        if (assignedDriverId != null) {
            Subquery<UUID> assignedStores = query.subquery(UUID.class);
            Root<Order> order = assignedStores.from(Order.class);
            assignedStores.select(order.get("store").get("id"))
                    .where(cb.equal(order.get("driver").get("id"), assignedDriverId));
            predicate = cb.and(predicate, root.get("id").in(assignedStores));
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";
            Predicate matchesSearch = cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("phoneNumber")), likePattern),
                    cb.like(cb.lower(root.get("tinNumber")), likePattern),
                    cb.like(cb.lower(root.get("email")), likePattern),
                    cb.like(cb.lower(root.get("city")), likePattern),
                    cb.like(cb.lower(root.get("subCity")), likePattern),
                    cb.like(cb.lower(root.get("address")), likePattern)
            );
            predicate = cb.and(predicate, matchesSearch);
        }

        if (isActive != null) {
            predicate = cb.and(predicate, cb.equal(root.get("active"), isActive));
        }

        return predicate;
    }
}
