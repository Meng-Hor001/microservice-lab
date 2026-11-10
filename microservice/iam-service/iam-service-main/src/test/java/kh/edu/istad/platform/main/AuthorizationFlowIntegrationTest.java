package kh.edu.istad.platform.main;

import kh.edu.istad.platform.restapi.security.ApiJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:iam-test.properties")
@Import(AuthorizationFlowIntegrationTest.LocalDecoder.class)
class AuthorizationFlowIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry p) {
        p.add("spring.datasource.url", DB::getJdbcUrl);
        p.add("spring.datasource.username", DB::getUsername);
        p.add("spring.datasource.password", DB::getPassword);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class LocalDecoder {
        @Bean
        @Primary
        JwtDecoder flowDecoder(Environment environment) {
            return token -> new ApiJwtDecoderConfig().jwtDecoder("http://localhost:9999",
                    "http://localhost:" + environment.getProperty("local.server.port") + "/oauth2/jwks",
                    "iam-api").decode(token);
        }
    }

    @LocalServerPort
    int port;
    @Autowired
    RegisteredClientRepository clients;
    final JsonMapper json = JsonMapper.builder().build();
    final HttpClient browser = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();

    String base() {
        return "http://localhost:" + port;
    }

    static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    static String form(Map<String, String> values) {
        return values.entrySet().stream().map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
    }

