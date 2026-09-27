package com.YM.Beverage.Distribution.Backend.store.enums;

public enum TransactionType {
    /**
     * Goods delivered but not paid — increases outstanding balance.
     */
    CREDIT,

    /**
     * Full cash payment at time of delivery — no outstanding balance added.
     */
    CASH_PAYMENT,

    /**
     * Store repays a previous credit — decreases outstanding balance.
     */
    CREDIT_REPAYMENT,

    /**
     * Refund issued to the store — decreases outstanding balance.
     */
    REFUND
}
