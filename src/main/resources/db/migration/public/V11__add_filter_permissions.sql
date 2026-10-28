-- Keep existing roles working as before: anyone who could view a resource can
-- continue using that resource's existing filters after filter authorization
-- becomes explicit.
WITH permission_mapping(filter_permission, view_permission) AS (
    VALUES
        ('ORDER_FILTER', 'ORDER_VIEW'),
        ('DRIVER_FILTER', 'DRIVER_VIEW'),
        ('STORE_FILTER', 'STORE_VIEW'),
        ('PRODUCT_FILTER', 'PRODUCT_VIEW'),
        ('SUPPLIER_FILTER', 'SUPPLIER_VIEW'),
        ('LOOKUP_FILTER', 'LOOKUP_VIEW'),
        ('USER_FILTER', 'USER_VIEW'),
        ('ROLE_FILTER', 'ROLE_MANAGE')
)
INSERT INTO role_permissions (role_id, permission)
SELECT DISTINCT grants.role_id, mapping.filter_permission
FROM role_permissions grants
JOIN permission_mapping mapping ON mapping.view_permission = grants.permission
ON CONFLICT DO NOTHING;

WITH permission_mapping(filter_permission, view_permission) AS (
    VALUES
        ('ORDER_FILTER', 'ORDER_VIEW'),
        ('DRIVER_FILTER', 'DRIVER_VIEW'),
        ('STORE_FILTER', 'STORE_VIEW'),
        ('PRODUCT_FILTER', 'PRODUCT_VIEW'),
        ('SUPPLIER_FILTER', 'SUPPLIER_VIEW'),
        ('LOOKUP_FILTER', 'LOOKUP_VIEW'),
        ('USER_FILTER', 'USER_VIEW'),
        ('ROLE_FILTER', 'ROLE_MANAGE')
)
INSERT INTO role_permission_scopes (role_id, permission, scope)
SELECT filter_grants.role_id,
       mapping.filter_permission,
       COALESCE(view_scopes.scope, 'ALL')
FROM role_permissions filter_grants
JOIN permission_mapping mapping ON mapping.filter_permission = filter_grants.permission
LEFT JOIN role_permission_scopes view_scopes
       ON view_scopes.role_id = filter_grants.role_id
      AND view_scopes.permission = mapping.view_permission
ON CONFLICT DO NOTHING;

-- Do the same for permissions granted directly to individual users.
WITH permission_mapping(filter_permission, view_permission) AS (
    VALUES
        ('ORDER_FILTER', 'ORDER_VIEW'),
        ('DRIVER_FILTER', 'DRIVER_VIEW'),
        ('STORE_FILTER', 'STORE_VIEW'),
        ('PRODUCT_FILTER', 'PRODUCT_VIEW'),
        ('SUPPLIER_FILTER', 'SUPPLIER_VIEW'),
        ('LOOKUP_FILTER', 'LOOKUP_VIEW'),
        ('USER_FILTER', 'USER_VIEW'),
        ('ROLE_FILTER', 'ROLE_MANAGE')
)
INSERT INTO user_permissions (user_id, permission)
SELECT DISTINCT grants.user_id, mapping.filter_permission
FROM user_permissions grants
JOIN permission_mapping mapping ON mapping.view_permission = grants.permission
ON CONFLICT DO NOTHING;

WITH permission_mapping(filter_permission, view_permission) AS (
    VALUES
        ('ORDER_FILTER', 'ORDER_VIEW'),
        ('DRIVER_FILTER', 'DRIVER_VIEW'),
        ('STORE_FILTER', 'STORE_VIEW'),
        ('PRODUCT_FILTER', 'PRODUCT_VIEW'),
        ('SUPPLIER_FILTER', 'SUPPLIER_VIEW'),
        ('LOOKUP_FILTER', 'LOOKUP_VIEW'),
        ('USER_FILTER', 'USER_VIEW'),
        ('ROLE_FILTER', 'ROLE_MANAGE')
)
INSERT INTO user_permission_scopes (user_id, permission, scope)
SELECT filter_grants.user_id,
       mapping.filter_permission,
       COALESCE(view_scopes.scope, 'ALL')
FROM user_permissions filter_grants
JOIN permission_mapping mapping ON mapping.filter_permission = filter_grants.permission
LEFT JOIN user_permission_scopes view_scopes
       ON view_scopes.user_id = filter_grants.user_id
      AND view_scopes.permission = mapping.view_permission
ON CONFLICT DO NOTHING;
