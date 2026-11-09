package kh.edu.istad.platform.main;

import kh.edu.istad.platform.iam.application.dto.*;
import kh.edu.istad.platform.iam.application.exception.*;
import kh.edu.istad.platform.iam.application.port.out.*;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.iam.domain.entity.*;
import kh.edu.istad.platform.iam.domain.enums.*;
import kh.edu.istad.platform.iam.domain.exception.DomainRuleException;
import kh.edu.istad.platform.iam.domain.service.AuthorityCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PartTwoIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        properties.add("spring.liquibase.enabled", () -> "true");
    }

    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PermissionRepository permissions;
    @Autowired UserRoleAssignmentRepository userRoles;
    @Autowired RolePermissionAssignmentRepository rolePermissions;
    @Autowired UserPermissionAssignmentRepository userPermissions;
    @Autowired IdentityRepository identities;
    @Autowired CreateRoleUseCase createRole;
    @Autowired FindOrProvisionFederatedUserUseCase federation;
    @Autowired LinkIdentityUseCase linkIdentity;
    @Autowired UnlinkIdentityUseCase unlinkIdentity;
    @Autowired JdbcTemplate jdbc;
    @Autowired Clock clock;

    private final CurrentActorProvider actor = () -> "integration-test";

    private String unique() { return UUID.randomUUID().toString().replace("-", "").substring(0, 16); }

    private User user() {
        String name = "user_" + unique();
        return users.save(User.builder().username(name).email(name + "@example.com")
                .givenName("Dara").familyName("Sok").locale("en")
                .passwordHash("test-password-hash").createdAt(clock.instant())
                .createdBy(actor.currentActor()).build());
    }

    private Role role() {
        return roles.save(Role.builder().name("ROLE_" + unique()).createdAt(clock.instant())
                .createdBy(actor.currentActor()).build());
    }

    private Permission permission() {
        return permissions.save(Permission.builder().resource("resource" + unique()).action("read")
                .createdAt(clock.instant()).createdBy(actor.currentActor()).build());
    }

    private AssignRolesToUserUseCase assignRoles() {
        return new AssignRolesToUserUseCase(users, roles, userRoles, actor, clock);
    }

    private GrantPermissionToUserUseCase grantPermissions() {
        return new GrantPermissionToUserUseCase(users, permissions, userPermissions, actor, clock);
    }

    private AssignmentPermissionsToRoleUseCase assignRolePermissions() {
        return new AssignmentPermissionsToRoleUseCase(roles, permissions, rolePermissions, actor, clock);
    }

    private GetUserAuthoritiesUseCase authorities(Clock at) {
        return new GetUserAuthoritiesUseCase(users, roles, permissions, userRoles,
                rolePermissions, userPermissions, new AuthorityCalculator(), at);
    }

    @Test
    void freshDatabaseRunsEveryMigrationAndSeedsAccess() {
        assertEquals(7, jdbc.queryForObject("select count(*) from databasechangelog", Integer.class));
        assertEquals(2, jdbc.queryForObject("select count(*) from roles where name in ('ADMIN','USER') and is_system", Integer.class));
        assertEquals(14, jdbc.queryForObject("select count(*) from permissions where is_system", Integer.class));
        assertEquals(17, jdbc.queryForObject("select count(*) from role_permissions rp join roles r on r.id=rp.role_id where r.name in ('ADMIN','USER')", Integer.class));
        assertEquals(7, jdbc.queryForObject("select count(*) from information_schema.tables where table_schema='public' and table_name in ('users','roles','permissions','identities','user_roles','role_permissions','user_permissions')", Integer.class));
    }

    @Test
    void revokeRolePersistsAuditAndAllowsReassignment() {
        var user = user(); var role = role();
        var command = new AssignRolesToUserCommand(user.getUuid(), Set.of(role.getUuid()), null, "Initial access");
        assignRoles().execute(command);
        assertThrows(DuplicateAssignmentException.class, () -> assignRoles().execute(command));
        new RevokedRoleFromUserUseCase(userRoles, actor, clock)
                .execute(user.getUuid(), role.getUuid(), "Access removed");
        assertTrue(userRoles.findActiveByUserUuid(user.getUuid()).isEmpty());
        assertTrue(authorities(clock).execute(user.getUuid()).roles().isEmpty());
        var row = jdbc.queryForMap("select revoked_at, revoked_by, reason from user_roles where user_id=?", user.getId());
        assertNotNull(row.get("revoked_at"));
        assertEquals("integration-test", row.get("revoked_by"));
        assertEquals("Access removed", row.get("reason"));
        assignRoles().execute(command);
        assertEquals(2, jdbc.queryForObject("select count(*) from user_roles where user_id=?", Integer.class, user.getId()));
    }

    @Test
    void duplicateRolePermissionIsRejected() {
        var role = role(); var permission = permission();
        var command = new AssignPermissionsToRoleCommand(role.getUuid(), Set.of(permission.getUuid()), null, "Access");
        assignRolePermissions().execute(command);
        assertThrows(DuplicateAssignmentException.class, () -> assignRolePermissions().execute(command));
    }

    @Test
    void directPermissionMustBeRevokedBeforeChangingEffect() {
        var user = user(); var permission = permission();
        var grants = grantPermissions().execute(new GrantPermissionToUserCommand(user.getUuid(), List.of(permission.getUuid()), PermissionEffect.GRANT, null, "Grant"));
        assertEquals(user.getUuid(), grants.getFirst().userUuid());
        assertEquals(permission.getUuid(), grants.getFirst().permissionUuid());
        assertThrows(DuplicateAssignmentException.class, () -> grantPermissions().execute(
                new GrantPermissionToUserCommand(user.getUuid(), List.of(permission.getUuid()), PermissionEffect.DENY, null, "Deny")));
        new RevokePermissionFromUserUseCase(userPermissions, actor, clock)
                .execute(user.getUuid(), permission.getUuid(), "Replace grant");
        grantPermissions().execute(new GrantPermissionToUserCommand(user.getUuid(), List.of(permission.getUuid()), PermissionEffect.DENY, null, "Deny"));
        assertEquals(2, jdbc.queryForObject("select count(*) from user_permissions where user_id=?", Integer.class, user.getId()));
    }

    @Test
    void duplicatePermissionInsideRequestWritesNothing() {
        var user = user(); var permission = permission();
        assertThrows(DuplicateAssignmentException.class, () -> grantPermissions().execute(
                new GrantPermissionToUserCommand(user.getUuid(), List.of(permission.getUuid(), permission.getUuid()), PermissionEffect.GRANT, null, "Duplicate")));
        assertTrue(userPermissions.findActiveByUserUuid(user.getUuid()).isEmpty());
    }

    @Test
    void databaseRejectsDuplicatesEvenWhenUseCaseIsBypassed() {
        var user = user(); var role = role();
        userRoles.saveAll(List.of(UserRole.assign(user, role, actor.currentActor(), clock.instant(), null, "First")));
        assertThrows(DataIntegrityViolationException.class, () -> userRoles.saveAll(
                List.of(UserRole.assign(user, role, actor.currentActor(), clock.instant(), null, "Duplicate"))));
        assertEquals(1, userRoles.findActiveByUserUuid(user.getUuid()).size());
    }

    @Test
    void denyOverridesRoleGrantAndExpiryRestoresPermission() {
        var user = user(); var role = role(); var permission = permission();
        assignRoles().execute(new AssignRolesToUserCommand(user.getUuid(), Set.of(role.getUuid()), null, "Role"));
        assignRolePermissions().execute(new AssignPermissionsToRoleCommand(role.getUuid(), Set.of(permission.getUuid()), null, "Permission"));
        assertTrue(authorities(clock).execute(user.getUuid()).effectivePermissions().contains(permission.getName()));
        Instant expiry = clock.instant().plusSeconds(60).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        grantPermissions().execute(new GrantPermissionToUserCommand(user.getUuid(), List.of(permission.getUuid()), PermissionEffect.DENY, expiry, "Temporary deny"));
        assertFalse(authorities(clock).execute(user.getUuid()).effectivePermissions().contains(permission.getName()));
        assertTrue(authorities(Clock.fixed(expiry, ZoneOffset.UTC)).execute(user.getUuid()).effectivePermissions().contains(permission.getName()));
    }

    @Test
    void expiredRoleStopsContributingAndMustBeRevokedBeforeReplacement() {
        var user = user(); var role = role();
        Instant expiry = clock.instant().plusSeconds(60).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        var command = new AssignRolesToUserCommand(user.getUuid(), Set.of(role.getUuid()), expiry, "Temporary role");
        assignRoles().execute(command);
        assertEquals(expiry, authorities(clock).execute(user.getUuid()).roles().getFirst().expiresAt());
        assertTrue(authorities(Clock.fixed(expiry, ZoneOffset.UTC)).execute(user.getUuid()).roles().isEmpty());
        var later = new AssignRolesToUserUseCase(users, roles, userRoles, actor, Clock.fixed(expiry, ZoneOffset.UTC));
        assertThrows(DuplicateAssignmentException.class, () -> later.execute(
                new AssignRolesToUserCommand(user.getUuid(), Set.of(role.getUuid()), null, "Replacement")));
    }

    @Test
    void invalidPermissionRollsBackRoleCreation() {
        String name = "ROLLBACK_" + unique();
        assertThrows(ResourceNotFoundException.class, () -> createRole.execute(
                new CreateRoleCommand(name, "Must roll back", Set.of(UUID.randomUUID()))));
        assertFalse(roles.existsByName(name));
    }

    @Test
    void identityValidationFailureRollsBackProvisionedUser() {
        String username = "social_" + unique();
        String email = username + "@example.com";
        assertThrows(DomainRuleException.class, () -> federation.execute(new FederatedIdentityCommand(
                IdentityProvider.GOOGLE, "x".repeat(256), email, true, "Dara Sok", username, null, Map.of())));
        assertFalse(users.existsByEmail(email));
        assertFalse(users.existsByUsername(username));
    }

    @Test
    void stalePermissionAssignmentCannotOverwriteNewerRevocation() {
        var user = user(); var permission = permission();
        grantPermissions().execute(new GrantPermissionToUserCommand(user.getUuid(), List.of(permission.getUuid()), PermissionEffect.GRANT, null, "Grant"));
        var stale = userPermissions.findActiveByUserUuid(user.getUuid()).getFirst();
        var current = userPermissions.findActiveByUserUuid(user.getUuid()).getFirst();
        current.revoke("first-actor", "First update", clock.instant());
        userPermissions.save(current);
        stale.revoke("stale-actor", "Stale update", clock.instant());
        assertThrows(VersionConflictException.class, () -> userPermissions.save(stale));
        assertEquals("first-actor", jdbc.queryForObject("select revoked_by from user_permissions where id=?", String.class, stale.getId()));
    }

    @Test
    void federationPersistsIdentityAndProtectsLastSignInMethod() {
        String username = "social_" + unique();
        var command = new FederatedIdentityCommand(IdentityProvider.GOOGLE, unique(), username + "@example.com", true, "Dara Sok", username, null, Map.of("source", "test"));
        var result = federation.execute(command);
        assertEquals(result.uuid(), federation.execute(command).uuid());
        var identity = identities.findByUserUuid(result.uuid()).getFirst();
        assertThrows(DomainRuleException.class, () -> unlinkIdentity.execute(result.uuid(), identity.getUuid()));
        assertThrows(DomainRuleException.class, () -> linkIdentity.execute(result.uuid(), new FederatedIdentityCommand(
                IdentityProvider.GOOGLE, unique(), command.email(), true, "Dara Sok", username, null, Map.of())));
        linkIdentity.execute(result.uuid(), new FederatedIdentityCommand(
                IdentityProvider.GITHUB, unique(), command.email(), true, "Dara Sok", username, null, Map.of()));
        unlinkIdentity.execute(result.uuid(), identity.getUuid());
        assertEquals(1, identities.findByUserUuid(result.uuid()).size());
        assertEquals(2, jdbc.queryForObject("select count(*) from identities where user_id=?", Integer.class, users.findByUuid(result.uuid()).orElseThrow().getId()));
    }
}
