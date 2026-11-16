package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, Long>, JpaSpecificationExecutor<AuditLogJpaEntity> {
}
