package kh.edu.istad.platform.infrastructure.security;

import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public final class SecurityContextCurrentActorProvider implements CurrentActorProvider {
    private static final String SYSTEM_ACTOR = "system";

    @Override
    public String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return SYSTEM_ACTOR;
        }

        String actor = authentication.getName();
        return actor == null || actor.isBlank() ? SYSTEM_ACTOR : actor;
    }
}
