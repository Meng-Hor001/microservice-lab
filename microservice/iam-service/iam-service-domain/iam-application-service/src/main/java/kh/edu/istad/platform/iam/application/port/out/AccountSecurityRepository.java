package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.AccountActivityResult;
import kh.edu.istad.platform.iam.application.dto.AccountSessionResult;
import kh.edu.istad.platform.iam.application.dto.ClientInfo;
import kh.edu.istad.platform.iam.application.dto.DeactivationResult;
import kh.edu.istad.platform.iam.application.dto.LoginHistoryResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.SuspiciousLoginResult;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountSecurityRepository {
    List<AccountSessionResult> sessions(UUID userId, UUID currentSessionId, Instant now);

    int revoke(UUID userId, UUID onlySessionId, UUID excludedSessionId, Instant now);

    boolean isSessionActive(UUID userId, UUID sessionId, Instant now);

    UUID registerSession(UUID userId, String authorizationId, ClientInfo client, Instant expiresAt, Instant now);

    PageResult<LoginHistoryResult> loginHistory(UUID userId, String result, Instant since, int page, int size);

    List<SuspiciousLoginResult> suspiciousLogins(Instant since, int threshold, int limit);

    void recordLogin(UUID userId, boolean success, String method, ClientInfo client, Instant now);

    List<AccountActivityResult> activities(UUID userId);

    void recordActivity(UUID userId, String type, String detail, Instant now);

    Optional<DeactivationResult> pendingDeactivation(UUID userId);

    DeactivationResult requestDeactivation(UUID userId, String reason, Instant now);

    void cancelDeactivation(UUID userId, Instant now);

    void savePasswordReset(UUID userId, String tokenHash, Instant expiresAt, Instant now);

    void confirmPasswordReset(String tokenHash, String passwordHash, Instant now);
}
