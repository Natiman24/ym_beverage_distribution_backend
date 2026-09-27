-- ============================================================
-- V2: Seed all permissions into Super Admin role &
--     create Driver role with driver-specific permissions
-- ============================================================

-- ── Ensure Super Admin role exists ───────────────────────────
INSERT INTO roles (id, name)
VALUES (gen_random_uuid(), 'Super Admin')
ON CONFLICT (name) DO NOTHING;

-- ── Grant ALL permissions to Super Admin ─────────────────────
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission
FROM roles r
         CROSS JOIN (VALUES
    -- Orders
    ('ORDER_CREATE'),
    ('ORDER_VIEW'),
    ('ORDER_UPDATE'),
    ('ORDER_DELETE'),
    ('ORDER_CONFIRM'),
    ('ORDER_APPROVE'),
    ('ORDER_ASSIGN_DRIVER'),
    ('ORDER_DISPATCH'),
    ('ORDER_DELIVER'),
    ('ORDER_RETURN'),
    ('ORDER_CONFIRM_DELIVERY'),
    ('ORDER_CANCEL'),
    -- Drivers
    ('DRIVER_CREATE'),
    ('DRIVER_VIEW'),
    ('DRIVER_UPDATE'),
    ('DRIVER_DELETE'),
    ('DRIVER_TOGGLE_ACTIVE'),
    ('DRIVER_VIEW_ORDERS'),
    ('DRIVER_VIEW_PROFILE'),
    -- Stores
    ('STORE_CREATE'),
    ('STORE_VIEW'),
    ('STORE_UPDATE'),
    ('STORE_DELETE'),
    ('STORE_TOGGLE_ACTIVE'),
    -- Store Transactions
    ('TRANSACTION_CREATE'),
    ('TRANSACTION_VIEW'),
    ('TRANSACTION_VIEW_BALANCE'),
    -- Products
    ('PRODUCT_CREATE'),
    ('PRODUCT_VIEW'),
    ('PRODUCT_UPDATE'),
    ('PRODUCT_DELETE'),
    -- Inventory
    ('INVENTORY_VIEW'),
    ('INVENTORY_ADJUST'),
    -- Suppliers
    ('SUPPLIER_CREATE'),
    ('SUPPLIER_VIEW'),
    ('SUPPLIER_UPDATE'),
    ('SUPPLIER_DELETE'),
    -- Lookups
    ('LOOKUP_CREATE'),
    ('LOOKUP_VIEW'),
    ('LOOKUP_UPDATE'),
    ('LOOKUP_DELETE'),
    -- Dashboard
    ('DASHBOARD_VIEW'),
    -- Users
    ('USER_CREATE'),
    ('USER_VIEW'),
    ('USER_UPDATE'),
    ('USER_DELETE'),
    ('USER_TOGGLE_ACTIVE'),
    ('PROFILE_VIEW'),
    -- Roles & Permissions
    ('ROLE_MANAGE'),
    -- Reports
    ('REPORT_VIEW')
) AS p(permission)
WHERE r.name = 'Super Admin'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission = p.permission
);

-- ── Create Driver role ────────────────────────────────────────
INSERT INTO roles (id, name)
VALUES (gen_random_uuid(), 'Driver')
ON CONFLICT (name) DO NOTHING;

-- ── Grant driver-specific permissions to Driver role ─────────
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission
FROM roles r
         CROSS JOIN (VALUES
    ('ORDER_VIEW'),
    ('ORDER_DELIVER'),
    ('ORDER_RETURN'),
    ('DRIVER_VIEW_ORDERS'),
    ('DRIVER_VIEW_PROFILE'),
    ('STORE_VIEW'),
    ('TRANSACTION_VIEW'),
    ('TRANSACTION_VIEW_BALANCE'),
    ('PRODUCT_VIEW'),
    ('PROFILE_VIEW')
) AS p(permission)
WHERE r.name = 'Driver'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission = p.permission
);
