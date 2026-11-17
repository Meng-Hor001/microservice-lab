--liquibase formatted sql

--changeset menghor:015-user-avatars dbms:postgresql
-- One uploaded profile photo per user. users.picture_url points at /api/avatars/{uuid}.
CREATE TABLE user_avatars (
    user_id       BIGINT       PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    content_type  VARCHAR(50)  NOT NULL,
    data          BYTEA        NOT NULL,
    size_bytes    INT          NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
