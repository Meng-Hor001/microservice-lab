package kh.edu.istad.platform.iam.application.support;

import kh.edu.istad.platform.iam.application.dto.PasswordPolicyResult;
import kh.edu.istad.platform.iam.application.port.out.SystemSettingsRepository;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Checks new passwords against the password policy an administrator configured in system settings.
 * Used by every flow that sets a password: sign-up, admin create, change, admin reset and reset by link.
 */
public final class PasswordPolicy {
    /** Upper bound regardless of settings: BCrypt only uses the first 72 bytes. */
    public static final int MAX_LENGTH = 64;

    private final SystemSettingsRepository settings;

    public PasswordPolicy(SystemSettingsRepository settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    public PasswordPolicyResult current() {
        var s = settings.get();
        return new PasswordPolicyResult(
                s.passwordMinLength(), MAX_LENGTH,
                s.passwordRequireUppercase(), s.passwordRequireLowercase(),
                s.passwordRequireDigit(), s.passwordRequireSymbol());
    }

    /** Returns the password unchanged when it follows the policy, so it can wrap a hash call. */
    public String checked(String password) {
        var p = current();
        var missing = new ArrayList<String>();
        if (password == null || password.length() < p.minLength() || password.length() > p.maxLength()) {
            missing.add(p.minLength() + "-" + p.maxLength() + " characters");
        }
        String value = password == null ? "" : password;
        if (p.requireUppercase() && !value.matches("(?s).*[A-Z].*")) missing.add("an uppercase letter");
        if (p.requireLowercase() && !value.matches("(?s).*[a-z].*")) missing.add("a lowercase letter");
        if (p.requireDigit() && !value.matches("(?s).*\\d.*")) missing.add("a number");
        if (p.requireSymbol() && !value.matches("(?s).*[^A-Za-z0-9].*")) missing.add("a symbol");
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Password must have " + String.join(", ", missing));
        }
        return password;
    }
}
