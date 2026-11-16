package kh.edu.istad.platform.infrastructure.account;

import kh.edu.istad.platform.iam.application.dto.AccountActivityResult;
import kh.edu.istad.platform.iam.application.dto.AccountSessionResult;
import kh.edu.istad.platform.iam.application.dto.ClientInfo;
import kh.edu.istad.platform.iam.application.dto.DeactivationResult;
import kh.edu.istad.platform.iam.application.dto.LoginHistoryResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.SuspiciousLoginResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.InvalidAccountSecurityRequestException;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class JdbcAccountSecurityRepository implements AccountSecurityRepository {
    private final JdbcTemplate jdbc;

    public JdbcAccountSecurityRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<AccountSessionResult> sessions(UUID userId, UUID currentSessionId, Instant now) {
        return jdbc.query("""
                SELECT * FROM account_sessions
                WHERE user_uuid = ? AND revoked_at IS NULL AND expires_at > ?
                ORDER BY signed_in_at DESC, id
                """, (rs, row) -> new AccountSessionResult(
                rs.getObject("id", UUID.class), java.util.Objects.equals(currentSessionId, rs.getObject("id", UUID.class)),
                rs.getString("device_type"), rs.getString("device"), rs.getString("browser"),
                rs.getString("os"), rs.getString("ip_address"), rs.getString("location"),
                rs.getTimestamp("signed_in_at").toInstant(), rs.getTimestamp("last_active_at").toInstant()
        ), userId, timestamp(now));
    }

    @Override
    public int revoke(UUID userId, UUID onlySessionId, UUID excludedSessionId, Instant now) {
        String conditions = "user_uuid = ? AND revoked_at IS NULL AND expires_at > ?";
        var arguments = new ArrayList<Object>();
        arguments.add(userId);
        arguments.add(timestamp(now));
        if (onlySessionId != null) {
            conditions += " AND id = ?";
            arguments.add(onlySessionId);
        }
        if (excludedSessionId != null) {
            conditions += " AND id <> ?";
            arguments.add(excludedSessionId);
        }
        var ids = jdbc.query("SELECT id, authorization_id FROM account_sessions WHERE " + conditions + " FOR UPDATE",
                (rs, row) -> new SessionReference(rs.getObject("id", UUID.class), rs.getString("authorization_id")),
                arguments.toArray());
        for (var session : ids) {
            jdbc.update("UPDATE account_sessions SET revoked_at = ? WHERE id = ? AND user_uuid = ?",
                    timestamp(now), session.id(), userId);
            jdbc.update("DELETE FROM oauth2_authorization WHERE id = ?", session.authorizationId());
        }
        if (!ids.isEmpty()) {
            recordActivity(userId, onlySessionId == null ? "sessions_terminated_all" : "session_terminated",
                    onlySessionId == null ? String.valueOf(ids.size()) : null, now);
        }
        return ids.size();
    }

    @Override
    public boolean isSessionActive(UUID userId, UUID sessionId, Instant now) {
        return jdbc.update("""
                UPDATE account_sessions s SET last_active_at = ?
                WHERE s.id = ? AND s.user_uuid = ? AND s.revoked_at IS NULL AND s.expires_at > ?
                AND EXISTS (SELECT 1 FROM users u WHERE u.uuid = s.user_uuid
                    AND u.enabled = TRUE AND u.account_non_locked = TRUE AND u.is_deleted = FALSE)
                """, timestamp(now), sessionId, userId, timestamp(now)) == 1;
    }

    @Override
    public UUID registerSession(UUID userId, String authorizationId, ClientInfo client, Instant expiresAt, Instant now) {
        UUID proposed = UUID.randomUUID();
        // Never restore a revoked session when the same authorization is reused.
        var ids = jdbc.query("""
                        INSERT INTO account_sessions
                            (id, user_uuid, authorization_id, device_type, device, browser, os, ip_address,
                             signed_in_at, last_active_at, expires_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (authorization_id) DO UPDATE
                            SET expires_at = EXCLUDED.expires_at, last_active_at = EXCLUDED.last_active_at
                            WHERE account_sessions.revoked_at IS NULL
                              AND account_sessions.user_uuid = EXCLUDED.user_uuid
                        RETURNING id
                        """, (rs, row) -> rs.getObject("id", UUID.class), proposed, userId, authorizationId,
                client.type(), client.device(), client.browser(), client.os(), client.ip(),
                timestamp(now), timestamp(now), timestamp(expiresAt));
        if (ids.isEmpty()) {
            throw new InvalidAccountSecurityRequestException("Account session has been revoked");
        }
        return ids.getFirst();
    }

    @Override
    public PageResult<LoginHistoryResult> loginHistory(UUID userId, String result, Instant since, int page, int size) {
        String conditions = "user_uuid = ?";
        var arguments = new ArrayList<Object>();
        arguments.add(userId);
        if (!"all".equals(result)) {
            conditions += " AND success = ?";
            arguments.add("success".equals(result));
        }
        if (since != null) {
            conditions += " AND occurred_at >= ?";
            arguments.add(timestamp(since));
        }
        long count = jdbc.queryForObject("SELECT COUNT(*) FROM account_login_history WHERE " + conditions,
                Long.class, arguments.toArray());
        arguments.add(size);
        arguments.add((long) page * size);
        var items = jdbc.query("SELECT * FROM account_login_history WHERE " + conditions
                + " ORDER BY occurred_at DESC, id LIMIT ? OFFSET ?", loginMapper(), arguments.toArray());
        return new PageResult<>(items, page, size, count, (int) ((count + size - 1) / size));
    }

    @Override
    public List<SuspiciousLoginResult> suspiciousLogins(Instant since, int threshold, int limit) {
        return jdbc.query("""
                SELECT h.user_uuid, u.username,
                       COUNT(*) AS failed_count,
                       COUNT(DISTINCT h.ip_address) AS distinct_ips,
                       MAX(h.occurred_at) AS last_failed_at
                FROM account_login_history h
                JOIN users u ON u.uuid = h.user_uuid
                WHERE h.success = FALSE AND h.occurred_at >= ?
                GROUP BY h.user_uuid, u.username
                HAVING COUNT(*) >= ?
                ORDER BY failed_count DESC, last_failed_at DESC
                LIMIT ?
                """, (rs, row) -> new SuspiciousLoginResult(
                rs.getObject("user_uuid", UUID.class), rs.getString("username"),
                rs.getLong("failed_count"), rs.getLong("distinct_ips"),
                rs.getTimestamp("last_failed_at").toInstant()
        ), timestamp(since), threshold, limit);
    }

    @Override
    public void recordLogin(UUID userId, boolean success, String method, ClientInfo client, Instant now) {
        jdbc.update("""
                        INSERT INTO account_login_history
                            (id, user_uuid, success, method, device, browser, os, ip_address, occurred_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, UUID.randomUUID(), userId, success, method, client.device(), client.browser(), client.os(),
                client.ip(), timestamp(now));
    }

    @Override
    public List<AccountActivityResult> activities(UUID userId) {
        return jdbc.query("""
                SELECT * FROM account_activities WHERE user_uuid = ? ORDER BY occurred_at DESC, id
                """, (rs, row) -> new AccountActivityResult(rs.getObject("id", UUID.class), rs.getString("event_type"),
                rs.getString("detail"), rs.getTimestamp("occurred_at").toInstant()), userId);
    }

    @Override
    public void recordActivity(UUID userId, String type, String detail, Instant now) {
        jdbc.update("""
                INSERT INTO account_activities (id, user_uuid, event_type, detail, occurred_at)
                VALUES (?, ?, ?, ?, ?)
                """, UUID.randomUUID(), userId, type, detail, timestamp(now));
    }

    @Override
    public Optional<DeactivationResult> pendingDeactivation(UUID userId) {
        return jdbc.query("""
                        SELECT requested_at FROM account_deactivation_requests WHERE user_uuid = ? AND status = 'requested'
                        """, (rs, row) -> new DeactivationResult("requested", rs.getTimestamp("requested_at").toInstant()),
                userId).stream().findFirst();
    }

    @Override
    public DeactivationResult requestDeactivation(UUID userId, String reason, Instant now) {
        lockUser(userId);
        var existing = pendingDeactivation(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        jdbc.update("""
                INSERT INTO account_deactivation_requests (user_uuid, reason, status, requested_at)
                VALUES (?, ?, 'requested', ?)
                ON CONFLICT (user_uuid) DO UPDATE SET reason = EXCLUDED.reason, status = 'requested',
                    requested_at = EXCLUDED.requested_at, cancelled_at = NULL
                """, userId, reason, timestamp(now));
        recordActivity(userId, "deactivation_requested", null, now);
        return pendingDeactivation(userId).orElseThrow();
    }

    @Override
    public void cancelDeactivation(UUID userId, Instant now) {
        lockUser(userId);
        int count = jdbc.update("""
                UPDATE account_deactivation_requests SET status = 'cancelled', cancelled_at = ?
                WHERE user_uuid = ? AND status = 'requested'
                """, timestamp(now), userId);
        if (count > 0) {
            recordActivity(userId, "deactivation_cancelled", null, now);
        }
    }

    @Override
    public void savePasswordReset(UUID userId, String tokenHash, Instant expiresAt, Instant now) {
        lockUser(userId);
        var recent = jdbc.query("SELECT created_at FROM account_password_resets WHERE user_uuid = ?",
                (rs, row) -> rs.getTimestamp("created_at").toInstant(), userId);
        if (!recent.isEmpty() && recent.getFirst().isAfter(now.minusSeconds(30))) {
            throw new InvalidAccountSecurityRequestException("Wait 30 seconds before requesting another reset email");
        }
        jdbc.update("""
                INSERT INTO account_password_resets (user_uuid, token_hash, created_at, expires_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (user_uuid) DO UPDATE SET token_hash = EXCLUDED.token_hash,
                    created_at = EXCLUDED.created_at, expires_at = EXCLUDED.expires_at, used_at = NULL
                """, userId, tokenHash, timestamp(now), timestamp(expiresAt));
        recordActivity(userId, "password_reset_requested", null, now);
    }

    @Override
    public void confirmPasswordReset(String tokenHash, String passwordHash, Instant now) {
        // Lock the user before the token to keep the lock order consistent with requesting a reset.
        var users = jdbc.query("SELECT user_uuid FROM account_password_resets WHERE token_hash = ?",
                (rs, row) -> rs.getObject("user_uuid", UUID.class), tokenHash);
        if (users.isEmpty()) {
            throw new InvalidAccountSecurityRequestException("Invalid or expired reset token");
        }
        UUID userId = users.getFirst();
        lockUser(userId);
        int consumed = jdbc.update("""
                UPDATE account_password_resets SET used_at = ?
                WHERE user_uuid = ? AND token_hash = ? AND used_at IS NULL AND expires_at > ?
                """, timestamp(now), userId, tokenHash, timestamp(now));
        if (consumed != 1) {
            throw new InvalidAccountSecurityRequestException("Invalid or expired reset token");
        }
        jdbc.update("""
                UPDATE users SET password_hash = ?, password_changed_at = ?, updated_at = ?,
                    updated_by = ?, version = version + 1 WHERE uuid = ?
                """, passwordHash, timestamp(now), timestamp(now), userId.toString(), userId);
        revoke(userId, null, null, now);
        recordActivity(userId, "password_changed", null, now);
    }

    private void lockUser(UUID userId) {
        var ids = jdbc.query("SELECT uuid FROM users WHERE uuid = ? AND is_deleted = FALSE FOR UPDATE",
                (rs, row) -> rs.getObject("uuid", UUID.class), userId);
        if (ids.isEmpty()) {
            throw new ResourceNotFoundException("User", userId);
        }
    }

    private RowMapper<LoginHistoryResult> loginMapper() {
        return (rs, row) -> new LoginHistoryResult(rs.getObject("id", UUID.class),
                rs.getBoolean("success") ? "success" : "failed", rs.getString("method"),
                rs.getBoolean("success") ? null : "wrong_password", rs.getString("device"),
                rs.getString("browser"), rs.getString("os"), rs.getString("ip_address"),
                rs.getString("location"), rs.getTimestamp("occurred_at").toInstant());
    }

    private static Timestamp timestamp(Instant value) {
        return Timestamp.from(value);
    }

    private record SessionReference(UUID id, String authorizationId) {
    }
}
