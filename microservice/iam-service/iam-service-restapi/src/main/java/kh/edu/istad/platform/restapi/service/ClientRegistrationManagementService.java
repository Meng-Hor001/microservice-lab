package kh.edu.istad.platform.restapi.service;

import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.restapi.dto.request.ClientRegistrationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClientRegistrationManagementService {

    private static final String REGISTRATION_TOKEN_HASH_SETTING =
            "registration_access_token_hash";

    private final RegisteredClientRepository clients;
    private final PasswordHasher passwordHasher;

    public ClientRegistrationManagementService(
            RegisteredClientRepository clients,
            PasswordHasher passwordHasher
    ) {
        this.clients = clients;
        this.passwordHasher = passwordHasher;
    }

    public RegisteredClient getRegistration(
            String clientId,
            String authorizationHeader
    ) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        RegisteredClient client = clients.findByClientId(clientId);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        Object storedHash = client.getClientSettings()
                .getSetting(REGISTRATION_TOKEN_HASH_SETTING);

        if (!(storedHash instanceof String hash)
                || !passwordHasher.matches(token, hash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return client;
    }

    public RegisteredClient updateRegistration(
            String clientId,
            String authorizationHeader,
            ClientRegistrationRequest request
    ) {
        RegisteredClient client = getRegistration(clientId, authorizationHeader);

        String tokenHash = (String) client.getClientSettings()
                .getSetting(REGISTRATION_TOKEN_HASH_SETTING);

        RegisteredClient updated = RegisteredClient.from(client)
                .clientName(request.clientName())
                .clientAuthenticationMethods(methods -> {
                    methods.clear();
                    methods.add(new ClientAuthenticationMethod(
                            request.tokenEndpointAuthMethod()
                    ));
                })
                .authorizationGrantTypes(grants -> {
                    grants.clear();
                    request.grantTypes().forEach(value ->
                            grants.add(new AuthorizationGrantType(value))
                    );
                })
                .redirectUris(uris -> {
                    uris.clear();
                    uris.addAll(request.redirectUris());
                })
                .scopes(scopes -> {
                    scopes.clear();
                    scopes.addAll(java.util.List.of(
                            request.scope().trim().split("\\s+")
                    ));
                })
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(
                                request.grantTypes().contains("authorization_code")
                        )
                        .setting(REGISTRATION_TOKEN_HASH_SETTING, tokenHash)
                        .build())
                .build();

        clients.save(updated);
        return updated;
    }
}
