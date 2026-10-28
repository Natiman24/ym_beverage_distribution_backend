-- Product details are a company-only resource. New permissions are granted
-- automatically only to Super Admin; other company roles can opt in through
-- role and permission management.
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, permission_name
FROM roles r
CROSS JOIN (VALUES
    ('PRODUCT_VIEW_DETAIL'),
    ('PRODUCT_VIEW_QUANTITY')
) AS permissions(permission_name)
WHERE LOWER(r.name) = LOWER('Super Admin')
ON CONFLICT DO NOTHING;

INSERT INTO role_permission_scopes (role_id, permission, scope)
SELECT r.id, permission_name, 'ALL'
FROM roles r
CROSS JOIN (VALUES
    ('PRODUCT_VIEW_DETAIL'),
    ('PRODUCT_VIEW_QUANTITY')
) AS permissions(permission_name)
WHERE LOWER(r.name) = LOWER('Super Admin')
ON CONFLICT DO NOTHING;
