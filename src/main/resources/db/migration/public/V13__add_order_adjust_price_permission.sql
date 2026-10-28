-- Price adjustments are restricted to Super Admin by default. Other users can
-- receive this permission explicitly through roles and permissions management.
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, 'ORDER_ADJUST_PRICE'
FROM roles r
WHERE LOWER(r.name) = LOWER('Super Admin')
ON CONFLICT DO NOTHING;
