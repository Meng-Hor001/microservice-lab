--liquibase formatted sql

--changeset menghor:014-system-settings dbms:postgresql
CREATE TABLE system_settings (
    id                          SMALLINT PRIMARY KEY CHECK (id = 1),   -- exactly one row
    allow_self_registration     BOOLEAN  NOT NULL,
    allow_social_login          BOOLEAN  NOT NULL,
    max_failed_attempts         INT      NOT NULL,
    lockout_minutes             INT      NOT NULL,
    password_min_length         INT      NOT NULL,
    password_require_uppercase  BOOLEAN  NOT NULL,
    password_require_lowercase  BOOLEAN  NOT NULL,
    password_require_digit      BOOLEAN  NOT NULL,
    password_require_symbol     BOOLEAN  NOT NULL,
    password_expiry_days        INT      NOT NULL,   -- 0 = never
    session_idle_minutes        INT      NOT NULL,
    session_max_hours           INT      NOT NULL,
    access_token_minutes        INT      NOT NULL,
    refresh_token_days          INT      NOT NULL,
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(100) NOT NULL,
    version                     BIGINT   NOT NULL DEFAULT 0
);

INSERT INTO system_settings (
    id, allow_self_registration, allow_social_login, max_failed_attempts, lockout_minutes,
    password_min_length, password_require_uppercase, password_require_lowercase,
    password_require_digit, password_require_symbol, password_expiry_days,
    session_idle_minutes, session_max_hours, access_token_minutes, refresh_token_days,
    updated_at, updated_by, version
)
VALUES (
    1, TRUE, TRUE, 5, 15,
    8, TRUE, TRUE, TRUE, FALSE, 0,
    30, 24, 15, 30,
    CURRENT_TIMESTAMP, 'system', 0
);

INSERT INTO permissions (uuid, name, resource, action, description, is_system, is_active, created_by, updated_by)
VALUES
    ('20000000-0000-4000-8000-000000000020', 'settings:read',   'settings', 'read',   'View system configuration',   TRUE, TRUE, 'system', 'system'),
    ('20000000-0000-4000-8000-000000000021', 'settings:update', 'settings', 'update', 'Change system configuration', TRUE, TRUE, 'system', 'system');

INSERT INTO role_permissions (role_id, permission_id, assigned_by, reason)
SELECT r.id, p.id, 'system', 'Allow administrators to manage system configuration'
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('settings:read', 'settings:update');
