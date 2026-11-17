package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.UserAvatarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAvatarJpaRepository extends JpaRepository<UserAvatarJpaEntity, Long> {

    @Query("""
            select a
            from UserAvatarJpaEntity a, UserJpaEntity u
            where u.id = a.userId
              and u.uuid = :uuid
              and u.deleted = false
            """)
    Optional<UserAvatarJpaEntity> findByUserUuid(@Param("uuid") UUID uuid);
}
