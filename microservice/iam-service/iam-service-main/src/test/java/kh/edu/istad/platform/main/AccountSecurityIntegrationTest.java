package kh.edu.istad.platform.main;

import kh.edu.istad.platform.iam.application.dto.ClientInfo;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.InvalidAccountSecurityRequestException;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.application.port.out.PasswordResetDelivery;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.restapi.security.AccountSessionTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:iam-test.properties")
class AccountSecurityIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", DATABASE::getJdbcUrl);
        properties.add("spring.datasource.username", DATABASE::getUsername);
        properties.add("spring.datasource.password", DATABASE::getPassword);
    }

    @Autowired
    AccountSecurityRepository accounts;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    Clock clock;
    @Autowired
    RevokeAccountSessionsUseCase revoke;
    @Autowired
    ListLoginHistoryUseCase history;
    @Autowired
    ManageDeactivationUseCase deactivation;
    @Autowired
    RequestPasswordResetUseCase requestReset;
    @Autowired
    ConfirmPasswordResetUseCase confirmReset;
    @Autowired
    PasswordHasher hasher;
    @MockitoBean
    PasswordResetDelivery delivery;

    UUID userId;
    UUID otherUserId;

    @BeforeEach
    void createUsers() {
        userId = createUser();
        otherUserId = createUser();
    }

    @Test
    void sessionOwnershipRevocationAndCurrentSessionAreEnforced() {
        Instant now = clock.instant();
        UUID current = accounts.registerSession(userId, UUID.randomUUID().toString(),
                ClientInfo.unknown(), now.plusSeconds(600), now);
        UUID other = accounts.registerSession(userId, UUID.randomUUID().toString(),
                ClientInfo.unknown(), now.plusSeconds(600), now);
        UUID foreign = accounts.registerSession(otherUserId, UUID.randomUUID().toString(),
                ClientInfo.unknown(), now.plusSeconds(600), now);
        assertTrue(accounts.sessions(userId, current, now).stream()
                .anyMatch(session -> session.id().equals(current) && session.current()));
        assertThrows(ResourceNotFoundException.class, () -> revoke.revokeOne(userId, foreign));
        assertThrows(IllegalArgumentException.class, () -> revoke.revokeOthers(userId, null));
        assertEquals(1, revoke.revokeOthers(userId, current.toString()));
        assertFalse(accounts.isSessionActive(userId, other, now));
        assertTrue(accounts.isSessionActive(otherUserId, foreign, now));
        var validator = new AccountSessionTokenValidator(accounts, clock);
        var token = Jwt.withTokenValue("test").header("alg", "RS256")
                .subject(userId.toString()).claim("sid", current.toString()).build();
        assertFalse(validator.validate(token).hasErrors());
        assertEquals(1, revoke.revokeAll(userId));
        assertTrue(validator.validate(token).hasErrors());
    }

    @Test
    void historyFiltersPaginationAndActivityAreUserScoped() {
        Instant now = clock.instant();
        accounts.recordLogin(userId, true, "password", ClientInfo.unknown(), now.minusSeconds(120));
        accounts.recordLogin(userId, false, "password", ClientInfo.unknown(), now.minusSeconds(60));
        accounts.recordLogin(userId, true, "password", ClientInfo.unknown(), now.minusSeconds(86400 * 10));
        accounts.recordLogin(otherUserId, false, "password", ClientInfo.unknown(), now);
        var page = history.execute(userId, "all", 7, 0, 1);
        assertEquals(2, page.totalElements());
        assertEquals(2, page.totalPages());
        assertEquals("failed", page.content().getFirst().result());
        assertEquals(1, history.execute(userId, "success", 7, 0, 10).totalElements());
        assertEquals(3, history.execute(userId, "all", null, 0, 10).totalElements());
        assertThrows(IllegalArgumentException.class, () -> history.execute(userId, "anything", 7, 0, 10));
        accounts.recordActivity(userId, "profile_updated", null, now);
        accounts.recordActivity(otherUserId, "password_changed", null, now);
        assertEquals(1, accounts.activities(userId).size());
        assertEquals("profile_updated", accounts.activities(userId).getFirst().type());
    }

    @Test
    void deactivationRequestsArePersistentIdempotentAndCancellable() {
        assertThrows(ResourceNotFoundException.class, () -> deactivation.getPending(userId));
        var requested = deactivation.request(userId, "privacy");
        assertEquals(requested, deactivation.request(userId, "other"));
        assertEquals(requested, deactivation.getPending(userId));
        assertThrows(ResourceNotFoundException.class, () -> deactivation.getPending(otherUserId));
        assertEquals(1, accounts.activities(userId).size());
        deactivation.cancel(userId);
        deactivation.cancel(userId);
        assertThrows(ResourceNotFoundException.class, () -> deactivation.getPending(userId));
        assertEquals(2, accounts.activities(userId).size());
        assertTrue(jdbc.queryForObject("SELECT enabled FROM users WHERE uuid = ?", Boolean.class, userId));
    }

    @Test
    void resetTokensAreHashedSingleUseAndRevokeSessions() {
        Instant now = clock.instant();
        UUID session = accounts.registerSession(userId, UUID.randomUUID().toString(),
                ClientInfo.unknown(), now.plusSeconds(600), now);
        requestReset.execute(userId);
        var token = ArgumentCaptor.forClass(String.class);
        verify(delivery).send(anyString(), token.capture());
        String raw = token.getValue();
        assertNotEquals(raw, jdbc.queryForObject("SELECT token_hash FROM account_password_resets WHERE user_uuid = ?",
                String.class, userId));
        assertThrows(InvalidAccountSecurityRequestException.class, () -> requestReset.execute(userId));
        assertThrows(IllegalArgumentException.class, () -> confirmReset.execute(raw, "weak", "weak"));
        confirmReset.execute(raw, "UpdatedPassword123!", "UpdatedPassword123!");
        String passwordHash = jdbc.queryForObject("SELECT password_hash FROM users WHERE uuid = ?", String.class, userId);
        assertTrue(hasher.matches("UpdatedPassword123!", passwordHash));
        assertFalse(accounts.isSessionActive(userId, session, clock.instant()));
        assertThrows(InvalidAccountSecurityRequestException.class,
                () -> confirmReset.execute(raw, "UpdatedPassword123!", "UpdatedPassword123!"));
    }

    @Test
    void expiredTokensAndDeliveryFailuresDoNotChangePasswords() {
        Instant now = clock.instant();
        String token = "a".repeat(43);
        accounts.savePasswordReset(userId, RequestPasswordResetUseCase.hashToken(token), now.minusSeconds(1), now.minusSeconds(120));
        assertThrows(InvalidAccountSecurityRequestException.class,
                () -> confirmReset.execute(token, "UpdatedPassword123!", "UpdatedPassword123!"));
        doThrow(new IllegalStateException("Mail unavailable")).when(delivery).send(anyString(), anyString());
        assertThrows(IllegalStateException.class, () -> requestReset.execute(otherUserId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM account_password_resets WHERE user_uuid = ?",
                Integer.class, otherUserId));
        assertTrue(accounts.activities(otherUserId).isEmpty());
    }

    private UUID createUser() {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO users (uuid, username, email, given_name, family_name, created_by, updated_by)
                VALUES (?, ?, ?, 'Test', 'User', 'test', 'test')
                """, id, "u" + id.toString().replace("-", ""), id + "@example.com");
        return id;
    }
}
