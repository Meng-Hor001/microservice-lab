--liquibase formatted sql

--changeset codex:010-account-security dbms:postgresql
CREATE TABLE account_sessions (
    id UUID PRIMARY KEY,
    user_uuid UUID NOT NULL REFERENCES users(uuid),
    authorization_id VARCHAR(100) NOT NULL UNIQUE,
    device_type VARCHAR(20) NOT NULL,
    device VARCHAR(255) NOT NULL,
    browser VARCHAR(100) NOT NULL,
    os VARCHAR(100) NOT NULL,
    ip_address VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL DEFAULT 'Unknown',
    signed_in_at TIMESTAMPTZ NOT NULL,
    last_active_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);
CREATE INDEX account_sessions_user_idx ON account_sessions(user_uuid, expires_at);

CREATE TABLE account_login_history (
    id UUID PRIMARY KEY,
    user_uuid UUID NOT NULL REFERENCES users(uuid),
    success BOOLEAN NOT NULL,
    method VARCHAR(50) NOT NULL,
    device VARCHAR(255) NOT NULL,
    browser VARCHAR(100) NOT NULL,
    os VARCHAR(100) NOT NULL,
    ip_address VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL DEFAULT 'Unknown',
    occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX account_login_history_user_idx ON account_login_history(user_uuid, occurred_at DESC);

CREATE TABLE account_activities (
    id UUID PRIMARY KEY,
    user_uuid UUID NOT NULL REFERENCES users(uuid),
    event_type VARCHAR(100) NOT NULL,
    detail VARCHAR(1000),
    occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX account_activities_user_idx ON account_activities(user_uuid, occurred_at DESC);

CREATE TABLE account_deactivation_requests (
    user_uuid UUID PRIMARY KEY REFERENCES users(uuid),
    reason VARCHAR(50),
    status VARCHAR(20) NOT NULL CHECK (status IN ('requested', 'cancelled')),
    requested_at TIMESTAMPTZ NOT NULL,
    cancelled_at TIMESTAMPTZ
);

CREATE TABLE account_password_resets (
    user_uuid UUID PRIMARY KEY REFERENCES users(uuid),
    token_hash CHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ
);
