package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.UserPermissionJpaEntity;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPermissionJpaRepository extends JpaRepository<UserPermissionJpaEntity, Long> {

    List<UserPermissionJpaEntity> findByUserIdAndRevokedAtIsNull(Long userId);


    Optional<UserPermissionJpaEntity>
    findByUserIdAndPermissionIdAndRevokedAtIsNull(Long userId, Long permissionId);
}
