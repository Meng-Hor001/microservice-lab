package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import kh.edu.istad.platform.iam.application.dto.UpdateSystemSettingsCommand;

/** Body of PUT /api/settings. Wrapper types + @NotNull so a missing field is a 400, not a silent false/0. */
public record UpdateSystemSettingsRequest(
        @NotNull Boolean allowSelfRegistration,
        @NotNull Boolean allowSocialLogin,
        @NotNull @Min(1) @Max(20) Integer maxFailedAttempts,
        @NotNull @Min(1) @Max(1440) Integer lockoutMinutes,

        @NotNull @Min(6) @Max(128) Integer passwordMinLength,
        @NotNull Boolean passwordRequireUppercase,
        @NotNull Boolean passwordRequireLowercase,
        @NotNull Boolean passwordRequireDigit,
        @NotNull Boolean passwordRequireSymbol,
        @NotNull @Min(0) @Max(365) Integer passwordExpiryDays,

        @NotNull @Min(5) @Max(1440) Integer sessionIdleMinutes,
        @NotNull @Min(1) @Max(720) Integer sessionMaxHours,

        @NotNull @Min(1) @Max(1440) Integer accessTokenMinutes,
        @NotNull @Min(1) @Max(365) Integer refreshTokenDays
) {
    public UpdateSystemSettingsCommand toCommand() {
        return new UpdateSystemSettingsCommand(
                allowSelfRegistration, allowSocialLogin, maxFailedAttempts, lockoutMinutes,
                passwordMinLength, passwordRequireUppercase, passwordRequireLowercase,
                passwordRequireDigit, passwordRequireSymbol, passwordExpiryDays,
                sessionIdleMinutes, sessionMaxHours, accessTokenMinutes, refreshTokenDays);
    }
}
