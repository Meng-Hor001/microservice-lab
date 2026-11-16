package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.OAuthClientCredentialsResult;
import kh.edu.istad.platform.iam.application.dto.OAuthClientResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.SaveOAuthClientCommand;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.OAuthClientRepository;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Admin management of OAuth2/OIDC clients: register, view, update, rotate secret, delete. */
public final class ManageOAuthClientsUseCase {
    private static final Set<String> GRANT_TYPES = Set.of("authorization_code", "refresh_token", "client_credentials");
    private static final Set<String> AUTH_METHODS = Set.of("client_secret_basic", "client_secret_post", "none");

    private final OAuthClientRepository clients;
    private final PasswordHasher hasher;
    private final Set<String> protectedClientIds;   // e.g. iam-web: deleting it would lock everyone out
    private final SecureRandom random = new SecureRandom();

    public ManageOAuthClientsUseCase(OAuthClientRepository clients, PasswordHasher hasher, Set<String> protectedClientIds) {
        this.clients = Objects.requireNonNull(clients);
        this.hasher = Objects.requireNonNull(hasher);
        this.protectedClientIds = Set.copyOf(protectedClientIds);
    }

    public PageResult<OAuthClientResult> list(String search, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page request");
        }
        return clients.findAll(search == null || search.isBlank() ? null : search.trim(), page, size);
    }

    public OAuthClientResult get(String clientId) {
        return clients.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("OAuth client", clientId));
    }

    public OAuthClientCredentialsResult create(SaveOAuthClientCommand command) {
        validate(command);
        String clientId = UUID.randomUUID().toString();
        String secret = isPublic(command) ? null : newSecret();
        var client = clients.create(clientId, secret == null ? null : hasher.hash(secret), command);
        return new OAuthClientCredentialsResult(client, secret);
    }

    public OAuthClientResult update(String clientId, SaveOAuthClientCommand command) {
        var existing = get(clientId);
        validate(command);
        if (!existing.authenticationMethod().equals(command.authenticationMethod())
                && ("none".equals(existing.authenticationMethod()) || isPublic(command))) {
            throw new IllegalArgumentException("Switching between public and confidential clients is not supported; "
                    + "register a new client instead");
        }
        return clients.update(clientId, command);
    }

    public OAuthClientCredentialsResult rotateSecret(String clientId) {
        var client = get(clientId);
        if ("none".equals(client.authenticationMethod())) {
            throw new IllegalArgumentException("Public clients have no secret");
        }
        String secret = newSecret();
        clients.updateSecret(clientId, hasher.hash(secret));
        return new OAuthClientCredentialsResult(client, secret);
    }

    public void delete(String clientId) {
        if (protectedClientIds.contains(clientId)) {
            throw new IllegalArgumentException("Client '" + clientId + "' is required by the platform and cannot be deleted");
        }
        if (!clients.delete(clientId)) {
            throw new ResourceNotFoundException("OAuth client", clientId);
        }
    }

    private void validate(SaveOAuthClientCommand c) {
        if (c.clientName() == null || c.clientName().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (!AUTH_METHODS.contains(c.authenticationMethod())) {
            throw new IllegalArgumentException("Unsupported authentication method: " + c.authenticationMethod());
        }
        if (c.grantTypes().isEmpty() || !GRANT_TYPES.containsAll(c.grantTypes())) {
            throw new IllegalArgumentException("Grant types must be from " + GRANT_TYPES);
        }
        if (c.grantTypes().contains("authorization_code") && c.redirectUris().isEmpty()) {
            throw new IllegalArgumentException("authorization_code requires at least one redirect URI");
        }
        if (isPublic(c) && (c.grantTypes().contains("client_credentials") || !c.requireProofKey())) {
            throw new IllegalArgumentException("Public clients must use PKCE and cannot use client_credentials");
        }
        if (c.scopes().isEmpty()) {
            throw new IllegalArgumentException("At least one scope is required");
        }
        if (c.accessTokenMinutes() < 1 || c.accessTokenMinutes() > 1440) {
            throw new IllegalArgumentException("Access token lifetime must be 1-1440 minutes");
        }
        if (c.refreshTokenMinutes() < 1 || c.refreshTokenMinutes() > 525600) {
            throw new IllegalArgumentException("Refresh token lifetime must be 1-525600 minutes");
        }
    }

    private static boolean isPublic(SaveOAuthClientCommand c) {
        return "none".equals(c.authenticationMethod());
    }

    private String newSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
