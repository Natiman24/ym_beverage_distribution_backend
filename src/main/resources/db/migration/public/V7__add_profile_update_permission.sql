-- Allow users to edit their own profile and change their own password.
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, 'PROFILE_UPDATE'
FROM roles r
WHERE r.name IN ('Super Admin', 'Driver')
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission = 'PROFILE_UPDATE'
  );
