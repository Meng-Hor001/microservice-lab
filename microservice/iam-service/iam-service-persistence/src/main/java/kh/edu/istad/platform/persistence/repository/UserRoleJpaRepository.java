package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.UserRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleJpaEntity, Long> {
    List<UserRoleJpaEntity> findByUserIdAndRevokedAtIsNull(Long userId);
}
