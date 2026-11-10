package kh.edu.istad.platform.main;

import com.sun.net.httpserver.HttpServer;
import kh.edu.istad.platform.iam.application.dto.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.usecase.LinkIdentityUseCase;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigInteger;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@org.springframework.test.context.TestPropertySource(locations = "classpath:iam-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartThreeApiTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final KeyPair KEYS = keyPair();
    static final HttpServer JWKS = jwksServer();
    static final String ISSUER = "http://localhost:" + JWKS.getAddress().getPort();
    static final String SUBJECT = UUID.randomUUID().toString();
    static final List<String> ALL = List.of("user:create", "user:read", "user:update", "user:delete", "user:assign",
            "role:create", "role:read", "role:update", "role:delete", "role:assign",
            "permission:create", "permission:read", "permission:update", "permission:delete");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> ISSUER);
        properties.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> ISSUER + "/jwks");
        properties.add("spring.security.oauth2.resourceserver.jwt.audiences[0]", () -> "iam-api");
        properties.add("spring.jpa.open-in-view", () -> "false");
    }

    @LocalServerPort
    int port;
    @Autowired
    LinkIdentityUseCase linkIdentity;
    @Autowired
    UserRepository users;
    final HttpClient client = HttpClient.newHttpClient();

    @AfterAll
    static void closeJwks() {
        JWKS.stop(0);
    }

    static KeyPair keyPair() {
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    static String base64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String unsigned(BigInteger number) {
        byte[] bytes = number.toByteArray();
        return base64(bytes[0] == 0 ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes);
    }

    static HttpServer jwksServer() {
        try {
            var key = (RSAPublicKey) KEYS.getPublic();
            byte[] body = JSON.writeValueAsBytes(Map.of("keys", List.of(Map.of(
                    "kty", "RSA", "kid", "test-key", "alg", "RS256", "use", "sig",
                    "n", unsigned(key.getModulus()), "e", unsigned(key.getPublicExponent())))));
            var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/jwks", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(body);
                }
            });
            server.start();
            return server;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    String token(String subject, List<String> permissions) {
        return token(subject, permissions, Map.of(), KEYS);
    }

    String token(String subject, List<String> permissions, Map<String, Object> overrides, KeyPair keys) {
        try {
            var claims = new HashMap<String, Object>();
            claims.put("iss", ISSUER);
            claims.put("sub", subject);
            claims.put("aud", List.of("iam-api"));
            claims.put("iat", Instant.now().minusSeconds(5).getEpochSecond());
            claims.put("exp", Instant.now().plusSeconds(300).getEpochSecond());
            claims.put("permissions", permissions);
            claims.put("roles", List.of("ADMIN"));
            claims.putAll(overrides);
            String unsigned = base64(JSON.writeValueAsBytes(Map.of("alg", "RS256", "kid", "test-key")))
                    + "." + base64(JSON.writeValueAsBytes(claims));
            var signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(keys.getPrivate());
            signer.update(unsigned.getBytes(StandardCharsets.US_ASCII));
            return unsigned + "." + base64(signer.sign());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    HttpResponse<String> request(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.header("Content-Type", "application/json");
        String content = body == null ? "" : body instanceof String value ? value : JSON.writeValueAsString(body);
        return client.send(builder.method(method, HttpRequest.BodyPublishers.ofString(content)).build(), HttpResponse.BodyHandlers.ofString());
    }

    JsonNode ok(String method, String path, Object body, int status, String token) throws Exception {
        var response = request(method, path, body, token);
        assertEquals(status, response.statusCode(), method + " " + path + ": " + response.body());
        if (status == 204) {
            assertEquals("", response.body());
            return null;
        }
        return JSON.readTree(response.body());
    }

    void problem(HttpResponse<String> response, int status) {
        assertEquals(status, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/problem+json"));
        var tree = JSON.readTree(response.body());
        assertEquals(status, tree.path("status").asInt());
        assertFalse(tree.path("title").asString().isBlank());
        assertFalse(tree.has("trace"));
    }

    String admin() {
        return token(SUBJECT, ALL);
    }

    String unique() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    Map<String, Object> userRequest() {
        String username = "api_" + unique();
        return Map.of("username", username, "email", username + "@example.com", "password", "Str0ng!Pass2026",
                "givenName", "Dara", "familyName", "Sok", "locale", "km");
    }

    JsonNode createUser() throws Exception {
        return ok("POST", "/api/users", userRequest(), 201, admin());
    }

    JsonNode createRole() throws Exception {
        return ok("POST", "/api/roles", Map.of("name", "EDITOR_" + unique(), "description", "Editor"), 201, admin());
    }

    JsonNode createPermission() throws Exception {
        return ok("POST", "/api/permissions", Map.of("resource", "course" + unique(), "action", "read", "description", "Read"), 201, admin());
    }

    String uuid(JsonNode result) {
        return result.path("uuid").asString();
    }

    String identity(String userUuid, IdentityProvider provider) {
        return linkIdentity.execute(UUID.fromString(userUuid), new FederatedIdentityCommand(
                provider, unique(), "linked@example.com", true, "Dara Sok", "dara", null, Map.of())).uuid().toString();
    }

    @Test
    void formLoginPersistsFailuresAndSuccessWithoutExposingUnknownUsers() throws Exception {
        var body = userRequest();
        var created = ok("POST", "/api/users", body, 201, admin());
        var userUuid = UUID.fromString(uuid(created));
        var browser = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        String base = "http://localhost:" + port;
        for (String username : List.of(body.get("username").toString(), "missing_" + unique(),
                body.get("email").toString())) {
            var login = browser.send(HttpRequest.newBuilder(URI.create(base + "/login")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, login.statusCode());
            var csrf = java.util.regex.Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"")
                    .matcher(login.body());
            assertTrue(csrf.find(), "Login form must contain a CSRF token");
            boolean success = username.equals(body.get("email"));
            String form = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(success ? body.get("password").toString() : "wrong-password", StandardCharsets.UTF_8)
                    + "&_csrf=" + URLEncoder.encode(csrf.group(1), StandardCharsets.UTF_8);
            var result = browser.send(HttpRequest.newBuilder(URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form)).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(302, result.statusCode());
            assertTrue(result.headers().firstValue("Location").orElseThrow()
                    .endsWith(success ? "/" : "/login?error"));
            var stored = users.findByUuid(userUuid).orElseThrow();
            assertEquals(success ? 0 : 1, stored.getFailedLoginAttempts());
            if (success) assertNotNull(stored.getLastLoginAt());
            else assertNull(stored.getLastLoginAt());
        }
    }

    @Test
    void registrationAndAllMeRoutesUseAuthenticatedSubject() throws Exception {
        var body = new HashMap<>(userRequest());
        body.put("confirmPassword", body.get("password"));
        var response = request("POST", "/api/auth/register", body, null);
        assertEquals(201, response.statusCode(), response.body());
        var user = JSON.readTree(response.body());
        String userUuid = uuid(user);
        assertEquals("/api/users/" + userUuid, response.headers().firstValue("Location").orElseThrow());
        assertFalse(user.has("passwordHash"));
        String mine = token(userUuid, List.of());
        var other = createUser();
        assertEquals(userUuid, uuid(ok("GET", "/api/me", null, 200, mine)));
        var updated = ok("PATCH", "/api/me", Map.of("givenName", "Sophea", "address", Map.of("country", "KH")), 200, mine);
        assertEquals("Sophea Sok", updated.path("name").asString());
        assertEquals(body.get("email"), updated.path("email").asString());
        assertEquals("Dara Sok", ok("GET", "/api/users/" + uuid(other), null, 200, admin()).path("name").asString());
        ok("PUT", "/api/me/password", Map.of("currentPassword", "Str0ng!Pass2026", "newPassword", "N3w!Pass2026", "confirmPassword", "N3w!Pass2026"), 204, mine);
        String identityUuid = identity(userUuid, IdentityProvider.GOOGLE);
        var identities = ok("GET", "/api/me/identities", null, 200, mine);
        assertEquals(identityUuid, uuid(identities.get(0)));
        String otherIdentity = identity(uuid(other), IdentityProvider.GITHUB);
        problem(request("DELETE", "/api/me/identities/" + otherIdentity, null, mine), 404);
        ok("DELETE", "/api/me/identities/" + identityUuid, null, 204, mine);
        assertTrue(ok("GET", "/api/me/identities", null, 200, mine).isEmpty());
        problem(request("POST", "/api/auth/register", body, null), 409);
    }

    @Test
    void allUserRoutesPreserveAssignmentsAndAudit() throws Exception {
        var role = createRole();
        var permission = createPermission();
        var body = new HashMap<>(userRequest());
        body.put("roleUuids", List.of(uuid(role)));
        body.put("address", Map.of("country", "KH", "locality", "Phnom Penh"));
        var response = request("POST", "/api/users", body, admin());
        assertEquals(201, response.statusCode(), response.body());
        var user = JSON.readTree(response.body());
        String userUuid = uuid(user);
        String path = "/api/users/" + userUuid;
        assertEquals("/api/users/" + userUuid, response.headers().firstValue("Location").orElseThrow());
        assertEquals(uuid(role), uuid(user.path("roles").get(0)));
        assertNotEquals(body.get("password"), users.findByUuid(UUID.fromString(userUuid)).orElseThrow().getPasswordHash());
        assertEquals(SUBJECT, users.findByUuid(UUID.fromString(userUuid)).orElseThrow().getCreatedBy());
        ok("GET", path, null, 200, admin());
        var page = ok("GET", "/api/users?page=0&size=1&sort=username,desc&search=" + body.get("username") + "&enabled=true", null, 200, admin());
        assertEquals(1, page.path("content").size());
        assertEquals(1, page.path("page").path("size").asInt());
        assertEquals("Updated Sok", ok("PATCH", path, Map.of("givenName", "Updated"), 200, admin()).path("name").asString());
        for (String action : List.of("disable", "enable", "lock", "unlock"))
            ok("POST", path + "/" + action, null, 204, admin());
        ok("PUT", path + "/password", Map.of("newPassword", "Reset!Pass2026"), 204, admin());
        var authorities = ok("GET", path + "/authorities", null, 200, admin());
        assertEquals(uuid(role), uuid(authorities.path("roles").get(0)));
        ok("DELETE", path + "/roles/" + uuid(role) + "?reason=Removed", null, 204, admin());
        ok("POST", path + "/roles", Map.of("roleUuids", List.of(uuid(role)), "reason", "Restored"), 200, admin());
        var grantBody = Map.of("permissionUuids", List.of(uuid(permission)), "effect", "GRANT", "reason", "Direct access");
        var grants = ok("POST", path + "/permissions", grantBody, 200, admin());
        assertEquals(uuid(permission), grants.get(0).path("permissionUuid").asString());
        assertFalse(grants.get(0).has("permissionId"));
        problem(request("POST", path + "/permissions", grantBody, admin()), 409);
        ok("DELETE", path + "/permissions/" + uuid(permission) + "?reason=Removed", null, 204, admin());
        String identityUuid = identity(userUuid, IdentityProvider.GOOGLE);
        assertEquals(1, ok("GET", path + "/identities", null, 200, admin()).size());
        ok("DELETE", path + "/identities/" + identityUuid, null, 204, admin());
        ok("DELETE", path, null, 204, admin());
        problem(request("GET", path, null, admin()), 404);
        problem(request("GET", path + "/identities", null, admin()), 404);
    }

    @Test
    void allRoleRoutesAndVersionConflicts() throws Exception {
        var role = createRole();
        String path = "/api/roles/" + uuid(role);
        var permission = createPermission();
        ok("GET", path, null, 200, admin());
        var page = ok("GET", "/api/roles?size=1&sort=name,asc&search=" + role.path("name").asString(), null, 200, admin());
        assertEquals(1, page.path("content").size());
        var update = Map.of("name", "UPDATED_" + unique(), "description", "Updated", "version", role.path("version").asLong());
        ok("PATCH", path, update, 200, admin());
        problem(request("PATCH", path, update, admin()), 409);
        ok("POST", path + "/deactivate", null, 204, admin());
        ok("POST", path + "/activate", null, 204, admin());
        var body = Map.of("permissionUuids", List.of(uuid(permission)), "reason", "Role access");
        ok("POST", path + "/permissions", body, 200, admin());
        problem(request("POST", path + "/permissions", body, admin()), 409);
        ok("DELETE", path + "/permissions/" + uuid(permission) + "?reason=Removed", null, 204, admin());
        ok("DELETE", path, null, 204, admin());
        problem(request("GET", path, null, admin()), 404);
    }

    @Test
    void allPermissionRoutesAndVersionConflicts() throws Exception {
        var permission = createPermission();
        String path = "/api/permissions/" + uuid(permission);
        ok("GET", path, null, 200, admin());
        var page = ok("GET", "/api/permissions?size=1&sort=name,desc&resource=" + permission.path("resource").asString() + "&active=true", null, 200, admin());
        assertEquals(1, page.path("content").size());
        var update = Map.of("resource", permission.path("resource").asString(), "action", "publish", "description", "Publish", "version", permission.path("version").asLong());
        ok("PATCH", path, update, 200, admin());
        problem(request("PATCH", path, update, admin()), 409);
        ok("POST", path + "/deactivate", null, 204, admin());
        ok("POST", path + "/activate", null, 204, admin());
        ok("DELETE", path, null, 204, admin());
        problem(request("GET", path, null, admin()), 404);
    }

    @Test
    void missingInvalidAndUntrustedTokensReturnProblemDetails() throws Exception {
        var missing = request("GET", "/api/users", null, null);
        problem(missing, 401);
        assertTrue(missing.headers().firstValue("WWW-Authenticate").orElse("").startsWith("Bearer"));
        for (String bad : List.of("invalid-token",
                token(SUBJECT, ALL, Map.of("exp", Instant.now().minusSeconds(120).getEpochSecond()), KEYS),
                token(SUBJECT, ALL, Map.of("iss", "https://wrong.example"), KEYS),
                token(SUBJECT, ALL, Map.of("aud", List.of("wrong-api")), KEYS),
                token("not-a-uuid", ALL),
                token(SUBJECT, ALL, Map.of(), keyPair()))) {
            problem(request("GET", "/api/users", null, bad), 401);
        }
    }

    @Test
    void rolesAloneDoNotBypassPermissionChecks() throws Exception {
        String noPermissions = token(SUBJECT, List.of());
        problem(request("GET", "/api/users", null, noPermissions), 403);
        problem(request("POST", "/api/users", userRequest(), noPermissions), 403);
        problem(request("GET", "/api/roles", null, token(SUBJECT, List.of("user:read"))), 403);
        problem(request("GET", "/api/permissions", null, noPermissions), 403);
    }

    @Test
    void validationAndMalformedInputsReturnProblemDetails() throws Exception {
        var invalid = new HashMap<>(userRequest());
        invalid.put("email", "not-an-email");
        invalid.put("confirmPassword", invalid.get("password"));
        var validation = request("POST", "/api/auth/register", invalid, null);
        problem(validation, 400);
        assertFalse(JSON.readTree(validation.body()).path("errors").isEmpty());
        problem(request("POST", "/api/auth/register", "{", null), 400);
        problem(request("GET", "/api/users/not-a-uuid", null, admin()), 400);
        for (String query : List.of("size=0", "page=-1", "sort=passwordHash,asc", "sort=name,bad", "enabled=invalid")) {
            problem(request("GET", "/api/users?" + query, null, admin()), 400);
        }
        problem(request("DELETE", "/api/users/" + UUID.randomUUID() + "/roles/" + UUID.randomUUID() + "?reason=" + "x".repeat(256), null, admin()), 400);
    }
}
