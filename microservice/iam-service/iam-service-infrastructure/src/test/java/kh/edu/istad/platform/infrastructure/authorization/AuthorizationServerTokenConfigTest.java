package kh.edu.istad.platform.infrastructure.authorization;

import kh.edu.istad.platform.iam.application.dto.AuthUserResult;
import kh.edu.istad.platform.iam.application.dto.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.application.usecase.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.application.usecase.LoadUserForAuthenticationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthorizationServerTokenConfigTest {
    @Test
    void userAccessTokenTargetsApiWhileIdTokenKeepsClientAudience() {
        var load = mock(LoadUserForAuthenticationUseCase.class);
        var authorities = mock(GetUserAuthoritiesUseCase.class);
        var user = mock(AuthUserResult.class);
        var uuid = UUID.randomUUID();
        when(user.uuid()).thenReturn(uuid);
        when(load.execute("dara")).thenReturn(user);
        when(authorities.execute(uuid)).thenReturn(new UserAuthoritiesResult(
                List.of(), List.of(), List.of("user:read")));
        var accounts = mock(AccountSecurityRepository.class);
        var sessionId = UUID.randomUUID();
        when(accounts.registerSession(eq(uuid), anyString(), any(), any(), any())).thenReturn(sessionId);
        var client = RegisteredClient.withId("client-id").clientId("web-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback").build();
        var authorization = OAuth2Authorization.withRegisteredClient(client)
                .id("authorization-id").principalName("dara")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).build();
        var customizer = new AuthorizationServerTokenConfig()
                .jwtTokenCustomizer(load, authorities, "iam-api", accounts, Clock.systemUTC());
        var principal = UsernamePasswordAuthenticationToken.authenticated(
                User.withUsername("dara").password("unused").authorities("user:read").build(),
                null, List.of());

        for (var type : List.of(OAuth2TokenType.ACCESS_TOKEN, new OAuth2TokenType("id_token"))) {
            var claims = JwtClaimsSet.builder().audience(List.of("web-client"));
            var context = JwtEncodingContext.with(
                            JwsHeader.with(SignatureAlgorithm.RS256), claims)
                    .registeredClient(client)
                    .authorization(authorization)
                    .principal(principal)
                    .authorizedScopes(Set.of("openid"))
                    .tokenType(type)
                    .build();
            customizer.customize(context);
            var token = claims.build();
            assertEquals(uuid.toString(), token.getSubject());
            assertEquals(List.of(type.equals(OAuth2TokenType.ACCESS_TOKEN)
                    ? "iam-api" : "web-client"), token.getAudience());
            if (type.equals(OAuth2TokenType.ACCESS_TOKEN)) {
                assertEquals(List.of("user:read"), token.getClaim("permissions"));
                assertEquals(sessionId.toString(), token.getClaim("sid"));
            }
        }
    }
}
