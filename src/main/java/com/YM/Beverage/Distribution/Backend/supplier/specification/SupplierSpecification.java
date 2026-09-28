package com.YM.Beverage.Distribution.Backend.supplier.specification;

import com.YM.Beverage.Distribution.Backend.supplier.models.Supplier;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class SupplierSpecification implements Specification<Supplier> {
    private final String searchQuery;
    private final Boolean active;

    @Override
    public Predicate toPredicate(Root<Supplier> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if( searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), likePattern);
            Predicate phonePredicate = cb.like(cb.lower(root.get("phoneNumber")), likePattern);
            Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), likePattern);
            predicate = cb.and(predicate, cb.or(namePredicate, phonePredicate, descriptionPredicate));
        }

        if (active != null) predicate = cb.and(predicate, cb.equal(root.get("active"), active));

        return predicate;
    }
}
