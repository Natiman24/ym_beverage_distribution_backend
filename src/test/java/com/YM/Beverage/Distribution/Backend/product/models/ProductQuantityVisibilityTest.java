package com.YM.Beverage.Distribution.Backend.product.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductQuantityVisibilityTest {

    private final Product product = Product.builder()
            .name("Water")
            .quantity(24)
            .thresholdQuantity(5)
            .sellingPrice(BigDecimal.TEN)
            .build();

    @Test
    void omitsQuantityFromListAndDetailResponsesWithoutPermission() {
        assertNull(product.toListResponseDTO(false).getQuantity());
        assertNull(product.toSingleResponseDTO(false).getQuantity());
    }

    @Test
    void includesQuantityFromListAndDetailResponsesWithPermission() {
        assertEquals(24, product.toListResponseDTO(true).getQuantity());
        assertEquals(24, product.toSingleResponseDTO(true).getQuantity());
    }
}
