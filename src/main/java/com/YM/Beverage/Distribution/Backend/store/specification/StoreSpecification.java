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
    private final Boolean isActive;

    @Override
    public Predicate toPredicate(Root<Store> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

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
