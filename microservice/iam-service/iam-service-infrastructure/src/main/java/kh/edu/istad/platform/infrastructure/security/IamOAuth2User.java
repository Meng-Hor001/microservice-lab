package kh.edu.istad.platform.infrastructure.security;

import kh.edu.istad.platform.iam.application.dto.AuthUserResult;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class IamOAuth2User extends DefaultOAuth2User implements UserDetails {
    private final AuthUserResult user;

    public IamOAuth2User(
            AuthUserResult user,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes
    ) {
        super(authorities, attributes, "id");
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
