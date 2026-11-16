package kh.edu.istad.platform.iam.application.dto;

public record UpdateSystemSettingsCommand(
        boolean allowSelfRegistration,
        boolean allowSocialLogin,
        int maxFailedAttempts,
        int lockoutMinutes,
        int passwordMinLength,
        boolean passwordRequireUppercase,
        boolean passwordRequireLowercase,
        boolean passwordRequireDigit,
        boolean passwordRequireSymbol,
        int passwordExpiryDays,
        int sessionIdleMinutes,
        int sessionMaxHours,
        int accessTokenMinutes,
        int refreshTokenDays
) {
}
