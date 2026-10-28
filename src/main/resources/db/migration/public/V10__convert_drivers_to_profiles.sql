-- The existing drivers table already has the correct one-to-one shape:
-- one optional row per user. It is now treated as a capability profile,
-- rather than as a joined-inheritance child entity.
ALTER TABLE drivers
    ADD COLUMN available_for_delivery BOOLEAN NOT NULL DEFAULT TRUE;

-- A regular Driver role should only grant access to assigned operational data.
-- A company user with another broader role (for example Super Admin) keeps the
-- broader grant because effective permission resolution chooses the broadest scope.
UPDATE role_permission_scopes rps
SET scope = 'ASSIGNED'
FROM roles r
WHERE rps.role_id = r.id
  AND LOWER(r.name) = 'driver'
  AND rps.permission IN (
      'ORDER_VIEW',
      'ORDER_DELIVER',
      'ORDER_RETURN',
      'DRIVER_VIEW_ORDERS',
      'STORE_VIEW',
      'TRANSACTION_VIEW',
      'TRANSACTION_VIEW_BALANCE'
  );
