--liquibase formatted sql

--changeset menghor:013-seed-client-management-permissions dbms:postgresql
INSERT INTO permissions (
    uuid, name, resource, action, description,
    is_system, is_active, created_by, updated_by
)
VALUES
    ('20000000-0000-4000-8000-000000000017', 'client:read',   'client', 'read',   'View OAuth clients',   TRUE, TRUE, 'system', 'system'),
    ('20000000-0000-4000-8000-000000000018', 'client:update', 'client', 'update', 'Update OAuth clients and rotate secrets', TRUE, TRUE, 'system', 'system'),
    ('20000000-0000-4000-8000-000000000019', 'client:delete', 'client', 'delete', 'Delete OAuth clients', TRUE, TRUE, 'system', 'system');

INSERT INTO role_permissions (role_id, permission_id, assigned_by, reason)
SELECT r.id, p.id, 'system', 'Allow administrators to manage OAuth clients'
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('client:read', 'client:update', 'client:delete');
