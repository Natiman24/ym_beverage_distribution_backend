package com.YM.Beverage.Distribution.Backend.store.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreBuilderTest {

    @Test
    void newStoreIsActiveByDefault() {
        assertTrue(Store.builder().build().isActive());
    }
}
