package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;

public record SystemSettingsResult(
        // authentication policy
        boolean allowSelfRegistration,
        boolean allowSocialLogin,
        int maxFailedAttempts,
        int lockoutMinutes,
        // password policy
        int passwordMinLength,
        boolean passwordRequireUppercase,
        boolean passwordRequireLowercase,
        boolean passwordRequireDigit,
        boolean passwordRequireSymbol,
        int passwordExpiryDays,
        // session expiration
        int sessionIdleMinutes,
        int sessionMaxHours,
        // token lifetimes
        int accessTokenMinutes,
        int refreshTokenDays,
        // metadata
        Instant updatedAt,
        String updatedBy
) {
}
