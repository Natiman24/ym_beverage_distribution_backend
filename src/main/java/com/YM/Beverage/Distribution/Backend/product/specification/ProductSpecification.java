package com.YM.Beverage.Distribution.Backend.product.specification;

import com.YM.Beverage.Distribution.Backend.product.models.Product;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ProductSpecification implements Specification<Product> {

    private final String searchQuery;
    private final List<UUID> categoryIds;
    private final List<UUID> unitIds;
    private final List<UUID> brandIds;
    private final Boolean active;

    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        if (searchQuery != null && !searchQuery.isBlank()) {
            String like = "%" + searchQuery.toLowerCase() + "%";
            predicate = cb.and(predicate, cb.or(
                    cb.like(cb.lower(root.get("name")), like)
            ));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicate = cb.and(predicate, root.get("category").get("id").in(categoryIds));
        }

        if (unitIds != null && !unitIds.isEmpty()) {
            predicate = cb.and(predicate, root.get("unit").get("id").in(unitIds));
        }

        if (brandIds != null && !brandIds.isEmpty()) {
            predicate = cb.and(predicate, root.get("brand").get("id").in(brandIds));
        }

        if (active != null) {
            predicate = cb.and(predicate, cb.equal(root.get("active"), active));
        }

        return predicate;
    }
}
