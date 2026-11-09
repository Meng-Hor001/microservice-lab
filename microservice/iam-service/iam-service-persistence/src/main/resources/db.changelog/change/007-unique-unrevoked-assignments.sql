--liquibase formatted sql

--changeset menghor:007-unique-unrevoked-assignments dbms:postgresql

CREATE UNIQUE INDEX uk_user_roles_unrevoked
    ON user_roles (user_id, role_id)
    WHERE revoked_at IS NULL;

CREATE UNIQUE INDEX uk_role_permissions_unrevoked
    ON role_permissions (role_id, permission_id)
    WHERE revoked_at IS NULL;

CREATE UNIQUE INDEX uk_user_permissions_unrevoked
    ON user_permissions (user_id, permission_id)
    WHERE revoked_at IS NULL;
