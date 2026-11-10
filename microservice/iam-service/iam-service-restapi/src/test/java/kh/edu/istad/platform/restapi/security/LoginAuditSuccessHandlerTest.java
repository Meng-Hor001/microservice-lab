package kh.edu.istad.platform.restapi.security;

import kh.edu.istad.platform.iam.application.dto.AuthUserResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginAuditSuccessHandlerTest {
    final RecordLoginAttemptUseCase attempts = mock(RecordLoginAttemptUseCase.class);
    final RecordIdentityLoginUseCase identities = mock(RecordIdentityLoginUseCase.class);
    final LoadUserForAuthenticationUseCase load = mock(LoadUserForAuthenticationUseCase.class);
    final LoginAuditSuccessHandler handler = new LoginAuditSuccessHandler(attempts, identities, load);

    @Test
    void localSuccessAuditsAndReturnsToSavedAuthorizationRequest() throws Exception {
        var request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setParameter("client_id", "web-client");
        var cache = new HttpSessionRequestCache();
        cache.saveRequest(request, new MockHttpServletResponse());
        String target = cache.getRequest(request, new MockHttpServletResponse()).getRedirectUrl();
        var response = new MockHttpServletResponse();
        var principal = User.withUsername("dara").password("unused").authorities("USER").build();

        handler.onAuthenticationSuccess(request, response,
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));

        verify(attempts).execute("dara", true);
        verifyNoInteractions(identities, load);
        assertEquals(target, response.getRedirectedUrl());
    }

    @ParameterizedTest
    @EnumSource(IdentityProvider.class)
    void socialSuccessUsesLocalUsernameAndAuditsMatchingProvider(IdentityProvider provider) throws Exception {
        var principal = mock(OAuth2User.class, withSettings().extraInterfaces(UserDetails.class));
        when(((UserDetails) principal).getUsername()).thenReturn("dara");
        when(principal.getName()).thenReturn(UUID.randomUUID().toString());
        var user = mock(AuthUserResult.class);
        var uuid = UUID.randomUUID();
        when(user.uuid()).thenReturn(uuid);
        when(load.execute("dara")).thenReturn(user);
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                new OAuth2AuthenticationToken(principal, List.of(),
                        provider.name().toLowerCase(Locale.ROOT)));

        verify(attempts).execute("dara", true);
        verify(identities).execute(uuid, provider);
        assertEquals("/", response.getRedirectedUrl());
    }

    @Test
    void failureAuditIgnoresUnknownUsersAndBlankNames() {
        doThrow(new ResourceNotFoundException("User", "unknown"))
                .when(attempts).execute("unknown", false);
        assertDoesNotThrow(() -> ApiSecurityConfig.auditLogin(attempts, "unknown", false));
        ApiSecurityConfig.auditLogin(attempts, "", false);
        verify(attempts).execute("unknown", false);
        verifyNoMoreInteractions(attempts);
    }

    @Test
    void auditStorageFailureDoesNotBreakSuccessfulLogin() throws Exception {
        doThrow(new IllegalStateException("storage unavailable"))
                .when(attempts).execute("dara", true);
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                UsernamePasswordAuthenticationToken.authenticated("dara", null, List.of()));
        assertEquals("/", response.getRedirectedUrl());
    }
}
