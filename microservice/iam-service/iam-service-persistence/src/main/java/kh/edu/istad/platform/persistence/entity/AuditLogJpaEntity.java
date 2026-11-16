package kh.edu.istad.platform.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "audit_logs")
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, updatable = false, length = 100)
    private String actor;

    @Column(nullable = false, updatable = false, length = 100)
    private String action;

    @Column(name = "target_type", nullable = false, updatable = false, length = 50)
    private String targetType;

    @Column(name = "target_id", updatable = false, length = 100)
    private String targetId;

    @Column(updatable = false, length = 1000)
    private String detail;

    @Column(name = "ip_address", updatable = false, length = 100)
    private String ip;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    public static AuditLogJpaEntity create() {
        return new AuditLogJpaEntity();
    }
}
