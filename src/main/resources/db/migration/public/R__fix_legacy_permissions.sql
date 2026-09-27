-- ============================================================
-- R__fix_legacy_permissions
-- Repeatable migration: renames legacy permission values that
-- no longer exist in the Permission enum.
-- Runs on every checksum change — idempotent by design.
-- ============================================================

-- ── role_permissions ─────────────────────────────────────────
UPDATE role_permissions SET permission = 'STORE_CREATE'       WHERE permission = 'CUSTOMER_CREATE';
UPDATE role_permissions SET permission = 'STORE_VIEW'         WHERE permission = 'CUSTOMER_VIEW';
UPDATE role_permissions SET permission = 'STORE_UPDATE'       WHERE permission = 'CUSTOMER_UPDATE';
UPDATE role_permissions SET permission = 'STORE_DELETE'       WHERE permission = 'CUSTOMER_DELETE';
UPDATE role_permissions SET permission = 'TRANSACTION_CREATE' WHERE permission = 'PAYMENT_CREATE';
UPDATE role_permissions SET permission = 'TRANSACTION_VIEW'   WHERE permission = 'PAYMENT_VIEW';
UPDATE role_permissions SET permission = 'TRANSACTION_CREATE' WHERE permission = 'PAYMENT_UPDATE';
UPDATE role_permissions SET permission = 'ORDER_CONFIRM'      WHERE permission = 'ORDER_SUBMIT';

-- Remove duplicates that the above updates may produce
DELETE FROM role_permissions
WHERE ctid NOT IN (
    SELECT MIN(ctid)
    FROM role_permissions
    GROUP BY role_id, permission
);

-- ── user_permissions ─────────────────────────────────────────
UPDATE user_permissions SET permission = 'STORE_CREATE'       WHERE permission = 'CUSTOMER_CREATE';
UPDATE user_permissions SET permission = 'STORE_VIEW'         WHERE permission = 'CUSTOMER_VIEW';
UPDATE user_permissions SET permission = 'STORE_UPDATE'       WHERE permission = 'CUSTOMER_UPDATE';
UPDATE user_permissions SET permission = 'STORE_DELETE'       WHERE permission = 'CUSTOMER_DELETE';
UPDATE user_permissions SET permission = 'TRANSACTION_CREATE' WHERE permission = 'PAYMENT_CREATE';
UPDATE user_permissions SET permission = 'TRANSACTION_VIEW'   WHERE permission = 'PAYMENT_VIEW';
UPDATE user_permissions SET permission = 'TRANSACTION_CREATE' WHERE permission = 'PAYMENT_UPDATE';
UPDATE user_permissions SET permission = 'ORDER_CONFIRM'      WHERE permission = 'ORDER_SUBMIT';

DELETE FROM user_permissions
WHERE ctid NOT IN (
    SELECT MIN(ctid)
    FROM user_permissions
    GROUP BY user_id, permission
);
