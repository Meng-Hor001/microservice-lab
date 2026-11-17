package kh.edu.istad.platform.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** A user's uploaded profile photo; one row per user. */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_avatars")
public class UserAvatarJpaEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    // Plain byte[] maps to PostgreSQL BYTEA (no @Lob, which would use large-object OIDs).
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static UserAvatarJpaEntity forUser(Long userId) {
        UserAvatarJpaEntity entity = new UserAvatarJpaEntity();
        entity.userId = userId;
        return entity;
    }
}
