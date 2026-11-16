package kh.edu.istad.platform.iam.application.dto;

/** Returned only when a client is created or its secret is rotated: the plain secret is never stored. */
public record OAuthClientCredentialsResult(
        OAuthClientResult client,
        String clientSecret   // null for public clients (authenticationMethod = none)
) {
}
