--liquibase formatted sql

--changeset menghor:006-seed-user-role-permissions dbms:postgresql

INSERT INTO role_permissions (
    role_id,
    permission_id,
    assigned_by,
    reason
)
SELECT
    r.id,
    p.id,
    'system',
    'Initial standard user permissions'
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.name IN (
                 'user:read',
                 'role:read',
                 'permission:read'
    )
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions existing
    WHERE existing.role_id = r.id
      AND existing.permission_id = p.id
      AND existing.revoked_at IS NULL
);