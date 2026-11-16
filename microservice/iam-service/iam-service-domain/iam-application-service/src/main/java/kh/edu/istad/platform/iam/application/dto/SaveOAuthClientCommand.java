package kh.edu.istad.platform.iam.application.dto;

import java.util.Set;

public record SaveOAuthClientCommand(
        String clientName,
        String authenticationMethod,
        Set<String> grantTypes,
        Set<String> redirectUris,
        Set<String> postLogoutRedirectUris,
        Set<String> scopes,
        boolean requireProofKey,
        boolean requireConsent,
        long accessTokenMinutes,
        long refreshTokenMinutes
) {
    public SaveOAuthClientCommand {
        clientName = clientName == null ? null : clientName.trim();
        grantTypes = grantTypes == null ? Set.of() : Set.copyOf(grantTypes);
        redirectUris = redirectUris == null ? Set.of() : Set.copyOf(redirectUris);
        postLogoutRedirectUris = postLogoutRedirectUris == null ? Set.of() : Set.copyOf(postLogoutRedirectUris);
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
    }
}
