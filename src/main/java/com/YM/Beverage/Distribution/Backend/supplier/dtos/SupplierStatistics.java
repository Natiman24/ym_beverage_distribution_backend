package com.YM.Beverage.Distribution.Backend.supplier.dtos;

import java.math.BigDecimal;

public interface SupplierStatistics {
    Long getTotalItemsSupplied();
    Long getTotalQuantitySupplied();
    BigDecimal getTotalPurchaseAmount();
}
