import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import kh.edu.istad.platform.iam.domain.enums.PermissionEffect;
import kh.edu.istad.platform.iam.domain.exception.DomainRuleException;
import kh.edu.istad.platform.iam.domain.service.AuthorityCalculator;
import kh.edu.istad.platform.iam.domain.valueobject.UserProfile;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    private static final Instant NOW = Instant.parse("2026-11-06T08:00:00Z");
    private final AuthorityCalculator calculator = new AuthorityCalculator();

    // IDs simulate objects returned by persistence; no database is needed.
    private User user(String passwordHash) {
        return User.builder()
                .id(1L).uuid(UUID.randomUUID())
                .username("dara").email("DARA@EXAMPLE.COM")
                .passwordHash(passwordHash)
                .givenName("Dara").familyName("Sok").locale("km")
                .createdAt(NOW).createdBy("system")
                .build();
    }

    private Role editor() {
        return Role.builder()
                .id(10L).uuid(UUID.randomUUID()).name("EDITOR")
                .createdAt(NOW).createdBy("system")
                .build();
    }

    private Permission permission(long id, String resource, String action) {
        return Permission.builder()
                .id(id).uuid(UUID.randomUUID())
                .resource(resource).action(action)
                .createdAt(NOW).createdBy("system")
                .build();
    }

    private Identity identity(IdentityProvider provider, long userId) {
        return Identity.builder()
                .userId(userId).provider(provider)
                .providerSubject(provider.name() + "-subject")
                .createdAt(NOW).createdBy("system")
                .build();
    }

    @Test
    void roleRenamePreservesIdentityAndUpdatesAudit() {
        Role role = editor();
        UUID originalUuid = role.getUuid();

        role.rename("CONTENT_MANAGER", "admin", NOW.plusSeconds(1));

        assertEquals("CONTENT_MANAGER", role.getName());
        assertEquals(originalUuid, role.getUuid());
        assertEquals(Long.valueOf(10), role.getId());
        assertEquals(NOW, role.getCreatedAt());
        assertEquals("system", role.getCreatedBy());
        assertEquals(NOW.plusSeconds(1), role.getUpdatedAt());
        assertEquals("admin", role.getUpdatedBy());
    }

    @Test
    void roleNameCannotBeBlank() {
        assertThrows(DomainRuleException.class, () -> Role.builder()
                .name(" ").createdAt(NOW).createdBy("system").build());
    }

    @Test
    void systemRoleCannotBeChangedOrDeleted() {
        Role role = Role.builder().name("ADMIN").system(true)
                .createdAt(NOW).createdBy("system").build();

        assertThrows(DomainRuleException.class,
                () -> role.rename("OTHER", "admin", NOW));
        assertThrows(DomainRuleException.class,
                () -> role.updateDescription("Other", "admin", NOW));
        assertThrows(DomainRuleException.class,
                () -> role.deactivate("admin", NOW));
        assertThrows(DomainRuleException.class,
                () -> role.softDelete("admin", NOW));
        assertEquals("ADMIN", role.getName());
        assertTrue(role.isActive());
        assertFalse(role.isDeleted());
    }

    @Test
    void permissionNameIsDerivedAndMustMatchResourceAndAction() {
        Permission permission = permission(20, "course", "read");
        assertEquals("course:read", permission.getName());

        assertThrows(DomainRuleException.class, () -> Permission.builder()
                .name("course:delete").resource("course").action("read")
                .createdAt(NOW).createdBy("system").build());
    }

    @Test
    void systemPermissionCannotBeChangedOrDeleted() {
        Permission permission = Permission.builder()
                .resource("user").action("read").system(true)
                .createdAt(NOW).createdBy("system").build();

        assertThrows(DomainRuleException.class,
                () -> permission.update("user", "write", null, "admin", NOW));
        assertThrows(DomainRuleException.class,
                () -> permission.deactivate("admin", NOW));
        assertThrows(DomainRuleException.class,
                () -> permission.softDelete("admin", NOW));
    }

    @Test
    void emailIsLowercaseAndNewUserHasExpectedAccountDefaults() {
        User user = user("encoded-password");
        assertEquals("dara@example.com", user.getEmail());
        assertEquals("Dara Sok", user.getName());
        assertFalse(user.isEmailVerified());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.canSignInWithPassword());
    }

    @Test
    void changingEmailAndPhoneResetsVerification() {
        User user = User.builder().username("dara")
                .email("old@example.com").emailVerified(true)
                .phoneNumber("+85512345678").phoneNumberVerified(true)
                .givenName("Dara").familyName("Sok").locale("km")
                .createdAt(NOW).createdBy("system").build();

        UserProfile replacement = new UserProfile(
                "new@example.com", "Dara", "Sok", null, null, null,
                null, null, null, null, "Asia/Phnom_Penh", "en",
                "+85598765432", null, null, null, null, "KH");

        user.updateProfile(replacement, "user", NOW.plusSeconds(1));

        assertEquals("new@example.com", user.getEmail());
        assertEquals("+85598765432", user.getPhoneNumber());
        assertFalse(user.isEmailVerified());
        assertFalse(user.isPhoneNumberVerified());
    }

    @Test
    void successfulLoginResetsFailedAttemptsAndRecordsTimestamp() {
        User user = user("encoded-password");
        user.recordLoginAttempt(false, "authentication", NOW.plusSeconds(1));
        user.recordLoginAttempt(false, "authentication", NOW.plusSeconds(2));
        assertEquals(2, user.getFailedLoginAttempts());
        assertNull(user.getLastLoginAt());

        user.recordLoginAttempt(true, "authentication", NOW.plusSeconds(3));

        assertEquals(0, user.getFailedLoginAttempts());
        assertEquals(NOW.plusSeconds(3), user.getLastLoginAt());
    }

    @Test
    void lockedAndDisabledUsersCannotSignIn() {
        User user = user("encoded-password");
        user.lock("admin", NOW);
        assertFalse(user.canSignIn());
        assertThrows(DomainRuleException.class,
                () -> user.recordLoginAttempt(true, "authentication", NOW));

        user.unlock("admin", NOW.plusSeconds(1));
        assertTrue(user.canSignIn());
        user.disable("admin", NOW.plusSeconds(2));
        assertFalse(user.canSignIn());
    }

    @Test
    void passwordChangeRenewsExpiredCredentials() {
        User user = user("old-hash");
        user.expireCredentials("admin", NOW);
        assertFalse(user.canSignInWithPassword());

        user.changePasswordHash("new-hash", "user", NOW.plusSeconds(1));

        assertEquals("new-hash", user.getPasswordHash());
        assertEquals(NOW.plusSeconds(1), user.getPasswordChangedAt());
        assertTrue(user.canSignInWithPassword());
    }

    @Test
    void softDeletionRetainsUserAndPreventsFurtherChanges() {
        User user = user("encoded-password");
        UUID uuid = user.getUuid();
        user.softDelete("admin", NOW.plusSeconds(1));

        assertTrue(user.isDeleted());
        assertFalse(user.isEnabled());
        assertEquals(uuid, user.getUuid());
        assertEquals("admin", user.getDeletedBy());
        assertEquals(NOW.plusSeconds(1), user.getDeletedAt());
        assertFalse(user.canSignIn());
        assertThrows(DomainRuleException.class,
                () -> user.enable("admin", NOW.plusSeconds(2)));
    }

    @Test
    void inactiveRoleAndPermissionCannotBeAssigned() {
        User user = user("encoded-password");
        Role role = editor();
        Permission permission = permission(20, "course", "read");
        role.deactivate("admin", NOW);

        assertThrows(DomainRuleException.class,
                () -> UserRole.assign(user, role, "admin", NOW, null, null));

        role.activate("admin", NOW);
        permission.deactivate("admin", NOW);

        assertThrows(DomainRuleException.class,
                () -> RolePermission.assign(role, permission, "admin", NOW, null, null));
        assertThrows(DomainRuleException.class,
                () -> UserPermission.assign(user, permission, PermissionEffect.GRANT,
                        "admin", NOW, null, null));
    }

    @Test
    void assignmentExpiresExactlyAtExpiry() {
        UserRole assignment = UserRole.assign(user("hash"), editor(),
                "admin", NOW, NOW.plusSeconds(60), "Temporary access");

        assertFalse(assignment.isActiveAt(NOW.minusSeconds(1)));
        assertTrue(assignment.isActiveAt(NOW));
        assertTrue(assignment.isActiveAt(NOW.plusSeconds(59)));
        assertFalse(assignment.isActiveAt(NOW.plusSeconds(60)));
    }

    @Test
    void revocationKeepsAssignmentHistory() {
        UserRole assignment = UserRole.assign(user("hash"), editor(),
                "admin", NOW, null, "Initial access");
        assignment.revoke("operator", "Access removed", NOW.plusSeconds(1));

        assertEquals(Long.valueOf(1), assignment.getUserId());
        assertEquals(Long.valueOf(10), assignment.getRoleId());
        assertEquals("admin", assignment.getAssignedBy());
        assertEquals(NOW, assignment.getAssignedAt());
        assertEquals("operator", assignment.getRevokedBy());
        assertEquals("Access removed", assignment.getReason());
        assertFalse(assignment.isActiveAt(NOW.plusSeconds(2)));
    }

    @Test
    void directGrantAddsPermissionAndDenyOverridesRoleGrant() {
        User user = user("hash");
        Role role = editor();
        Permission read = permission(20, "course", "read");
        Permission update = permission(21, "course", "update");
        Permission report = permission(22, "report", "read");

        var result = calculator.calculate(user, List.of(role),
                List.of(read, update, report),
                List.of(UserRole.assign(user, role, "admin", NOW, null, null)),
                List.of(RolePermission.assign(role, read, "admin", NOW, null, null),
                        RolePermission.assign(role, update, "admin", NOW, null, null)),
                List.of(UserPermission.assign(user, update, PermissionEffect.DENY,
                                "admin", NOW, null, null),
                        UserPermission.assign(user, report, PermissionEffect.GRANT,
                                "admin", NOW, null, null)), NOW);

        assertEquals(Set.of("ROLE_EDITOR", "course:read", "report:read"),
                result.authorities());
    }

    @Test
    void expiredDenyNoLongerRemovesRolePermission() {
        User user = user("hash");
        Role role = editor();
        Permission read = permission(20, "course", "read");

        var result = calculator.calculate(user, List.of(role), List.of(read),
                List.of(UserRole.assign(user, role, "admin", NOW, null, null)),
                List.of(RolePermission.assign(role, read, "admin", NOW, null, null)),
                List.of(UserPermission.assign(user, read, PermissionEffect.DENY,
                        "admin", NOW, NOW.plusSeconds(10), null)), NOW.plusSeconds(10));

        assertEquals(Set.of("ROLE_EDITOR", "course:read"), result.authorities());
    }

    @Test
    void expiredRolePermissionDoesNotRemoveTheRoleAuthority() {
        User user = user("hash");
        Role role = editor();
        Permission read = permission(20, "course", "read");

        var result = calculator.calculate(user, List.of(role), List.of(read),
                List.of(UserRole.assign(user, role, "admin", NOW, null, null)),
                List.of(RolePermission.assign(role, read, "admin", NOW,
                        NOW.plusSeconds(10), null)), List.of(), NOW.plusSeconds(10));

        assertEquals(Set.of("ROLE_EDITOR"), result.authorities());
    }

    @Test
    void revokedUserRoleContributesNeitherRoleNorPermissions() {
        User user = user("hash");
        Role role = editor();
        Permission read = permission(20, "course", "read");
        UserRole assignment = UserRole.assign(user, role, "admin", NOW, null, null);
        assignment.revoke("admin", "Removed", NOW.plusSeconds(1));

        var result = calculator.calculate(user, List.of(role), List.of(read),
                List.of(assignment),
                List.of(RolePermission.assign(role, read, "admin", NOW, null, null)),
                List.of(), NOW.plusSeconds(2));

        assertTrue(result.authorities().isEmpty());
    }

    @Test
    void deletedPermissionDoesNotContributeAuthorities() {
        User user = user("hash");
        Permission read = permission(20, "course", "read");
        UserPermission assignment = UserPermission.assign(user, read,
                PermissionEffect.GRANT, "admin", NOW, null, null);
        read.softDelete("admin", NOW.plusSeconds(1));

        var result = calculator.calculate(user, List.of(), List.of(read),
                List.of(), List.of(), List.of(assignment), NOW.plusSeconds(2));

        assertTrue(result.authorities().isEmpty());
    }

    @Test
    void duplicateSocialProviderCannotBeLinked() {
        User user = user("hash");
        Identity existing = identity(IdentityProvider.GOOGLE, 1L);
        Identity candidate = identity(IdentityProvider.GOOGLE, 1L);

        assertThrows(DomainRuleException.class,
                () -> candidate.ensureCanLink(user, List.of(existing)));
    }

    @Test
    void lastIdentityCannotBeUnlinkedWithoutUsablePassword() {
        User user = user(null);
        Identity identity = identity(IdentityProvider.GOOGLE, 1L);

        assertThrows(DomainRuleException.class,
                () -> identity.unlink(user, List.of(identity), "user", NOW.plusSeconds(1)));
        assertFalse(identity.isDeleted());
    }

    @Test
    void passwordAllowsUnlinkingLastSocialIdentity() {
        User user = user("encoded-password");
        Identity identity = identity(IdentityProvider.GOOGLE, 1L);

        identity.unlink(user, List.of(identity), "user", NOW.plusSeconds(1));

        assertTrue(identity.isDeleted());
        assertEquals("user", identity.getDeletedBy());
        assertEquals(NOW.plusSeconds(1), identity.getDeletedAt());
    }

    @Test
    void anotherProviderAllowsUnlinkingWithoutPassword() {
        User user = user(null);
        Identity google = identity(IdentityProvider.GOOGLE, 1L);
        Identity github = identity(IdentityProvider.GITHUB, 1L);

        google.unlink(user, List.of(google, github), "user", NOW.plusSeconds(1));

        assertTrue(google.isDeleted());
        assertFalse(github.isDeleted());
    }

    @Test
    void anotherUsersIdentityDoesNotCountAsASignInMethod() {
        User user = user(null);
        Identity google = identity(IdentityProvider.GOOGLE, 1L);
        Identity otherUsersGithub = identity(IdentityProvider.GITHUB, 2L);

        assertThrows(DomainRuleException.class, () -> google.unlink(user,
                List.of(google, otherUsersGithub), "user", NOW.plusSeconds(1)));
    }

    @Test
    void nestedProviderAttributesAreDefensivelyCopied() {
        List<String> scopes = new ArrayList<>(List.of("email"));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("scopes", scopes);
        Identity identity = Identity.builder().userId(1L)
                .provider(IdentityProvider.GOOGLE).providerSubject("subject")
                .attributes(attributes).createdAt(NOW).createdBy("system").build();

        scopes.add("profile");
        attributes.put("extra", true);

        assertEquals(List.of("email"), identity.getAttributes().get("scopes"));
        assertFalse(identity.getAttributes().containsKey("extra"));
        assertThrows(UnsupportedOperationException.class,
                () -> identity.getAttributes().put("new", true));
    }
}
