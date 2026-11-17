package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.UserRoleJpaEntity;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleJpaEntity, Long> {
    List<UserRoleJpaEntity> findByUserIdAndRevokedAtIsNull(Long userId);

    Optional<UserRoleJpaEntity> findByUserIdAndRoleIdAndRevokedAtIsNull(Long userId, Long roleId);

    // Names of the roles each user currently holds: not revoked, not expired, role not deleted.
    @Query("""
            select ur.userId as userId, r.name as roleName
            from UserRoleJpaEntity ur, RoleJpaEntity r
            where r.id = ur.roleId
              and r.deleted = false
              and ur.userId in :userIds
              and ur.revokedAt is null
              and (ur.expiresAt is null or ur.expiresAt > :now)
            order by r.name
            """)
    List<UserRoleNameView> findActiveRoleNames(@Param("userIds") Collection<Long> userIds, @Param("now") Instant now);

    // Users who currently hold each role: not revoked, not expired, user not deleted.
    @Query("""
            select ur.roleId as roleId, count(distinct ur.userId) as total
            from UserRoleJpaEntity ur, UserJpaEntity u
            where u.id = ur.userId
              and u.deleted = false
              and ur.roleId in :roleIds
              and ur.revokedAt is null
              and (ur.expiresAt is null or ur.expiresAt > :now)
            group by ur.roleId
            """)
    List<RoleCountView> countActiveByRoleIds(@Param("roleIds") Collection<Long> roleIds, @Param("now") Instant now);
}
