package kh.edu.istad.platform.iam.application.dto;

/** The password rules currently configured in system settings. */
public record PasswordPolicyResult(
        int minLength,
        int maxLength,
        boolean requireUppercase,
        boolean requireLowercase,
        boolean requireDigit,
        boolean requireSymbol
) {
}
