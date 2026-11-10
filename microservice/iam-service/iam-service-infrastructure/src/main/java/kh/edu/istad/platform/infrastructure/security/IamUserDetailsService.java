package kh.edu.istad.platform.infrastructure.security;

import kh.edu.istad.platform.iam.application.dto.AuthUserResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.usecase.LoadUserForAuthenticationUseCase;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class IamUserDetailsService implements UserDetailsService {

    private final LoadUserForAuthenticationUseCase loadUserForAuthentication;

    public IamUserDetailsService(
            LoadUserForAuthenticationUseCase loadUserForAuthentication
    ) {
        this.loadUserForAuthentication = loadUserForAuthentication;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
        try {
            AuthUserResult result = loadUserForAuthentication.execute(usernameOrEmail);
            return User.withUsername(result.username())
                    .password(result.passwordHash())
                    .disabled(!result.enabled())
                    .accountExpired(!result.accountNonExpired())
                    .accountLocked(!result.accountNonLocked())
                    .credentialsExpired(!result.credentialsNonExpired())
                    .authorities(java.util.List.of())
                    .build();
        } catch (ResourceNotFoundException exception) {
            throw new UsernameNotFoundException(
                    "User not found: " + usernameOrEmail,
                    exception
            );
        }
    }
}
