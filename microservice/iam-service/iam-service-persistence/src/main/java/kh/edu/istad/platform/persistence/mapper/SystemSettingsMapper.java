package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.application.dto.SystemSettingsResult;
import kh.edu.istad.platform.iam.application.dto.UpdateSystemSettingsCommand;
import kh.edu.istad.platform.persistence.entity.SystemSettingsJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SystemSettingsMapper {
    SystemSettingsResult toResult(SystemSettingsJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateSystemSettingsCommand command, @MappingTarget SystemSettingsJpaEntity entity);
}
