package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.persistence.entity.RolePermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, Long> {

    List<RolePermissionJpaEntity> findByRoleIdAndRevokedAtIsNull(Long roleId);

    List<RolePermissionJpaEntity> findByRoleIdInAndRevokedAtIsNull(List<Long> roleIds);

    // Roles that currently grant each permission: not revoked, not expired, role not deleted.
    @Query("""
            select rp.permissionId as permissionId, count(distinct rp.roleId) as total
            from RolePermissionJpaEntity rp, RoleJpaEntity r
            where r.id = rp.roleId
              and r.deleted = false
              and rp.permissionId in :permissionIds
              and rp.revokedAt is null
              and (rp.expiresAt is null or rp.expiresAt > :now)
            group by rp.permissionId
            """)
    List<PermissionCountView> countActiveRolesByPermissionIds(
            @Param("permissionIds") Collection<Long> permissionIds, @Param("now") Instant now);

    // Permissions each role currently grants: not revoked, not expired, permission not deleted.
    @Query("""
            select rp.roleId as roleId, count(rp) as total
            from RolePermissionJpaEntity rp, PermissionJpaEntity p
            where p.id = rp.permissionId
              and p.deleted = false
              and rp.roleId in :roleIds
              and rp.revokedAt is null
              and (rp.expiresAt is null or rp.expiresAt > :now)
            group by rp.roleId
            """)
    List<RoleCountView> countActiveByRoleIds(@Param("roleIds") Collection<Long> roleIds, @Param("now") Instant now);
}
