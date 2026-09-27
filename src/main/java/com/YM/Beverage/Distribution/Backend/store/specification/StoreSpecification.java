package com.YM.Beverage.Distribution.Backend.store.specification;

import com.YM.Beverage.Distribution.Backend.store.models.Store;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class StoreSpecification implements Specification<Store> {
    private final String searchQuery;

    @Override
    public Predicate toPredicate(Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), likePattern);
            Predicate phoneNumberPredicate = cb.like(cb.lower(root.get("phoneNumber")), likePattern);
            Predicate tinNumberPredicate = cb.like(cb.lower(root.get("tinNumber")), likePattern);
            predicate = cb.and(predicate, namePredicate, phoneNumberPredicate, tinNumberPredicate);
        }

        return predicate;
    }
}
