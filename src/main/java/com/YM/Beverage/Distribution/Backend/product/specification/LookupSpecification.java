package com.YM.Beverage.Distribution.Backend.product.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generic specification for ProductCategory, ProductUnit, and VolumeUnit.
 * All three share the same fields: name, description, active.
 */
@RequiredArgsConstructor
public class LookupSpecification<T> implements Specification<T> {

    private final String searchQuery;
    private final Boolean active;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if (searchQuery != null && !searchQuery.isBlank()) {
            String like = "%" + searchQuery.toLowerCase() + "%";
            predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), like));
        }

        if (active != null) {
            predicate = cb.and(predicate, cb.equal(root.get("active"), active));
        }

        return predicate;
    }
}
