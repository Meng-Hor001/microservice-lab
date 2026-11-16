--liquibase formatted sql

--changeset menghor:012-seed-audit-read-permission dbms:postgresql
INSERT INTO permissions (
    uuid, name, resource, action, description,
    is_system, is_active, created_by, updated_by
)
VALUES (
           '20000000-0000-4000-8000-000000000016',
           'audit:read',
           'audit',
           'read',
           'View system audit logs',
           TRUE,
           TRUE,
           'system',
           'system'
       );

INSERT INTO role_permissions (role_id, permission_id, assigned_by, reason)
SELECT r.id, p.id, 'system', 'Allow administrators to view audit logs'
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.name = 'audit:read';