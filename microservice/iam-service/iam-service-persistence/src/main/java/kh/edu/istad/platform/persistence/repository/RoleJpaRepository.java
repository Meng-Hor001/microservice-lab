package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity,Long> , JpaSpecificationExecutor<RoleJpaEntity> {
    Optional<RoleJpaEntity> findByUuidAndDeletedFalse(UUID uuid);

    boolean existsByName(String name);
}
