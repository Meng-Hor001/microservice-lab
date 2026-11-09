package kh.edu.istad.platform.restapi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ClientRegistrationRequest(
        @JsonProperty("client_name")
        @NotBlank
        @Size(max = 200)
        String clientName,

        @JsonProperty("redirect_uris")
        @NotEmpty
        Set<@NotBlank @Size(max = 1000) String> redirectUris,

        @JsonProperty("grant_types")
        @NotEmpty
        Set<@NotBlank String> grantTypes,

        @JsonProperty("scope")
        @NotBlank
        String scope,

        @JsonProperty("token_endpoint_auth_method")
        @NotBlank
        String tokenEndpointAuthMethod
) {
}
