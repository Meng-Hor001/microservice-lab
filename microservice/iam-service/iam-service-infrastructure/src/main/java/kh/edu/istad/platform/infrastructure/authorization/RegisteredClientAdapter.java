package kh.edu.istad.platform.infrastructure.authorization;

import kh.edu.istad.platform.iam.application.dto.OAuthClientResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.SaveOAuthClientCommand;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.OAuthClientRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Clients live in Spring Authorization Server's own {@code oauth2_registered_client} table, so this adapter
 * reads and writes through its {@link RegisteredClientRepository} and uses SQL only for what that
 * repository lacks: listing and deleting.
 */
@Component
public class RegisteredClientAdapter implements OAuthClientRepository {
    private final RegisteredClientRepository clients;
    private final JdbcOperations jdbc;

    public RegisteredClientAdapter(RegisteredClientRepository clients, JdbcOperations jdbc) {
        this.clients = clients;
        this.jdbc = jdbc;
    }

    @Override
    public PageResult<OAuthClientResult> findAll(String search, int page, int size) {
        String where = "";
        var arguments = new ArrayList<Object>();
        if (search != null) {
            where = " WHERE LOWER(client_name) LIKE ? OR LOWER(client_id) LIKE ?";
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
            arguments.add(pattern);
            arguments.add(pattern);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM oauth2_registered_client" + where,
                Long.class, arguments.toArray());
        long count = total == null ? 0 : total;

        arguments.add(size);
        arguments.add((long) page * size);
        List<String> clientIds = jdbc.queryForList("SELECT client_id FROM oauth2_registered_client" + where
                + " ORDER BY client_id_issued_at DESC, client_id LIMIT ? OFFSET ?", String.class, arguments.toArray());
        var items = clientIds.stream()
                .map(clients::findByClientId)
                .filter(Objects::nonNull)
                .map(RegisteredClientAdapter::toResult)
                .toList();
        return new PageResult<>(items, page, size, count, (int) ((count + size - 1) / size));
    }

    @Override
    public Optional<OAuthClientResult> findByClientId(String clientId) {
        return Optional.ofNullable(clients.findByClientId(clientId)).map(RegisteredClientAdapter::toResult);
    }

    @Override
    public OAuthClientResult create(String clientId, String secretHash, SaveOAuthClientCommand command) {
        var builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientIdIssuedAt(Instant.now())
                .clientSecret(secretHash);
        RegisteredClient client = apply(builder, command,
                ClientSettings.builder().build(), TokenSettings.builder().build()).build();
        clients.save(client);
        return toResult(client);
    }

    @Override
    public OAuthClientResult update(String clientId, SaveOAuthClientCommand command) {
        RegisteredClient existing = require(clientId);
        RegisteredClient client = apply(RegisteredClient.from(existing), command,
                existing.getClientSettings(), existing.getTokenSettings()).build();
        clients.save(client);
        return toResult(client);
    }

    @Override
    public void updateSecret(String clientId, String secretHash) {
        clients.save(RegisteredClient.from(require(clientId)).clientSecret(secretHash).build());
    }

    @Override
    @Transactional
    public boolean delete(String clientId) {
        RegisteredClient client = clients.findByClientId(clientId);
        if (client == null) {
            return false;
        }
        // Remove issued tokens and consents first so nothing keeps working for a deleted client.
        jdbc.update("DELETE FROM oauth2_authorization WHERE registered_client_id = ?", client.getId());
        jdbc.update("DELETE FROM oauth2_authorization_consent WHERE registered_client_id = ?", client.getId());
        return jdbc.update("DELETE FROM oauth2_registered_client WHERE id = ?", client.getId()) == 1;
    }

    private RegisteredClient require(String clientId) {
        RegisteredClient client = clients.findByClientId(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("OAuth client", clientId);
        }
        return client;
    }

    /** Copies the editable fields onto the builder; other stored client/token settings are kept. */
    private static RegisteredClient.Builder apply(RegisteredClient.Builder builder, SaveOAuthClientCommand c,
                                                  ClientSettings clientSettings, TokenSettings tokenSettings) {
        return builder
                .clientName(c.clientName())
                .clientAuthenticationMethods(methods -> {
                    methods.clear();
                    methods.add(new ClientAuthenticationMethod(c.authenticationMethod()));
                })
                .authorizationGrantTypes(grants -> {
                    grants.clear();
                    c.grantTypes().forEach(grant -> grants.add(new AuthorizationGrantType(grant)));
                })
                .redirectUris(uris -> {
                    uris.clear();
                    uris.addAll(c.redirectUris());
                })
                .postLogoutRedirectUris(uris -> {
                    uris.clear();
                    uris.addAll(c.postLogoutRedirectUris());
                })
                .scopes(scopes -> {
                    scopes.clear();
                    scopes.addAll(c.scopes());
                })
                .clientSettings(ClientSettings.withSettings(clientSettings.getSettings())
                        .requireProofKey(c.requireProofKey())
                        .requireAuthorizationConsent(c.requireConsent())
                        .build())
                .tokenSettings(TokenSettings.withSettings(tokenSettings.getSettings())
                        .accessTokenTimeToLive(Duration.ofMinutes(c.accessTokenMinutes()))
                        .refreshTokenTimeToLive(Duration.ofMinutes(c.refreshTokenMinutes()))
                        .build());
    }

    private static OAuthClientResult toResult(RegisteredClient client) {
        return new OAuthClientResult(
                client.getClientId(),
                client.getClientName(),
                client.getClientAuthenticationMethods().stream().findFirst()
                        .map(ClientAuthenticationMethod::getValue).orElse("none"),
                client.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.toUnmodifiableSet()),
                copy(client.getRedirectUris()),
                copy(client.getPostLogoutRedirectUris()),
                copy(client.getScopes()),
                client.getClientSettings().isRequireProofKey(),
                client.getClientSettings().isRequireAuthorizationConsent(),
                client.getTokenSettings().getAccessTokenTimeToLive().toMinutes(),
                client.getTokenSettings().getRefreshTokenTimeToLive().toMinutes(),
                client.getClientIdIssuedAt()
        );
    }

    private static Set<String> copy(Set<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }
}
