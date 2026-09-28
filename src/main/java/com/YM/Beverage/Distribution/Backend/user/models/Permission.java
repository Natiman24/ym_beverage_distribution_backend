package com.YM.Beverage.Distribution.Backend.user.models;

public enum Permission {

    // ─── Orders ───────────────────────────────────────────────────────────────
    ORDER_CREATE,
    ORDER_VIEW,             // DRIVER
    ORDER_UPDATE,
    ORDER_DELETE,
    ORDER_CONFIRM,
    ORDER_APPROVE,
    ORDER_ASSIGN_DRIVER,
    ORDER_DISPATCH,
    ORDER_DELIVER,          // DRIVER
    ORDER_RETURN,           // DRIVER
    ORDER_CONFIRM_DELIVERY,    // DRIVER
    ORDER_CANCEL,

    // ─── Drivers ──────────────────────────────────────────────────────────────
    DRIVER_CREATE,
    DRIVER_VIEW,
    DRIVER_UPDATE,
    DRIVER_DELETE,
    DRIVER_TOGGLE_ACTIVE,
    DRIVER_VIEW_ORDERS,     // DRIVER
    DRIVER_VIEW_PROFILE,    // DRIVER

    // ─── Stores (Customers) ───────────────────────────────────────────────────
    STORE_CREATE,
    STORE_VIEW,             // DRIVER
    STORE_UPDATE,
    STORE_DELETE,
    STORE_TOGGLE_ACTIVE,

    // ─── Store Transactions (Payments) ────────────────────────────────────────
    TRANSACTION_CREATE,
    TRANSACTION_VIEW,       // DRIVER
    TRANSACTION_VIEW_BALANCE,   // DRIVER

    // ─── Products ─────────────────────────────────────────────────────────────
    PRODUCT_CREATE,
    PRODUCT_VIEW,           // DRIVER
    PRODUCT_UPDATE,
    PRODUCT_DELETE,

    // ─── Inventory ────────────────────────────────────────────────────────────
    INVENTORY_VIEW,
    INVENTORY_ADJUST,

    // ─── Suppliers ────────────────────────────────────────────────────────────
    SUPPLIER_CREATE,
    SUPPLIER_VIEW,
    SUPPLIER_UPDATE,
    SUPPLIER_DELETE,

    // ─── Product Lookups (Brands, Categories, Units, Volume Units) ────────────
    LOOKUP_CREATE,
    LOOKUP_VIEW,
    LOOKUP_UPDATE,
    LOOKUP_DELETE,

    // ─── Dashboard ────────────────────────────────────────────────────────────
    DASHBOARD_VIEW,

    // ─── Users ────────────────────────────────────────────────────────────────
    USER_CREATE,
    USER_VIEW,
    USER_UPDATE,
    USER_DELETE,
    USER_TOGGLE_ACTIVE,
    PROFILE_VIEW,           // Any authenticated user — view own profile
    PROFILE_UPDATE,         // Any authenticated user — edit own profile/password

    // ─── Roles & Permissions ──────────────────────────────────────────────────
    ROLE_MANAGE,

    // ─── Reports ──────────────────────────────────────────────────────────────
    REPORT_VIEW
}
