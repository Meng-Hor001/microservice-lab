package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.*;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResponseJsonTest {
    private final JsonMapper json = JsonMapper.builder().build();
    private final Instant now = Instant.parse("2026-11-09T08:00:00Z");

    @Test
    void pageUsesNestedMetadataAndPublicSummary() {
        var result = new PageResult<>(List.of(
                new RoleSummary(UUID.randomUUID(), "ADMIN", "Internal description", true)
        ), 0, 20, 1, 1);
        var tree = json.valueToTree(PageResponse.from(result, RoleSummaryResponse::from));
        assertEquals(2, tree.size());
        assertEquals(20, tree.path("page").path("size").asInt());
        assertEquals(1, tree.path("page").path("totalElements").asLong());
        assertEquals("ADMIN", tree.path("content").get(0).path("name").asText());
        assertFalse(tree.path("content").get(0).has("description"));
    }

    @Test
    void userContainsComposedNameAndPublicRoles() {
        UUID roleUuid = UUID.randomUUID();
        var user = new UserResult(UUID.randomUUID(), "dara", "dara@example.com", true,
                "Dara", "Sok", "km", true, now, now, 2);
        var authorities = new UserAuthoritiesResult(
                List.of(new UserAuthoritiesResult.RoleAuthority(roleUuid, "USER", null)),
                List.of(), List.of("user:read"));
        var tree = json.valueToTree(UserResponse.from(user, authorities));
        assertEquals("Dara Sok", tree.path("name").asText());
        assertEquals(roleUuid.toString(), tree.path("roles").get(0).path("uuid").asText());
        assertFalse(tree.has("passwordHash"));
        assertFalse(tree.has("id"));
        assertEquals(now.toString(), tree.path("createdAt").asText());
    }

    @Test
    void authoritiesIncludeExpiryAndDirectDeny() {
        var result = new UserAuthoritiesResult(
                List.of(new UserAuthoritiesResult.RoleAuthority(UUID.randomUUID(), "ADMIN", now)),
                List.of(new UserAuthoritiesResult.DirectPermissionAuthority("user:delete", "DENY")),
                List.of("user:read"));
        var tree = json.valueToTree(UserAuthoritiesResponse.from(result));
        assertEquals(now.toString(), tree.path("roles").get(0).path("expiresAt").asText());
        assertEquals("DENY", tree.path("directPermissions").get(0).path("effect").asText());
        assertEquals("user:read", tree.path("effectivePermissions").get(0).asText());
    }

    @Test
    void identityExposesOnlyAssignmentResponseFields() {
        var result = new IdentityResult(UUID.randomUUID(), IdentityProvider.GOOGLE,
                "private-provider-subject", "dara@example.com", true, "Dara Sok",
                "provider-handle", "https://example.com/avatar", now, now);
        var tree = json.valueToTree(IdentityResponse.from(result));
        assertEquals(5, tree.size());
        assertEquals("GOOGLE", tree.path("provider").asText());
        assertEquals(now.toString(), tree.path("linkedAt").asText());
        assertFalse(tree.has("providerSubject"));
        assertFalse(tree.has("attributes"));
    }
}
