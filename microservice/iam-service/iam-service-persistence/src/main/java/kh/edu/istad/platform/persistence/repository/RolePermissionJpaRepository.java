package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.persistence.entity.RolePermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, Long> {

    List<RolePermissionJpaEntity> findByRoleIdAndRevokedAtIsNull(Long roleId);
}
