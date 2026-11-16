package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.application.dto.AuditLogResult;
import kh.edu.istad.platform.iam.application.dto.RecordAuditLogCommand;
import kh.edu.istad.platform.persistence.entity.AuditLogJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    @Mapping(target = "id", source = "uuid")
    AuditLogResult toResult(AuditLogJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", expression = "java(java.util.UUID.randomUUID())")
    AuditLogJpaEntity toNewEntity(RecordAuditLogCommand command);

    @ObjectFactory
    default AuditLogJpaEntity createEntity() {
        return AuditLogJpaEntity.create();
    }
}
