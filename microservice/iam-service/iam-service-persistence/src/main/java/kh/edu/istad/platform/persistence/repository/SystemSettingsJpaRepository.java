package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.SystemSettingsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsJpaRepository extends JpaRepository<SystemSettingsJpaEntity, Short> {
}
