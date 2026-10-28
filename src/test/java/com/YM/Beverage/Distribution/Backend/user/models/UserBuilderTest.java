package com.YM.Beverage.Distribution.Backend.user.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserBuilderTest {

    @Test
    void newUserIsActiveByDefault() {
        assertTrue(User.builder().build().isActive());
    }
}
