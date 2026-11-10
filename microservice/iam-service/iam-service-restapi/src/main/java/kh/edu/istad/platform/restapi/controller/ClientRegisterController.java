package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.restapi.dto.request.ClientRegistrationRequest;
import kh.edu.istad.platform.restapi.service.ClientRegistrationManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/connect")
@RequiredArgsConstructor
public class ClientRegisterController {
    private final RegisteredClientRepository clients;
    private final PasswordHasher passwordHasher;
    private final ClientRegistrationManagementService registrationManagement;
    private final SecureRandom secureRandom = new SecureRandom();


    @PostMapping("/register")
    @PreAuthorize("hasAuthority('client:create')")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody ClientRegistrationRequest request
    ) {
        String clientId = UUID.randomUUID().toString();
        String clientSecret = generateSecret();
        String registrationAccessToken = generateSecret();
        var client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret("{bcrypt}" + passwordHasher.hash(clientSecret))
                .clientName(request.clientName())
                .clientAuthenticationMethod(
                        new ClientAuthenticationMethod(
                                request.tokenEndpointAuthMethod()
                        )
                )
                .authorizationGrantTypes(grantTypes ->
                        request.grantTypes().forEach(value ->
                                grantTypes.add(new AuthorizationGrantType(value))
                        )
                )
                .redirectUris(redirectUris ->
                        redirectUris.addAll(request.redirectUris())
                )
                .scopes(scopes ->
                        scopes.addAll(
                                java.util.List.of(
                                        request.scope().trim().split("\\s+")
                                )
                        )
                )
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(
                                request.grantTypes().contains("authorization_code")
                        )
                        .setting(
                                "registration_access_token_hash", passwordHasher.hash(registrationAccessToken)
                        )
                        .build())
                .build();

        clients.save(client);

        var response = Map.<String, Object>of(
                "client_id", clientId,
                "registration_access_token", registrationAccessToken,
                "registration_client_uri", "http://localhost:9999/connect/register/" + clientId,
                "client_secret", clientSecret,
                "client_name", request.clientName(),
                "redirect_uris", request.redirectUris(),
                "grant_types", request.grantTypes(),
                "scope", request.scope(),
                "token_endpoint_auth_method",
                request.tokenEndpointAuthMethod()
        );

        return ResponseEntity
                .created(URI.create("/connect/register/" + clientId))
                .body(response);
    }

    @GetMapping("/register/{clientId}")
    public ResponseEntity<Map<String, Object>> getRegistration(
            @PathVariable String clientId,
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader
    ) {
        var client = registrationManagement.getRegistration(
                clientId,
                authorizationHeader
        );

        return ResponseEntity.ok(Map.of(
                "client_id", client.getClientId(),
                "client_name", client.getClientName(),
                "redirect_uris", client.getRedirectUris(),
                "grant_types", client.getAuthorizationGrantTypes().stream()
                        .map(grant -> grant.getValue())
                        .toList(),
                "scope", String.join(" ", client.getScopes())
        ));
    }

    @PutMapping("/register/{clientId}")
    public ResponseEntity<Map<String, Object>> updateRegistration(
            @PathVariable String clientId,
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @Valid @RequestBody ClientRegistrationRequest request
    ) {
        var client = registrationManagement.updateRegistration(
                clientId,
                authorizationHeader,
                request
        );

        return ResponseEntity.ok(Map.of(
                "client_id", client.getClientId(),
                "client_name", client.getClientName(),
                "redirect_uris", client.getRedirectUris(),
                "scope", String.join(" ", client.getScopes())
        ));
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