    HttpResponse<String> send(String method, String path, String body, String contentType, String authorization) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(path.startsWith("http") ? path : base() + path));
        request.header("Accept", "application/json");
        if (contentType != null) request.header("Content-Type", contentType);
        if (authorization != null) request.header("Authorization", authorization);
        return browser.send(request.method(method, HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    JsonNode expect(HttpResponse<String> response, int status) {
        assertEquals(status, response.statusCode(), () -> "OAuth error: "
                + json.readTree(response.body()).path("error").asString() + " "
                + json.readTree(response.body()).path("error_description").asString());
        return json.readTree(response.body());
    }

    String csrf(String html) {
        var matcher = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(html);
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    String basic(String id, String secret) {
        return "Basic " + Base64.getEncoder().encodeToString((enc(id) + ":" + enc(secret)).getBytes(StandardCharsets.UTF_8));
    }

    String parameter(String location, String name) {
        return Arrays.stream(URI.create(location).getRawQuery().split("&"))
                .map(v -> v.split("=", 2)).filter(v -> v[0].equals(name))
                .map(v -> URLDecoder.decode(v[1], StandardCharsets.UTF_8)).findFirst().orElseThrow();
    }

    @Test
    void socialAuthorizationEndpointsRedirectToTheConfiguredProviders() throws Exception {
        var hosts = Map.of("google", "accounts.google.com", "github", "github.com",
                "facebook", "www.facebook.com", "telegram", "oauth.telegram.org");
        for (var provider : hosts.entrySet()) {
            var response = send("GET", "/oauth2/authorization/" + provider.getKey(), "", null, null);
            assertEquals(302, response.statusCode());
            var location = URI.create(response.headers().firstValue("Location").orElseThrow());
            assertEquals(provider.getValue(), location.getHost());
            assertFalse(parameter(location.toString(), "state").isBlank());
            assertEquals("code", parameter(location.toString(), "response_type"));
        }
    }

    @Test
    void dynamicRegistrationConsentCodeExchangeAndOwnApiToken() throws Exception {
        var initial = expect(send("POST", "/oauth2/token", form(Map.of(
                        "grant_type", "client_credentials", "scope", "client.create")),
                "application/x-www-form-urlencoded", basic("integration-test-registrar", "integration-test-only-secret")), 200);
        String callback = "http://127.0.0.1:8081/callback";
        String registration = json.writeValueAsString(Map.of(
                "client_name", "Flow test", "redirect_uris", List.of(callback),
                "grant_types", List.of("authorization_code", "refresh_token"),
                "response_types", List.of("code"), "scope", "openid profile email",
                "token_endpoint_auth_method", "client_secret_basic"));
        assertEquals(401, send("POST", "/connect/register", registration, "application/json", null).statusCode());
        var registered = expect(send("POST", "/connect/register", registration, "application/json",
                "Bearer " + initial.path("access_token").asString()), 201);
        String id = registered.path("client_id").asString();
        String secret = registered.path("client_secret").asString();
        assertFalse(id.isBlank());
        assertFalse(secret.isBlank());
        String registrationUri = registered.path("registration_client_uri").asString();
        String managementPath = URI.create(registrationUri).getRawPath() + "?" + URI.create(registrationUri).getRawQuery();
        var read = expect(send("GET", managementPath, "", null,
                "Bearer " + registered.path("registration_access_token").asString()), 200);
        assertEquals(id, read.path("client_id").asString());
        assertEquals(401, send("GET", managementPath, "", null, "Bearer invalid").statusCode());
        var stored = clients.findByClientId(id);
        clients.save(org.springframework.security.oauth2.server.authorization.client.RegisteredClient.from(stored)
                .clientSettings(ClientSettings.withSettings(stored.getClientSettings().getSettings())
                        .requireAuthorizationConsent(true).requireProofKey(true).build()).build());
        String username = "flow_" + UUID.randomUUID().toString().substring(0, 8);
        expect(send("POST", "/api/auth/register", json.writeValueAsString(Map.of(
                        "username", username, "email", username + "@example.com", "password", "Str0ng!Pass2026",
                        "confirmPassword", "Str0ng!Pass2026", "givenName", "Dara", "familyName", "Sok", "locale", "en")),
                "application/json", null), 201);
        String verifier = UUID.randomUUID().toString() + UUID.randomUUID();
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        String authorize = "/oauth2/authorize?" + form(Map.of("response_type", "code", "client_id", id,
                "redirect_uri", callback, "scope", "openid profile email", "state", "flow-state",
                "code_challenge", challenge, "code_challenge_method", "S256"));
        var loginPage = send("GET", "/login", "", null, null);
        var login = send("POST", "/login", form(Map.of("username", username, "password", "Str0ng!Pass2026",
                "_csrf", csrf(loginPage.body()))), "application/x-www-form-urlencoded", null);
        assertEquals(302, login.statusCode());
        var authorizeResponse = send("GET", authorize, "", null, null);
        assertEquals(302, authorizeResponse.statusCode());
        String consentLocation = authorizeResponse.headers().firstValue("Location").orElseThrow();
        assertTrue(consentLocation.contains("/oauth2/consent"));
        var consentPage = send("GET", consentLocation, "", null, null);
        assertEquals(200, consentPage.statusCode());
        String approval = form(Map.of("client_id", id, "state", parameter(consentLocation, "state"),
                "_csrf", csrf(consentPage.body()))) + "&scope=profile&scope=email";
        var approved = send("POST", "/oauth2/authorize", approval, "application/x-www-form-urlencoded", null);
        assertEquals(302, approved.statusCode());
        String redirect = approved.headers().firstValue("Location").orElseThrow();
        assertEquals("flow-state", parameter(redirect, "state"));
        String code = parameter(redirect, "code");
        var tokens = expect(send("POST", "/oauth2/token", form(Map.of("grant_type", "authorization_code",
                        "code", code, "redirect_uri", callback, "code_verifier", verifier)),
                "application/x-www-form-urlencoded", basic(id, secret)), 200);
        String accessToken = tokens.path("access_token").asString();
        assertFalse(tokens.path("id_token").asString().isBlank());
        assertFalse(tokens.path("refresh_token").asString().isBlank());
        var me = expect(send("GET", "/api/me", "", null, "Bearer " + accessToken), 200);
        assertEquals(username, me.path("username").asString());
        var info = expect(send("GET", "/userinfo", "", null, "Bearer " + accessToken), 200);
        assertEquals(me.path("uuid").asString(), info.path("sub").asString());
        var claims = json.readTree(Base64.getUrlDecoder().decode(accessToken.split("\\.")[1]));
        var audience = claims.path("aud");
        assertEquals("iam-api", audience.isArray() ? audience.get(0).asString() : audience.asString());
        assertTrue(claims.has("roles"));
        assertTrue(claims.has("permissions"));
        expect(send("POST", "/oauth2/token", form(Map.of("grant_type", "refresh_token",
                        "refresh_token", tokens.path("refresh_token").asString())),
                "application/x-www-form-urlencoded", basic(id, secret)), 200);
        assertEquals(400, send("POST", "/oauth2/token", form(Map.of("grant_type", "authorization_code",
                        "code", code, "redirect_uri", callback, "code_verifier", verifier)),
                "application/x-www-form-urlencoded", basic(id, secret)).statusCode());
    }
}
