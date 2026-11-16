package kh.edu.istad.platform.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** The single row of {@code system_settings} (id is always 1). */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "system_settings")
public class SystemSettingsJpaEntity {
    public static final short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column(name = "allow_self_registration", nullable = false)
    private boolean allowSelfRegistration;
    @Column(name = "allow_social_login", nullable = false)
    private boolean allowSocialLogin;
    @Column(name = "max_failed_attempts", nullable = false)
    private int maxFailedAttempts;
    @Column(name = "lockout_minutes", nullable = false)
    private int lockoutMinutes;

    @Column(name = "password_min_length", nullable = false)
    private int passwordMinLength;
    @Column(name = "password_require_uppercase", nullable = false)
    private boolean passwordRequireUppercase;
    @Column(name = "password_require_lowercase", nullable = false)
    private boolean passwordRequireLowercase;
    @Column(name = "password_require_digit", nullable = false)
    private boolean passwordRequireDigit;
    @Column(name = "password_require_symbol", nullable = false)
    private boolean passwordRequireSymbol;
    @Column(name = "password_expiry_days", nullable = false)
    private int passwordExpiryDays;

    @Column(name = "session_idle_minutes", nullable = false)
    private int sessionIdleMinutes;
    @Column(name = "session_max_hours", nullable = false)
    private int sessionMaxHours;

    @Column(name = "access_token_minutes", nullable = false)
    private int accessTokenMinutes;
    @Column(name = "refresh_token_days", nullable = false)
    private int refreshTokenDays;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @Version
    @Column(nullable = false)
    private Long version;
}
