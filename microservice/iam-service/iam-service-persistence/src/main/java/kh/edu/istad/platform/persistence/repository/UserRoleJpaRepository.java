package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.UserRoleJpaEntity;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleJpaEntity, Long> {
    List<UserRoleJpaEntity> findByUserIdAndRevokedAtIsNull(Long userId);

    Optional<UserRoleJpaEntity> findByUserIdAndRoleIdAndRevokedAtIsNull(Long userId, Long roleId);
}
