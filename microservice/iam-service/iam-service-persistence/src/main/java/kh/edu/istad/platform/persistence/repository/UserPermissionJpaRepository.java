package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.UserPermissionJpaEntity;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPermissionJpaRepository extends JpaRepository<UserPermissionJpaEntity, Long> {
    List<UserPermissionJpaEntity> findByUserIdAndRevokedAtIsNull(Long userId);
}
