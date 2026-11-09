
--liquibase formatted sql

--changeset menghor:009-seed-client-create-permission dbms:postgresql

INSERT INTO permissions (
    uuid, name, resource, action, description,
    is_system, is_active, created_by, updated_by
)
VALUES (
           '20000000-0000-4000-8000-000000000015',
           'client:create',
           'client',
           'create',
           'Register OAuth clients',
           TRUE,
           TRUE,
           'system',
           'system'
       );

INSERT INTO role_permissions (
    role_id, permission_id, assigned_by, reason
)
SELECT
    r.id,
    p.id,
    'system',
    'Allow administrators to register OAuth clients'
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.name = 'client:create';