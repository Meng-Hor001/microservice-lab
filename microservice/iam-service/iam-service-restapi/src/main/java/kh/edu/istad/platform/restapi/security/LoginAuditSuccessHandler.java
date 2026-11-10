package kh.edu.istad.platform.restapi.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.edu.istad.platform.iam.application.usecase.LoadUserForAuthenticationUseCase;
import kh.edu.istad.platform.iam.application.usecase.RecordIdentityLoginUseCase;
import kh.edu.istad.platform.iam.application.usecase.RecordLoginAttemptUseCase;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Locale;

final class LoginAuditSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final RecordLoginAttemptUseCase attempts;
    private final RecordIdentityLoginUseCase identities;
    private final LoadUserForAuthenticationUseCase loadUser;

    LoginAuditSuccessHandler(RecordLoginAttemptUseCase attempts,
                             RecordIdentityLoginUseCase identities,
                             LoadUserForAuthenticationUseCase loadUser) {
        this.attempts = attempts;
        this.identities = identities;
        this.loadUser = loadUser;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {
        String username = authentication.getPrincipal() instanceof UserDetails user
                ? user.getUsername() : authentication.getName();
        ApiSecurityConfig.auditLogin(attempts, username, true);

        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            try {
                var user = loadUser.execute(username);
                var provider = IdentityProvider.valueOf(
                        oauth.getAuthorizedClientRegistrationId().toUpperCase(Locale.ROOT));
                identities.execute(user.uuid(), provider);
            } catch (RuntimeException exception) {
                LoggerFactory.getLogger(LoginAuditSuccessHandler.class)
                        .warn("Could not record identity login audit: {}",
                                exception.getClass().getSimpleName());
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
