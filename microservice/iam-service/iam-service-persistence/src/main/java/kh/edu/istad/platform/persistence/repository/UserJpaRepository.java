package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, JpaSpecificationExecutor<UserJpaEntity> {
    boolean existsByUsername(String name);

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByUuidAndDeletedFalse(UUID uuid);

    @Query("""
            select u
            from UserJpaEntity u
            where u.deleted = false
              and (u.username = :value or u.email = :value)
            """)
    Optional<UserJpaEntity> findByUsernameOrEmailAndDeletedFalse(
            @Param("value") String value
    );

}
