package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, Long>, JpaSpecificationExecutor<PermissionJpaEntity> {


    Optional<PermissionJpaEntity> findByUuidAndDeletedFalse(UUID uuid);

    boolean existsByName(String name);
}
