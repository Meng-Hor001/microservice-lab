package kh.edu.istad.platform.restapi.mapper;

import kh.edu.istad.platform.iam.application.dto.CreatePermissionCommand;
import kh.edu.istad.platform.iam.application.dto.PermissionResult;
import kh.edu.istad.platform.iam.application.dto.UpdatePermissionCommand;
import kh.edu.istad.platform.restapi.dto.request.CreatePermissionRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdatePermissionRequest;
import kh.edu.istad.platform.restapi.dto.response.PermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PermissionApiMapper {
    CreatePermissionCommand toCommand(CreatePermissionRequest request);

    @Mapping(target = "id", source = "uuid")
    UpdatePermissionCommand toCommand(
            UUID uuid,
            UpdatePermissionRequest request
    );

    PermissionResponse toResponse(PermissionResult permission);
}
