package kh.edu.istad.platform.infrastructure.security;

import kh.edu.istad.platform.iam.application.dto.AuthUserResult;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;

public final class IamOidcUser extends DefaultOidcUser implements UserDetails {
    private final AuthUserResult user;

    public IamOidcUser(
            AuthUserResult user,
            OidcUser providerUser,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(
                authorities,
                providerUser.getIdToken(),
                providerUser.getUserInfo()
        );
        this.user = user;
    }

    @Override
    public String getName() {
        return user.uuid().toString();
    }

    @Override
    public String getUsername() {
        return user.username();
    }

    @Override
    public String getPassword() {
        return user.passwordHash();
    }

    @Override
    public boolean isEnabled() {
        return user.enabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.accountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.accountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.credentialsNonExpired();
    }
}
