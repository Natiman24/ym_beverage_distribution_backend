package com.YM.Beverage.Distribution.Backend.user.specification;

import com.YM.Beverage.Distribution.Backend.user.models.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {
    private final String searchQuery;
    private final Boolean isActive;
    private final Boolean isDeactivated;
    private final List<UUID> roleIds;
    private final UUID storeId;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";
            Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), searchPattern);
            Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), searchPattern);
            Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchPattern);
            predicate = cb.or(firstNamePredicate, lastNamePredicate, emailPredicate);
        }

        if (isActive != null) {
            predicate = cb.and(predicate, cb.equal(root.get("isActive"), isActive));
        }

        if (isDeactivated != null) {
            predicate = cb.and(predicate, cb.equal(root.get("isDeactivated"), isDeactivated));
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            predicate = cb.and(predicate, root.join("roles").get("id").in(roleIds));
        }

        if (storeId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("store").get("id"), storeId));
        }

        return predicate;
    }
}
