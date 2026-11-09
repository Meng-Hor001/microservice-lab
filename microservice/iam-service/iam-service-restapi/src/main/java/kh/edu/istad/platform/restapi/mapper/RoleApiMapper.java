package kh.edu.istad.platform.restapi.mapper;

import kh.edu.istad.platform.iam.application.dto.*;
import kh.edu.istad.platform.restapi.dto.request.AssignPermissionsToRoleRequest;
import kh.edu.istad.platform.restapi.dto.request.CreateRoleRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdateRoleRequest;
import kh.edu.istad.platform.restapi.dto.response.PermissionAssignmentResponse;
import kh.edu.istad.platform.restapi.dto.response.RoleResponse;
import kh.edu.istad.platform.restapi.dto.response.RoleSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RoleApiMapper {
    CreateRoleCommand toCommand(CreateRoleRequest request);

    @Mapping(target = "uuid", source = "uuid")
    UpdateRoleCommand toCommand(
            UUID uuid,
            UpdateRoleRequest request
    );

    @Mapping(target = "roleUuid", source = "uuid")
    AssignPermissionsToRoleCommand toCommand(
            UUID uuid,
            AssignPermissionsToRoleRequest request
    );

    RoleResponse toResponse(RoleResult role);

    RoleSummaryResponse toResponse(RoleSummary role);

    PermissionAssignmentResponse toResponse(
            PermissionAssignmentResult assignment
    );
}
