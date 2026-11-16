package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kh.edu.istad.platform.iam.application.dto.SaveOAuthClientCommand;

import java.util.Set;

/** Body of POST /api/clients and PUT /api/clients/{clientId}. */
public record SaveOAuthClientRequest(
        @NotBlank @Size(max = 200) String clientName,
        @NotBlank @Pattern(regexp = "client_secret_basic|client_secret_post|none") String authenticationMethod,
        @NotEmpty Set<@Pattern(regexp = "authorization_code|refresh_token|client_credentials") String> grantTypes,
        Set<@NotBlank @Size(max = 1000) String> redirectUris,
        Set<@NotBlank @Size(max = 1000) String> postLogoutRedirectUris,
        @NotEmpty Set<@NotBlank @Size(max = 100) String> scopes,
        Boolean requireProofKey,
        Boolean requireConsent,
        @Min(1) @Max(1440) Long accessTokenMinutes,
        @Min(1) @Max(525600) Long refreshTokenMinutes
) {
    public SaveOAuthClientCommand toCommand() {
        boolean usesAuthorizationCode = grantTypes != null && grantTypes.contains("authorization_code");
        return new SaveOAuthClientCommand(
                clientName,
                authenticationMethod,
                grantTypes,
                redirectUris,
                postLogoutRedirectUris,
                scopes,
                requireProofKey == null ? usesAuthorizationCode : requireProofKey,
                requireConsent != null && requireConsent,
                accessTokenMinutes == null ? 5 : accessTokenMinutes,      // Spring Authorization Server defaults
                refreshTokenMinutes == null ? 60 : refreshTokenMinutes
        );
    }
}
