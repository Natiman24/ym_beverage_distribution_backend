-- Super Admin can cancel orders through Processing by default. Other users can
-- receive this permission explicitly through roles and permissions management.
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, 'ORDER_CANCEL_UNTIL_DISPATCH'
FROM roles r
WHERE LOWER(r.name) = LOWER('Super Admin')
ON CONFLICT DO NOTHING;
