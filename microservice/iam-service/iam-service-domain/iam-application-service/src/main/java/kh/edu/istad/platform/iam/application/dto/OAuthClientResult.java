package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.Set;

public record OAuthClientResult(
        String clientId,
        String clientName,
        String authenticationMethod,      // client_secret_basic | client_secret_post | none
        Set<String> grantTypes,           // authorization_code | refresh_token | client_credentials
        Set<String> redirectUris,
        Set<String> postLogoutRedirectUris,
        Set<String> scopes,
        boolean requireProofKey,          // PKCE
        boolean requireConsent,
        long accessTokenMinutes,
        long refreshTokenMinutes,
        Instant issuedAt
) {
}
