package com.YM.Beverage.Distribution.Backend.user.specification;

import com.YM.Beverage.Distribution.Backend.user.models.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class RoleSpecification implements Specification<Role> {
    private final String searchQuery;

    @Override
    public Predicate toPredicate(Root<Role> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likePattern = "%" + searchQuery.toLowerCase() + "%";
            predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), likePattern));
        }

        return predicate;
    }
}
