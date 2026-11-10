package kh.edu.istad.platform.infrastructure.security;

import org.springframework.stereotype.Component;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
