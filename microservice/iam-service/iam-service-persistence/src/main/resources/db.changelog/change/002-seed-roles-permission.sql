--liquibase formatted sql

--changeset menghor:002-seed-roles-permissions dbms:postgresql

INSERT INTO roles (
    uuid, name, description,
    is_system, is_active, created_by, updated_by
)
VALUES
    (
        '10000000-0000-4000-8000-000000000001',
        'ADMIN',
        'System administrator',
        TRUE, TRUE, 'system', 'system'
    ),
    (
        '10000000-0000-4000-8000-000000000002',
        'USER',
        'Standard user',
        TRUE, TRUE, 'system', 'system'
    );

INSERT INTO permissions (
    uuid, name, resource, action, description,
    is_system, is_active, created_by, updated_by
)
SELECT
    seed.uuid::UUID,
    seed.resource || ':' || seed.action,
    seed.resource,
    seed.action,
    seed.description,
    TRUE,
    TRUE,
    'system',
    'system'
FROM (
         VALUES
             ('20000000-0000-4000-8000-000000000001', 'user', 'create', 'Create users'),
             ('20000000-0000-4000-8000-000000000002', 'user', 'read', 'Read users'),
             ('20000000-0000-4000-8000-000000000003', 'user', 'update', 'Update users'),
             ('20000000-0000-4000-8000-000000000004', 'user', 'delete', 'Delete users'),
             ('20000000-0000-4000-8000-000000000005', 'user', 'assign', 'Assign user access'),
             ('20000000-0000-4000-8000-000000000006', 'role', 'create', 'Create roles'),
             ('20000000-0000-4000-8000-000000000007', 'role', 'read', 'Read roles'),
             ('20000000-0000-4000-8000-000000000008', 'role', 'update', 'Update roles'),
             ('20000000-0000-4000-8000-000000000009', 'role', 'delete', 'Delete roles'),
             ('20000000-0000-4000-8000-000000000010', 'role', 'assign', 'Assign role permissions'),
             ('20000000-0000-4000-8000-000000000011', 'permission', 'create', 'Create permissions'),
             ('20000000-0000-4000-8000-000000000012', 'permission', 'read', 'Read permissions'),
             ('20000000-0000-4000-8000-000000000013', 'permission', 'update', 'Update permissions'),
             ('20000000-0000-4000-8000-000000000014', 'permission', 'delete', 'Delete permissions')
     ) AS seed(uuid, resource, action, description);

INSERT INTO role_permissions (
    role_id, permission_id, assigned_by, reason
)
SELECT
    r.id,
    p.id,
    'system',
    'Initial administrator permissions'
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN (
                 'user:create', 'user:read', 'user:update',
                 'user:delete', 'user:assign',
                 'role:create', 'role:read', 'role:update',
                 'role:delete', 'role:assign',
                 'permission:create', 'permission:read',
                 'permission:update', 'permission:delete'
    );